package no.nav.opptjening.hoi;

import no.nav.opptjening.skatt.client.api.JsonApi;
import no.nav.opptjening.skatt.client.api.JsonApiBuilder;
import no.nav.opptjening.skatt.client.api.beregnetskatt.BeregnetSkattClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComponentTest {


    private static final String EARLIEST_VALID_HENDELSE_YEAR = "2017";


    private static Application app;

    @BeforeAll
    static void setUp() {
        MockApi.start();
        KafkaTestEnvironment.setup();
        KafkaConfiguration kafkaConfiguration = KafkaTestEnvironment.getKafkaConfiguration();
        app = new Application(kafkaConfiguration, beregnetSkattClient(), new HendelseFilter(EARLIEST_VALID_HENDELSE_YEAR));
    }

    private static BeregnetSkattClient beregnetSkattClient() {
        JsonApi jsonApi = JsonApiBuilder.createJsonApi(() -> "foobar");
        return new BeregnetSkattClient(null, "http://localhost:" + MockApi.port() + "/", jsonApi);
    }

    @AfterAll
    static void tearDown() {
        KafkaTestEnvironment.tearDown();
        MockApi.stop();
    }

    @Test
    void application_reads_messages_from_topic_then_sends_filtered_messages_to_another_topic() throws Exception {
        KafkaTestEnvironment.createRecords();
        MockApi.initBeregnetSkatt();

        CountDownLatch expectedProducedRecordsCount = new CountDownLatch(6);

        Thread t1 = new Thread(() -> KafkaTestEnvironment.pensjonsgivendeInntektConsumerThread(expectedProducedRecordsCount));

        try {
            app.start();
            t1.start();

            assertTrue(expectedProducedRecordsCount.await(50000L, TimeUnit.MILLISECONDS));
            assertEquals(0, expectedProducedRecordsCount.getCount());
        } finally {
            t1.interrupt();
            app.shutdown();
        }
    }
}
