package no.nav.opptjening.hoi;

import no.nav.opptjening.nais.ApplicationRunner;
import no.nav.opptjening.nais.NaisHttpServer;
import no.nav.opptjening.skatt.api.beregnetskatt.BeregnetSkattClient;
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
                    env.getOrDefault("SKATT_API_URL", "https://api-gw-q0.adeo.no/ekstern/skatt/datasamarbeid/api/formueinntekt/beregnetskatt/"),
                    env.get("SKATT_API_KEY"));

            SkatteoppgjorhendelseConsumer skatteoppgjorhendelseConsumer = new SkatteoppgjorhendelseConsumer(kafkaConfiguration.hendelseConsumer(), beregnetSkattClient);
            PensjonsgivendeInntektKafkaProducer inntektProducer = new PensjonsgivendeInntektKafkaProducer(kafkaConfiguration.inntektsProducer());

            PensjonsgivendeInntektTask consumer = new PensjonsgivendeInntektTask(skatteoppgjorhendelseConsumer, inntektProducer);

            appRunner = new ApplicationRunner(consumer, naisHttpServer);

            appRunner.addShutdownListener(skatteoppgjorhendelseConsumer::shutdown);
            appRunner.addShutdownListener(inntektProducer::shutdown);
        } catch (Exception e) {
            LOG.error("Application failed to start", e);
            return;
        }
        appRunner.run();
    }
}
