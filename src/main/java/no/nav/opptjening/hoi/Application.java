package no.nav.opptjening.hoi;

import no.nav.opptjening.nais.NaisHttpServer;
import no.nav.opptjening.schema.skatt.hendelsesliste.Hendelse;
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
            NaisHttpServer naisHttpServer = new NaisHttpServer();
            naisHttpServer.run();

            KafkaConfiguration kafkaConfiguration = new KafkaConfiguration(env);

            final BeregnetSkattClient beregnetSkattClient = new BeregnetSkattClient(
                    env.getOrDefault("SKATT_API_URL", "https://api-gw-q0.adeo.no/ekstern/skatt/datasamarbeid/api/formueinntekt/beregnetskatt/"),
                    env.get("SKATT_API_KEY"));

            final Application app = new Application(kafkaConfiguration.streamsConfiguration(), beregnetSkattClient);
            app.start();
        } catch (Exception e) {
            LOG.error("Application failed to start", e);
            System.exit(1);
        }
    }

    public Application(Properties properties, BeregnetSkattClient beregnetSkattClient) {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Hendelse> stream = builder.stream(KafkaConfiguration.BEREGNET_SKATT_HENDELSE_TOPIC);
        stream.filter(HendelseFilter::testThatHendelseIsFromValidYear)
                .transformValues(() -> new BeregnetSkattMapper(beregnetSkattClient))
                .mapValues(new PensjonsgivendeInntektMapper())
                .to(KafkaConfiguration.PENSJONSGIVENDE_INNTEKT_TOPIC);

        streams = new KafkaStreams(builder.build(), properties);
        streams.setUncaughtExceptionHandler((t, e) -> LOG.error("Uncaught exception in thread {}", t, e));

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    public void start() {
        streams.start();
    }

    public void shutdown() {
        streams.close();
    }
}
