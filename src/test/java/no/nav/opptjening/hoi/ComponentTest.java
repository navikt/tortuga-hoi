package no.nav.opptjening.hoi;

import com.github.tomakehurst.wiremock.WireMockServer;
import no.nav.opptjening.schema.PensjonsgivendeInntekt;
import no.nav.opptjening.schema.skatt.hendelsesliste.HendelseKey;
import no.nav.opptjening.skatt.client.api.JsonApi;
import no.nav.opptjening.skatt.client.api.JsonApiBuilder;
import no.nav.opptjening.skatt.client.api.beregnetskatt.BeregnetSkattClient;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.KafkaException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComponentTest {

    private static final Logger LOG = LoggerFactory.getLogger(ComponentTest.class);
    private static final WireMockServer wireMockServer = new WireMockServer();

    private static final String EARLIEST_VALID_HENDELSE_YEAR = "2017";

    private Consumer<HendelseKey, PensjonsgivendeInntekt> pensjonsgivendeInntektConsumer;

    private static Application app;

    @BeforeAll
    static void setUp() {
        wireMockServer.start();
        KafkaTestEnvironment.setup();
        KafkaConfiguration kafkaConfiguration = KafkaTestEnvironment.getKafkaConfiguration();
        app = new Application(kafkaConfiguration, beregnetSkattClient(), new HendelseFilter(EARLIEST_VALID_HENDELSE_YEAR));
    }

    private static BeregnetSkattClient beregnetSkattClient() {
        JsonApi jsonApi = JsonApiBuilder.createJsonApi(() -> "foobar");
        return new BeregnetSkattClient(null, "http://localhost:" + wireMockServer.port() + "/", jsonApi);
    }

    @AfterAll
    static void tearDown() {
        KafkaTestEnvironment.tearDown();
        wireMockServer.stop();
    }

    @Test
    void application_reads_messages_from_topic_then_sends_filtered_messages_to_another_topic() throws Exception {
        pensjonsgivendeInntektConsumer = KafkaTestEnvironment.createRecords();
        MockApi.initBeregnetSkatt();

        CountDownLatch expectedProducedRecordsCount = new CountDownLatch(6);

        Thread t1 = new Thread(() -> pensjonsgivendeInntektConsumerThread(expectedProducedRecordsCount));

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


    private void pensjonsgivendeInntektConsumerThread(CountDownLatch latch) {
        pensjonsgivendeInntektConsumer.subscribe(Collections.singletonList("aapen-opptjening-pensjonsgivendeInntekt"));
        try {
            while (!Thread.currentThread().isInterrupted() && latch.getCount() > 0) {
                ConsumerRecords<HendelseKey, PensjonsgivendeInntekt> consumerRecords = pensjonsgivendeInntektConsumer.poll(Duration.ofSeconds(5L));

                for (ConsumerRecord<HendelseKey, PensjonsgivendeInntekt> record : consumerRecords) {
                    LOG.info("Received record = {}", record);
                    latch.countDown();
                }
            }
        } catch (KafkaException e) {
            LOG.error("Error while polling records", e);
        }
    }
}
