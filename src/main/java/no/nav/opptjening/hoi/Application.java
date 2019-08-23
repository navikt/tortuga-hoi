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

import static java.lang.System.*;
import static no.nav.opptjening.hoi.ApplicationProperties.*;

public class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);
    private final KafkaStreams beregnetSkattStream;

    public static void main(String[] args) {
        try {
            final Application app = new Application(new KafkaConfiguration(getenv()), beregnetSkattClient(), hendelseFilter());
            app.start();

            final NaisHttpServer naisHttpServer = new NaisHttpServer(app::isRunning, () -> true);
            naisHttpServer.start();
            addShutdownHook(naisHttpServer);

        } catch (Exception e) {
            LOG.error("Application failed to start", e);
            exit(1);
        }
    }

    Application(KafkaConfiguration kafkaConfiguration, BeregnetSkattClient beregnetSkattClient, HendelseFilter hendelseFilter) {

        StreamsBuilder streamBuilder = new StreamsBuilder();
        KStream<HendelseKey, Hendelse> stream = streamBuilder.stream(KafkaConfiguration.SKATTEOPPGJØRHENDELSE_TOPIC);
        stream.filter(hendelseFilter::testThatHendelseIsFromValidYear)
                .mapValues(new BeregnetSkattMapper(beregnetSkattClient))
                .mapValues(new PensjonsgivendeInntektMapper())
                .to(KafkaConfiguration.PENSJONSGIVENDE_INNTEKT_TOPIC);

        beregnetSkattStream = new KafkaStreams(streamBuilder.build(), kafkaConfiguration.streamsConfiguration());
        beregnetSkattStream.setUncaughtExceptionHandler((t, e) -> {
            LOG.error("Uncaught exception in thread {}, closing beregnetSkattStream", t, e);
            beregnetSkattStream.close();
        });
        beregnetSkattStream.setStateListener((newState, oldState) ->
                LOG.debug("State change from {} to {}", oldState, newState)
        );

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    private static HendelseFilter hendelseFilter() {
        String earliestValidHendelseYear = getFromEnvironment(getenv(), "EARLIEST_VALID_HENDELSE_YEAR");
        return new HendelseFilter(earliestValidHendelseYear);
    }

    private static BeregnetSkattClient beregnetSkattClient() {
        String beregnetSkattUrl = getFromEnvironment(getenv(), "BEREGNETSKATT_API_URL");
        String summertskattApiUrl = getFromEnvironment(getenv(), "SUMMERTSKATTEGRUNNLAG_API_URL");
        JsonApi jsonApiForBeregnetSkatt = JsonApiBuilder.createJsonApi(AuthenticationFromEnv.forBeregnetSkatt(getenv()));
        JsonApi jsonApiForSvalbardinntekt = JsonApiBuilder.createJsonApi(AuthenticationFromEnv.forSummertSkatt(getenv()));
        SvalbardApi svalbardApi = new SvalbardApi(summertskattApiUrl, jsonApiForSvalbardinntekt);
        return new BeregnetSkattClient(svalbardApi, beregnetSkattUrl, jsonApiForBeregnetSkatt);
    }

    private static void addShutdownHook(NaisHttpServer naisHttpServer) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                LOG.info("stopping nais http server");
                naisHttpServer.stop();
            } catch (Exception e) {
                LOG.error("Error while shutting down nais http server", e);
            }
        }));
    }

    private boolean isRunning() {
        return beregnetSkattStream.state().isRunning();
    }

    void start() {
        beregnetSkattStream.start();
    }

    void shutdown() {
        beregnetSkattStream.close();
    }
}
