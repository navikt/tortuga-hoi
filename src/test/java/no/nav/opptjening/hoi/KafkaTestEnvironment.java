package no.nav.opptjening.hoi;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import no.nav.common.KafkaEnvironment;
import no.nav.opptjening.schema.PensjonsgivendeInntekt;
import no.nav.opptjening.schema.skatt.hendelsesliste.Hendelse;
import no.nav.opptjening.schema.skatt.hendelsesliste.HendelseKey;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static no.nav.opptjening.hoi.KafkaConfiguration.PENSJONSGIVENDE_INNTEKT_TOPIC;
import static no.nav.opptjening.hoi.KafkaConfiguration.SKATTEOPPGJØRHENDELSE_TOPIC;

class KafkaTestEnvironment {
    private static final String KAFKA_USERNAME = "srvTest";
    private static final String KAFKA_PASSWORD = "opensourcedPassword";
    private static final List<String> TOPICS = Arrays.asList(PENSJONSGIVENDE_INNTEKT_TOPIC, SKATTEOPPGJØRHENDELSE_TOPIC);
    private static final String RECORD_TOPIC = "privat-tortuga-skatteoppgjorhendelse";
    private static final int NUMBER_OF_BROKERS = 3;
    private static final Logger LOG = LoggerFactory.getLogger(ComponentTest.class);

    private static Map<String, Object> configs;
    private static KafkaEnvironment kafkaEnvironment;

    static void setup() {
        kafkaEnvironment = new KafkaEnvironment(NUMBER_OF_BROKERS, TOPICS, Collections.emptyList(), true, false, Collections.emptyList(), false, new Properties());
        kafkaEnvironment.start();
        configs = getCommonConfig();
    }

    private static String getBrokersURL() {
        return kafkaEnvironment.getBrokersURL();
    }

    private static String getSchemaRegistryUrl() {
        return Objects.requireNonNull(kafkaEnvironment.getSchemaRegistry()).getUrl();
    }

    static KafkaConfiguration getKafkaConfiguration() {
        return new KafkaConfiguration(getTestEnvironment());
    }

    private static Map<String, String> getTestEnvironment() {
        Map<String, String> testEnvironment = new HashMap<>();
        testEnvironment.put("KAFKA_BOOTSTRAP_SERVERS", getBrokersURL());
        testEnvironment.put("SCHEMA_REGISTRY_URL", getSchemaRegistryUrl());
        testEnvironment.put("KAFKA_USERNAME", KAFKA_USERNAME);
        testEnvironment.put("KAFKA_PASSWORD", KAFKA_PASSWORD);
        testEnvironment.put("KAFKA_SASL_MECHANISM", "PLAIN");
        testEnvironment.put("KAFKA_SECURITY_PROTOCOL", "PLAINTEXT");
        return testEnvironment;
    }

    private static List<Hendelse> getHendelser() {
        List<Hendelse> hendelser = new LinkedList<>();
        hendelser.add(new Hendelse(1L, "01029804032", "2017"));
        hendelser.add(new Hendelse(7L, "04063100264", "2016"));
        hendelser.add(new Hendelse(50L, "04063100264", "2015"));
        hendelser.add(new Hendelse(133L, "11987654321", "2018"));
        return hendelser;
    }

    private static Map<String, Object> getCommonConfig() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, getBrokersURL());
        configs.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, getSchemaRegistryUrl());
        return configs;
    }

    private static KafkaProducer<HendelseKey, Hendelse> getKafkaProducer() {
        Map<String, Object> producerConfig = new HashMap<>(configs);
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        producerConfig.put(ProducerConfig.ACKS_CONFIG, "all");
        producerConfig.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        return new KafkaProducer<>(producerConfig);

    }

    private static KafkaConsumer<HendelseKey, PensjonsgivendeInntekt> getKafkaConsumer() {
        Map<String, Object> consumerConfigs = new HashMap<>(configs);
        consumerConfigs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        consumerConfigs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        consumerConfigs.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        consumerConfigs.put(ConsumerConfig.GROUP_ID_CONFIG, "loot-consumer-group");
        consumerConfigs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        consumerConfigs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new KafkaConsumer<>(consumerConfigs);
    }

    private static ProducerRecord<HendelseKey, Hendelse> createRecord(Hendelse hendelse) {
        return new ProducerRecord<>(RECORD_TOPIC, HendelseKey.newBuilder()
                .setIdentifikator(hendelse.getIdentifikator())
                .setGjelderPeriode(hendelse.getGjelderPeriode()).build(), hendelse);
    }

    static void createRecords() {
        Producer<HendelseKey, Hendelse> producer = getKafkaProducer();
        getHendelser().stream().map(KafkaTestEnvironment::createRecord).forEach(producer::send);
    }


    static void pensjonsgivendeInntektConsumerThread(CountDownLatch latch) {
        Consumer<HendelseKey, PensjonsgivendeInntekt> pensjonsgivendeInntektConsumer = getKafkaConsumer();

        pensjonsgivendeInntektConsumer.subscribe(Collections.singletonList("aapen-opptjening-pensjonsgivendeInntekt"));
        Duration duration = Duration.ofSeconds(5L);
        try {
            while (!Thread.currentThread().isInterrupted() && latch.getCount() > 0) {
                ConsumerRecords<HendelseKey, PensjonsgivendeInntekt> consumerRecords = pensjonsgivendeInntektConsumer.poll(duration);

                consumerRecords.forEach(record -> {
                    LOG.info("Received record = {}", record);
                    latch.countDown();
                });
            }
        } catch (KafkaException e) {
            LOG.error("Error while polling records", e);
        }
    }
}
