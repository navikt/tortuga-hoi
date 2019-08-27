package no.nav.opptjening.hoi;

import no.nav.opptjening.schema.skatt.hendelsesliste.Hendelse;
import no.nav.opptjening.skatt.client.api.beregnetskatt.BeregnetSkattClient;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static no.nav.opptjening.hoi.KafkaConfiguration.PENSJONSGIVENDE_INNTEKT_TOPIC;
import static no.nav.opptjening.hoi.KafkaTestEnvironment.*;
import static no.nav.opptjening.hoi.MockApi.port;
import static no.nav.opptjening.hoi.MockApi.setUpBeregnetSkattStubs;
import static no.nav.opptjening.skatt.client.api.JsonApiBuilder.createJsonApi;
import static org.junit.jupiter.api.Assertions.*;

class ComponentTest {

    private static final String EARLIEST_VALID_HENDELSE_YEAR = "2017";

    @BeforeAll
    static void setUp() {
        MockApi.start();
        KafkaTestEnvironment.setup();
        new Application(getKafkaConfiguration(), beregnetSkattClient(), new HendelseFilter(EARLIEST_VALID_HENDELSE_YEAR)).start();
    }

    private static BeregnetSkattClient beregnetSkattClient() {
        return new BeregnetSkattClient(null, "http://localhost:" + port() + "/", createJsonApi(() -> "foobar"));
    }

    @AfterAll
    static void tearDown() {
        MockApi.stop();
    }

    @Test
    void application_reads_messages_from_topic_then_sends_filtered_messages_to_another_topic() {
        setUpBeregnetSkattStubs();

        var hendelser = asList(
                new Hendelse(1L, "01029804032", "2017"),
                new Hendelse(7L, "12412512533", "2016"),
                new Hendelse(50L, "12412513568", "2015"),
                new Hendelse(133L, "11987654321", "2018")
        );
        populateHendelseTopic(hendelser);

        var consumerRecords = stream(getConsumerRecords().records(PENSJONSGIVENDE_INNTEKT_TOPIC).spliterator(), false).collect(toList());
        var consumerRecordKeys = consumerRecords.stream()
                .map(ConsumerRecord::key).collect(toList());
        var consumerRecordValues = consumerRecords.stream()
                .map(ConsumerRecord::value).collect(toList());
        assertEquals(consumerRecordKeys.get(0).getIdentifikator(), hendelser.get(0).getIdentifikator());
        assertNotNull(consumerRecordValues.get(0));
        assertEquals(consumerRecordKeys.get(1).getIdentifikator(), hendelser.get(3).getIdentifikator());
        assertNull(consumerRecordValues.get(1));
    }
}
