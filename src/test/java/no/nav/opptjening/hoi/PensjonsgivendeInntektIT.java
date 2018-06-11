package no.nav.opptjening.hoi;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.confluent.kafka.serializers.*;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import no.nav.common.KafkaEnvironment;
import no.nav.opptjening.schema.PensjonsgivendeInntekt;
import no.nav.opptjening.schema.skatt.hendelsesliste.Hendelse;
import no.nav.opptjening.skatt.client.api.beregnetskatt.BeregnetSkattClient;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class PensjonsgivendeInntektIT {

    private static final Logger LOG = LoggerFactory.getLogger(PensjonsgivendeInntektIT.class);

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    private static final int NUMBER_OF_BROKERS = 3;
    private static final List<String> TOPICS = Arrays.asList("privat-tortuga-beregnetSkattHendelseHentet", "aapen-opptjening-pensjonsgivendeInntekt");

    private static KafkaEnvironment kafkaEnvironment;
    private final Properties streamsConfiguration = new Properties();

    private Consumer<String, PensjonsgivendeInntekt> pensjonsgivendeInntektConsumer;

    @Before
    public void setUp() {
        kafkaEnvironment = new KafkaEnvironment(NUMBER_OF_BROKERS, TOPICS, true, false, false);
        kafkaEnvironment.start();

        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaEnvironment.getBrokersURL());
        streamsConfiguration.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, kafkaEnvironment.getServerPark().getSchemaregistry().getUrl());
    }

    @After
    public void tearDown() {
        kafkaEnvironment.stop();
    }

    private void createTestRecords() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafkaEnvironment.getBrokersURL());
        configs.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, kafkaEnvironment.getServerPark().getSchemaregistry().getUrl());

        Map<String, Object> producerConfig = new HashMap<>(configs);
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);

        producerConfig.put(ProducerConfig.ACKS_CONFIG, "all");
        producerConfig.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);

        Producer<String, Hendelse> producer = new KafkaProducer<>(producerConfig);

        Map<String, Object> consumerConfigs = new HashMap<>(configs);
        consumerConfigs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
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

        final String topic = "privat-tortuga-beregnetSkattHendelseHentet";
        for (Hendelse hendelse : hendelser) {
            producer.send(new ProducerRecord<>(topic, hendelse.getGjelderPeriode() + "-" + hendelse.getIdentifikator(), hendelse));
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
    }

    @Test
    public void that_KafkaStreamProcessesCorrectRecordsAndProducesOnNewTopic() throws Exception {
        final Properties config = (Properties)streamsConfiguration.clone();

        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "tortuga-hoi-streams");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        final BeregnetSkattClient client = new BeregnetSkattClient("http://localhost:" + wireMockRule.port() + "/", "foobar");
        final Application app = new Application(config, client);

        createTestRecords();
        createMockApi();

        CountDownLatch expectedProducedRecordsCount = new CountDownLatch(6);

        Thread t1 = new Thread(() -> pensjonsgivendeInntektConsumerThread(expectedProducedRecordsCount));

        try {
            app.start();
            t1.start();

            Assert.assertTrue(expectedProducedRecordsCount.await(20000L, TimeUnit.MILLISECONDS));
            Assert.assertEquals(0, expectedProducedRecordsCount.getCount());
        } finally {
            t1.interrupt();
            app.shutdown();
        }
    }

    private void pensjonsgivendeInntektConsumerThread(CountDownLatch latch) {
        pensjonsgivendeInntektConsumer.subscribe(Collections.singletonList("aapen-opptjening-pensjonsgivendeInntekt"));
        while (!Thread.currentThread().isInterrupted() && latch.getCount() > 0) {
            ConsumerRecords<String, PensjonsgivendeInntekt> consumerRecords = pensjonsgivendeInntektConsumer.poll(500);

            for (ConsumerRecord<String, PensjonsgivendeInntekt> record : consumerRecords) {
                LOG.info("Received record = {}", record);
                latch.countDown();
            }
        }
    }
}
