package no.nav.opptjening.hoi;

import no.nav.opptjening.schema.skatt.hendelsesliste.Hendelse;
import no.nav.opptjening.skatt.client.api.beregnetskatt.BeregnetSkattClient;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.*;

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
    private static final String INVALID_HENDELSE_YEAR = "2016";

    @BeforeAll
    static void setUp() {
        MockApi.start();
        KafkaTestEnvironment.setup();
        new Application(getKafkaConfiguration(), beregnetSkattClient(), new HendelseFilter(EARLIEST_VALID_HENDELSE_YEAR)).start();
        KafkaTestEnvironment.createConsumer();
    }

    private static BeregnetSkattClient beregnetSkattClient() {
        return new BeregnetSkattClient(null, "http://localhost:" + port() + "/", createJsonApi(() -> "foobar"));
    }

    @AfterAll
    static void tearDown() {
        MockApi.stop();
    }

    @Test
    void application_reads_messages_from_hendelse_topic_then_sends_filtered_messages_to_pensjonsgivende_inntekt_topic() {
        setUpBeregnetSkattStubs();

        var hendelser = asList(
                new Hendelse(1L, "01029804032", "2017"),
                new Hendelse(7L, "12412512533", INVALID_HENDELSE_YEAR),
                new Hendelse(50L, "12412513568", INVALID_HENDELSE_YEAR),
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

    @Test
    void application_filters_out_hendelser_from_invalid_year() {
        setUpBeregnetSkattStubs();

        var hendelser = asList(
                new Hendelse(7L, "12412512533", INVALID_HENDELSE_YEAR),
                new Hendelse(50L, "12412513568", INVALID_HENDELSE_YEAR)
        );
        populateHendelseTopic(hendelser);
        assertTrue(getConsumerRecords().isEmpty());
    }
}
