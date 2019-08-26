package no.nav.opptjening.hoi;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.confluent.kafka.serializers.*;
import no.nav.common.KafkaEnvironment;
import no.nav.opptjening.schema.PensjonsgivendeInntekt;
import no.nav.opptjening.schema.skatt.hendelsesliste.Hendelse;
import no.nav.opptjening.schema.skatt.hendelsesliste.HendelseKey;
import no.nav.opptjening.skatt.client.api.JsonApi;
import no.nav.opptjening.skatt.client.api.JsonApiBuilder;
import no.nav.opptjening.skatt.client.api.beregnetskatt.BeregnetSkattClient;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static no.nav.opptjening.hoi.KafkaConfiguration.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComponentTest {

    private static final Logger LOG = LoggerFactory.getLogger(ComponentTest.class);
    private static final WireMockServer wireMockServer = new WireMockServer();

    private static final int NUMBER_OF_BROKERS = 3;
    private static final List<String> TOPICS = Arrays.asList(PENSJONSGIVENDE_INNTEKT_TOPIC, SKATTEOPPGJØRHENDELSE_TOPIC);
    private static final String KAFKA_USERNAME = "srvTest";
    private static final String KAFKA_PASSWORD = "opensourcedPassword";
    private static final String EARLIEST_VALID_HENDELSE_YEAR = "2017";

    private Consumer<HendelseKey, PensjonsgivendeInntekt> pensjonsgivendeInntektConsumer;

    private static Application app;
    private static KafkaEnvironment kafkaEnvironment;

    @BeforeAll
    static void setUp() {
        wireMockServer.start();
        kafkaEnvironment = new KafkaEnvironment(NUMBER_OF_BROKERS, TOPICS, Collections.emptyList(), true, false, Collections.emptyList(), false, new Properties());
        kafkaEnvironment.start();
        KafkaConfiguration kafkaConfiguration = new KafkaConfiguration(testEnvironment());
        app = new Application(kafkaConfiguration, beregnetSkattClient(), new HendelseFilter(EARLIEST_VALID_HENDELSE_YEAR));
    }

    private static BeregnetSkattClient beregnetSkattClient() {
        JsonApi jsonApi = JsonApiBuilder.createJsonApi(() -> "foobar");
        return new BeregnetSkattClient(null, "http://localhost:" + wireMockServer.port() + "/", jsonApi);
    }

    @AfterAll
    static void tearDown() {
        kafkaEnvironment.tearDown();
        wireMockServer.stop();
    }

    @Test
    void application_reads_messages_from_topic_then_sends_filtered_messages_to_another_topic() throws Exception {
        createTestRecords();
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

    private static Map<String, String> testEnvironment() {
        Map<String, String> testEnvironment = new HashMap<>();
        testEnvironment.put("KAFKA_BOOTSTRAP_SERVERS", kafkaEnvironment.getBrokersURL());
        testEnvironment.put("SCHEMA_REGISTRY_URL", kafkaEnvironment.getSchemaRegistry().getUrl());
        testEnvironment.put("KAFKA_USERNAME", KAFKA_USERNAME);
        testEnvironment.put("KAFKA_PASSWORD", KAFKA_PASSWORD);
        testEnvironment.put("KAFKA_SASL_MECHANISM", "PLAIN");
        testEnvironment.put("KAFKA_SECURITY_PROTOCOL", "PLAINTEXT");
        return testEnvironment;
    }

    private void createTestRecords() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafkaEnvironment.getBrokersURL());
        configs.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, kafkaEnvironment.getSchemaRegistry().getUrl());

        Map<String, Object> producerConfig = new HashMap<>(configs);
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);

        producerConfig.put(ProducerConfig.ACKS_CONFIG, "all");
        producerConfig.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);

        Producer<HendelseKey, Hendelse> producer = new KafkaProducer<>(producerConfig);

        Map<String, Object> consumerConfigs = new HashMap<>(configs);
        consumerConfigs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        consumerConfigs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        consumerConfigs.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        consumerConfigs.put(ConsumerConfig.GROUP_ID_CONFIG, "loot-consumer-group");
        consumerConfigs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        consumerConfigs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        pensjonsgivendeInntektConsumer = new KafkaConsumer<>(consumerConfigs);

        List<Hendelse> hendelser = new LinkedList<>();
        hendelser.add(new Hendelse(1L, "01029804032", "2017"));
        hendelser.add(new Hendelse(2L, "04057849687", "2017"));
        hendelser.add(new Hendelse(3L, "09038800237", "2017"));
        hendelser.add(new Hendelse(4L, "01029413157", "2017"));
        hendelser.add(new Hendelse(5L, "10026300407", "2017"));
        hendelser.add(new Hendelse(6L, "10016000383", "2017"));
        hendelser.add(new Hendelse(7L, "04063100264", "2016"));
        hendelser.add(new Hendelse(8L, "04116500200", "2016"));
        hendelser.add(new Hendelse(9L, "04126200248", "2016"));
        hendelser.add(new Hendelse(10L, "04063100264", "2015"));
        hendelser.add(new Hendelse(11L, "04116500200", "2015"));
        hendelser.add(new Hendelse(12L, "04126200248", "2015"));
        hendelser.add(new Hendelse(13L, "11987654321", "2017"));

        final String topic = "privat-tortuga-skatteoppgjorhendelse";
        for (Hendelse hendelse : hendelser) {
            producer.send(new ProducerRecord<>(topic, HendelseKey.newBuilder()
                    .setIdentifikator(hendelse.getIdentifikator())
                    .setGjelderPeriode(hendelse.getGjelderPeriode()).build(), hendelse));
        }
        producer.flush();
    }

    private void createMockApi() {

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/nav/2017/01029804032"))
                .withHeader("X-Nav-Apikey", WireMock.equalTo("foobar"))
                .willReturn(WireMock.okJson("{\n" +
                        "  \"personidentifikator\": \"01029804032\",\n" +
                        "  \"inntektsaar\": \"2017\",\n" +
                        "  \"personinntektLoenn\": 350371,\n" +
                        "  \"skjermet\": false,\n" +
                        "  \"skatteoppgjoersdato\": \"2018-06-06\"\n" +
                        "}")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/nav/2017/04057849687"))
                .withHeader("X-Nav-Apikey", WireMock.equalTo("foobar"))
                .willReturn(WireMock.okJson("{\n" +
                        "  \"personidentifikator\": \"04057849687\",\n" +
                        "  \"inntektsaar\": \"2017\",\n" +
                        "  \"personinntektLoenn\": 350371,\n" +
                        "  \"skjermet\": false,\n" +
                        "  \"skatteoppgjoersdato\": \"2018-06-06\"\n" +
                        "}")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/nav/2017/09038800237"))
                .withHeader("X-Nav-Apikey", WireMock.equalTo("foobar"))
                .willReturn(WireMock.okJson("{\n" +
                        "  \"personidentifikator\": \"09038800237\",\n" +
                        "  \"inntektsaar\": \"2017\",\n" +
                        "  \"personinntektLoenn\": 192483,\n" +
                        "  \"personinntektNaering\": 23090,\n" +
                        "  \"skjermet\": false,\n" +
                        "  \"skatteoppgjoersdato\": \"2018-06-06\"\n" +
                        "}")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/nav/2017/01029413157"))
                .withHeader("X-Nav-Apikey", WireMock.equalTo("foobar"))
                .willReturn(WireMock.okJson("{\n" +
                        "  \"personidentifikator\": \"01029413157\",\n" +
                        "  \"inntektsaar\": \"2017\",\n" +
                        "  \"personinntektLoenn\": 195604,\n" +
                        "  \"personinntektFiskeFangstFamiliebarnehage\": 7860,\n" +
                        "  \"personinntektNaering\": 29540,\n" +
                        "  \"skjermet\": false,\n" +
                        "  \"skatteoppgjoersdato\": \"2018-06-06\"\n" +
                        "}")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/nav/2017/10026300407"))
                .withHeader("X-Nav-Apikey", WireMock.equalTo("foobar"))
                .willReturn(WireMock.okJson("{\n" +
                        "  \"personidentifikator\": \"10026300407\",\n" +
                        "  \"inntektsaar\": \"2017\",\n" +
                        "  \"personinntektLoenn\": 160000,\n" +
                        "  \"skjermet\": false,\n" +
                        "  \"skatteoppgjoersdato\": \"2018-06-06\"\n" +
                        "}")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/nav/2017/10016000383"))
                .withHeader("X-Nav-Apikey", WireMock.equalTo("foobar"))
                .willReturn(WireMock.okJson("{\n" +
                        "  \"personidentifikator\": \"10016000383\",\n" +
                        "  \"inntektsaar\": \"2017\",\n" +
                        "  \"personinntektLoenn\": 444800,\n" +
                        "  \"personinntektNaering\": 24600,\n" +
                        "  \"skjermet\": false,\n" +
                        "  \"skatteoppgjoersdato\": \"2018-06-06\"\n" +
                        "}")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/nav/2017/11987654321"))
                .withHeader("X-Nav-Apikey", WireMock.equalTo("foobar"))
                .willReturn(WireMock.notFound().withBody("{\n" +
                        "  \"kode\": \"BSA-006\",\n" +
                        "  \"melding\": \"Fant ikke Beregnet Skatt for gitt inntektsår og identifikator\",\n" +
                        "  \"korrelasjonsid\": \"13a865f5-28f9-47db-9abd-ab78977c79fe\"\n" +
                        "}")));
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
