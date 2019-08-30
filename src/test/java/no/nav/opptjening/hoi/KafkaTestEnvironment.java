package no.nav.opptjening.hoi;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import no.nav.common.KafkaEnvironment;
import no.nav.opptjening.schema.PensjonsgivendeInntekt;
import no.nav.opptjening.schema.skatt.hendelsesliste.*;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static io.confluent.kafka.serializers.KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG;
import static java.lang.Integer.MAX_VALUE;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static no.nav.opptjening.hoi.KafkaConfiguration.*;
import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.*;
import static org.apache.kafka.clients.producer.ProducerConfig.*;

class KafkaTestEnvironment {
    private static final String KAFKA_USERNAME = "srvTest";
    private static final String KAFKA_PASSWORD = "opensourcedPassword";
    private static final List<String> TOPICS = asList(PENSJONSGIVENDE_INNTEKT_TOPIC, SKATTEOPPGJØRHENDELSE_TOPIC);
    private static final String RECORD_TOPIC = "privat-tortuga-skatteoppgjorhendelse";
    private static final int NUMBER_OF_BROKERS = 3;

    private static Map<String, Object> configs;
    private static KafkaEnvironment kafkaEnvironment;
    private static KafkaConsumer<HendelseKey, PensjonsgivendeInntekt> pensjonsgivendeInntektConsumer;

    static void setup() {
        kafkaEnvironment = new KafkaEnvironment(NUMBER_OF_BROKERS, TOPICS, emptyList(), true, false, emptyList(), false, new Properties());
        kafkaEnvironment.start();
        configs = getCommonConfig();
    }

    static void createConsumer() {
        pensjonsgivendeInntektConsumer = getKafkaConsumer();
        pensjonsgivendeInntektConsumer.subscribe(singletonList(PENSJONSGIVENDE_INNTEKT_TOPIC));
    }

    private static String getBrokersURL() {
        return kafkaEnvironment.getBrokersURL();
    }

    private static String getSchemaRegistryUrl() {
        return requireNonNull(kafkaEnvironment.getSchemaRegistry()).getUrl();
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

    private static Map<String, Object> getCommonConfig() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(BOOTSTRAP_SERVERS_CONFIG, getBrokersURL());
        configs.put(SCHEMA_REGISTRY_URL_CONFIG, getSchemaRegistryUrl());
        return configs;
    }

    private static KafkaProducer<HendelseKey, Hendelse> getKafkaProducer() {
        var producerConfig = new HashMap<>(configs);
        producerConfig.put(KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        producerConfig.put(VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        producerConfig.put(ACKS_CONFIG, "all");
        producerConfig.put(RETRIES_CONFIG, MAX_VALUE);
        return new KafkaProducer<>(producerConfig);
    }

    private static KafkaConsumer<HendelseKey, PensjonsgivendeInntekt> getKafkaConsumer() {
        var consumerConfigs = new HashMap<>(configs);
        consumerConfigs.put(KEY_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        consumerConfigs.put(VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        consumerConfigs.put(SPECIFIC_AVRO_READER_CONFIG, true);
        consumerConfigs.put(GROUP_ID_CONFIG, "loot-consumer-group");
        consumerConfigs.put(ENABLE_AUTO_COMMIT_CONFIG, false);
        consumerConfigs.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new KafkaConsumer<>(consumerConfigs);
    }

    private static HendelseKey getHendelseKey(Hendelse hendelse) {
        return HendelseKey.newBuilder()
                .setIdentifikator(hendelse.getIdentifikator())
                .setGjelderPeriode(hendelse.getGjelderPeriode()).build();
    }

    private static ProducerRecord<HendelseKey, Hendelse> createRecord(Hendelse hendelse) {
        return new ProducerRecord<>(RECORD_TOPIC, getHendelseKey(hendelse), hendelse);
    }

    static void populateHendelseTopic(List<Hendelse> hendelser) {
        var producer = getKafkaProducer();
        hendelser.stream()
                .map(KafkaTestEnvironment::createRecord)
                .forEach(producer::send);
    }

    static ConsumerRecords<HendelseKey, PensjonsgivendeInntekt> getConsumerRecords() {
        return pensjonsgivendeInntektConsumer.poll(Duration.ofSeconds(5L));
    }
}
