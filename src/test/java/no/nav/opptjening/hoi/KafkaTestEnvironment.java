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
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.*;

import static no.nav.opptjening.hoi.KafkaConfiguration.PENSJONSGIVENDE_INNTEKT_TOPIC;
import static no.nav.opptjening.hoi.KafkaConfiguration.SKATTEOPPGJØRHENDELSE_TOPIC;

class KafkaTestEnvironment {
    private static final String KAFKA_USERNAME = "srvTest";
    private static final String KAFKA_PASSWORD = "opensourcedPassword";
    private static final int NUMBER_OF_BROKERS = 3;
    private static final List<String> TOPICS = Arrays.asList(PENSJONSGIVENDE_INNTEKT_TOPIC, SKATTEOPPGJØRHENDELSE_TOPIC);

    private static KafkaEnvironment kafkaEnvironment;

    static void setup(){
        kafkaEnvironment = new KafkaEnvironment(NUMBER_OF_BROKERS, TOPICS, Collections.emptyList(), true, false, Collections.emptyList(), false, new Properties());
        kafkaEnvironment.start();
    }

    static void tearDown(){
        kafkaEnvironment.tearDown();
    }

    private static String getBrokersURL(){
        return kafkaEnvironment.getBrokersURL();
    }

    private static String getSchemaRegistryUrl(){
        return Objects.requireNonNull(kafkaEnvironment.getSchemaRegistry()).getUrl();
    }

    static KafkaConfiguration getKafkaConfiguration(){
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

    private static List<Hendelse> getHendelser(){
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
        return hendelser;
    }

    private static Map<String, Object> getRecordConfig(){
        Map<String, Object> configs = new HashMap<>();
        configs.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, getBrokersURL());
        configs.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, getSchemaRegistryUrl());
        return configs;
    }

    private static KafkaProducer<HendelseKey, Hendelse> getKafkaProducer(Map<String, Object> configs){
        Map<String, Object> producerConfig = new HashMap<>(configs);
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        producerConfig.put(ProducerConfig.ACKS_CONFIG, "all");
        producerConfig.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        return new KafkaProducer<>(producerConfig);

    }

    private static KafkaConsumer<HendelseKey, PensjonsgivendeInntekt> getKafkaConsumer(Map<String, Object> configs){
        Map<String, Object> consumerConfigs = new HashMap<>(configs);
        consumerConfigs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        consumerConfigs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        consumerConfigs.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        consumerConfigs.put(ConsumerConfig.GROUP_ID_CONFIG, "loot-consumer-group");
        consumerConfigs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        consumerConfigs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new KafkaConsumer<>(consumerConfigs);
    }

    static Consumer<HendelseKey, PensjonsgivendeInntekt> createRecords() {
        Map<String, Object> configs = getRecordConfig();
        Producer<HendelseKey, Hendelse> producer = getKafkaProducer(configs);
        Consumer<HendelseKey, PensjonsgivendeInntekt> pensjonsgivendeInntektConsumer = getKafkaConsumer(configs);

        final String topic = "privat-tortuga-skatteoppgjorhendelse";
        for (Hendelse hendelse : getHendelser()) {
            producer.send(new ProducerRecord<>(topic, HendelseKey.newBuilder()
                    .setIdentifikator(hendelse.getIdentifikator())
                    .setGjelderPeriode(hendelse.getGjelderPeriode()).build(), hendelse));
        }
        producer.flush();
        return pensjonsgivendeInntektConsumer;
    }
}
