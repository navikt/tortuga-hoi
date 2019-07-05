package no.nav.opptjening.hoi;

import no.nav.opptjening.nais.NaisHttpServer;
import no.nav.opptjening.schema.skatt.hendelsesliste.Hendelse;
import no.nav.opptjening.schema.skatt.hendelsesliste.HendelseKey;
import no.nav.opptjening.skatt.client.api.JsonApi;
import no.nav.opptjening.skatt.client.api.JsonApiBuilder;
import no.nav.opptjening.skatt.client.api.beregnetskatt.BeregnetSkattClient;
import no.nav.opptjening.skatt.client.api.beregnetskatt.SvalbardApi;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

public class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);
    private final KafkaStreams streams;

    public static void main(String[] args) {
        Map<String, String> environment = System.getenv();

        try {
            KafkaConfiguration kafkaConfiguration = new KafkaConfiguration(environment);
            Properties streamsConfig = kafkaConfiguration.streamsConfiguration();
            String beregnetSkattUrl = environment.get("BEREGNETSKATT_API_URL");
            String summertskattApiUrl = environment.get("SUMMERTSKATTEGRUNNLAG_API_URL");

            JsonApi jsonApiForBeregnetSkatt = JsonApiBuilder.createJsonApi(AuthenticationFromEnv.forBeregnetSkatt(environment));
            JsonApi jsonApiForSvalbardinntekt = JsonApiBuilder.createJsonApi(AuthenticationFromEnv.forSummertSkatt(environment));
            SvalbardApi svalbardApi = new SvalbardApi(summertskattApiUrl, jsonApiForSvalbardinntekt);
            final BeregnetSkattClient beregnetSkattClient = new BeregnetSkattClient(svalbardApi, beregnetSkattUrl, jsonApiForBeregnetSkatt);

            String earliestValidHendelseYear = environment.get("EARLIEST_VALID_HENDELSE_YEAR");
            final HendelseFilter hendelseFilter = new HendelseFilter(earliestValidHendelseYear);

            final Application app = new Application(streamsConfig, beregnetSkattClient, hendelseFilter);
            app.start();

            final NaisHttpServer naisHttpServer = new NaisHttpServer(app::isRunning, () -> true);
            naisHttpServer.start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    LOG.info("stopping nais http server");
                    naisHttpServer.stop();
                } catch (Exception e) {
                    LOG.error("Error while shutting down nais http server", e);
                }
            }));

        } catch (Exception e) {
            LOG.error("Application failed to start", e);
            System.exit(1);
        }
    }

    private boolean isRunning() {
        return streams.state().isRunning();
    }

    public Application(Properties streamsConfig, BeregnetSkattClient beregnetSkattClient, HendelseFilter hendelseFilter) {

        StreamsBuilder builder = new StreamsBuilder();
        KStream<HendelseKey, Hendelse> stream = builder.stream(KafkaConfiguration.SKATTEOPPGJØRHENDELSE_TOPIC);
        stream.filter(hendelseFilter::testThatHendelseIsFromValidYear)
                .mapValues(new BeregnetSkattMapper(beregnetSkattClient))
                .mapValues(new PensjonsgivendeInntektMapper())
                .to(KafkaConfiguration.PENSJONSGIVENDE_INNTEKT_TOPIC);

        streams = new KafkaStreams(builder.build(), streamsConfig);
        streams.setUncaughtExceptionHandler((t, e) -> {
            LOG.error("Uncaught exception in thread {}, closing streams", t, e);
            streams.close();
        });
        streams.setStateListener((newState, oldState) ->
            LOG.debug("State change from {} to {}", oldState, newState)
        );

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    public void start() {
        streams.start();
    }

    public void shutdown() {
        streams.close();
    }
}
