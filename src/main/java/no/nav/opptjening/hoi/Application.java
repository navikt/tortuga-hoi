package no.nav.opptjening.hoi;

import no.nav.opptjening.nais.NaisHttpServer;
import no.nav.opptjening.schema.skatt.hendelsesliste.Hendelse;
import no.nav.opptjening.schema.skatt.hendelsesliste.HendelseKey;
import no.nav.opptjening.skatt.client.api.beregnetskatt.BeregnetSkattClient;
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
        Map<String, String> env = System.getenv();

        try {
            KafkaConfiguration kafkaConfiguration = new KafkaConfiguration(env);

            String beregnetSkattUrl = env.get("SKATT_API_URL");
            String skattApiKey = env.get("SKATT_API_KEY");

            final BeregnetSkattClient beregnetSkattClient = new BeregnetSkattClient(beregnetSkattUrl, skattApiKey);

            final Application app = new Application(kafkaConfiguration.streamsConfiguration(), beregnetSkattClient);
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

    public Application(Properties properties, BeregnetSkattClient beregnetSkattClient) {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<HendelseKey, Hendelse> stream = builder.stream(KafkaConfiguration.SKATTEOPPGJØRHENDELSE_TOPIC);
        stream.filter(HendelseFilter::testThatHendelseIsFromValidYear)
                .mapValues(new BeregnetSkattMapper(beregnetSkattClient))
                .mapValues(new PensjonsgivendeInntektMapper())
                .to(KafkaConfiguration.PENSJONSGIVENDE_INNTEKT_TOPIC);

        streams = new KafkaStreams(builder.build(), properties);
        streams.setUncaughtExceptionHandler((t, e) -> {
            LOG.error("Uncaught exception in thread {}, closing streams", t, e);
            streams.close();
        });
        streams.setStateListener((newState, oldState) -> {
            LOG.debug("State change from {} to {}", oldState, newState);
        });

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    public void start() {
        streams.start();
    }

    public void shutdown() {
        streams.close();
    }
}
