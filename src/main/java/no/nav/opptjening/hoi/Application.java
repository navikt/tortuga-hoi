package no.nav.opptjening.hoi;

import no.nav.opptjening.nais.NaisHttpServer;
import no.nav.opptjening.skatt.client.api.JsonApi;
import no.nav.opptjening.skatt.client.api.JsonApiBuilder;
import no.nav.opptjening.skatt.client.api.beregnetskatt.BeregnetSkattClient;
import no.nav.opptjening.skatt.client.api.beregnetskatt.SvalbardApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.System.exit;
import static java.lang.System.getenv;
import static no.nav.opptjening.hoi.ApplicationProperties.getFromEnvironment;

public class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);
    private final BeregnetSkattStream beregnetSkattStream;

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
        beregnetSkattStream = new BeregnetSkattStream(beregnetSkattClient, hendelseFilter, kafkaConfiguration);
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
        return beregnetSkattStream.isRunning();
    }

    void start() {
        beregnetSkattStream.start();
    }

    void shutdown() {
        beregnetSkattStream.close();
    }
}
