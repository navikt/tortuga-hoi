package no.nav.opptjening.hoi;

import no.nav.opptjening.skatt.api.beregnetskatt.BeregnetSkattClient;
import no.nav.opptjening.skatt.schema.hendelsesliste.Hendelse;
import org.apache.kafka.clients.consumer.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    public static void main(String [] args) {

        Map<String, String> env = System.getenv();

        ApplicationRunner appRunner;

        try {

            NaisHttpServer naisHttpServer = new NaisHttpServer();
            KafkaConfiguration kafkaConfiguration = new KafkaConfiguration(env);

            BeregnetSkattClient beregnetSkattClient = new BeregnetSkattClient(
                    env.getOrDefault("SKATT_API_URL", "http://tortuga-testapi/ekstern/skatt/datasamarbeid/api/formueinntekt/beregnetskatt/"));

            PensjonsgivendeInntektKafkaProducer inntektProducer = new PensjonsgivendeInntektKafkaProducer(kafkaConfiguration.inntektsProducer());
            Consumer<String, Hendelse> hendelseConsumer = kafkaConfiguration.hendelseConsumer();

            BeregnetSkattHendelseConsumer consumer = new BeregnetSkattHendelseConsumer(beregnetSkattClient, inntektProducer, hendelseConsumer);

            appRunner = new ApplicationRunner(consumer, naisHttpServer);

        } catch (Exception e) {
            LOG.error("Application failed to start", e);
            return;
        }
        appRunner.run();
    }
}
