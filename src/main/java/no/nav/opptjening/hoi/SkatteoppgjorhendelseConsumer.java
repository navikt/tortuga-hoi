package no.nav.opptjening.hoi;

import io.prometheus.client.Counter;

import no.nav.opptjening.schema.skatt.hendelsesliste.Hendelse;
import no.nav.opptjening.skatt.client.BeregnetSkatt;
import no.nav.opptjening.skatt.client.Hendelsesliste;
import no.nav.opptjening.skatt.client.api.beregnetskatt.BeregnetSkattClient;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class SkatteoppgjorhendelseConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(SkatteoppgjorhendelseConsumer.class);

    private static final Counter antallHendelserRecieved = Counter.build()
            .name("beregnet_skatt_hendelser_received")
            .help("Antall hendelser hentet.").register();

    private static final Counter inntektsHendelserProcessed = Counter.build()
            .name("beregnet_skatt_hendelser_processed")
            .help("Antall hendelser prosessert").register();

    private final Consumer<String, Hendelse> hendelseConsumer;
    private final BeregnetSkattClient beregnetSkattClient;

    public SkatteoppgjorhendelseConsumer(Consumer<String, Hendelse> hendelseConsumer, BeregnetSkattClient beregnetSkattClient) {
        this.hendelseConsumer = hendelseConsumer;
        this.beregnetSkattClient = beregnetSkattClient;

        subscribeToTopic();
    }

    public void shutdown() {
        LOG.info("Shutting down SkatteoppgjorhendelseConsumer");
        hendelseConsumer.close();
    }

    private void subscribeToTopic() {
        hendelseConsumer.subscribe(Collections.singletonList(KafkaConfiguration.BEREGNET_SKATT_HENDELSE_TOPIC), new ConsumerRebalanceListener() {
            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                LOG.info("Partition revoked: {}", partitions);
            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                LOG.info("Partitions assigned: {}", partitions);
            }
        });
    }

    public List<BeregnetSkatt> poll() throws IOException {
        ConsumerRecords<String, Hendelse> hendelser = hendelseConsumer.poll(500);

        List<BeregnetSkatt> beregnetSkattList = new ArrayList<>();
        for (ConsumerRecord<String, Hendelse> record : hendelser) {
            antallHendelserRecieved.inc();
            Hendelse hendelse = record.value();

            LOG.info("HOI haandterer hendelse={}", hendelse);

            BeregnetSkatt beregnetSkatt = beregnetSkattClient.getBeregnetSkatt("nav", hendelse.getGjelderPeriode(), hendelse.getIdentifikator());
            beregnetSkattList.add(beregnetSkatt);
            inntektsHendelserProcessed.inc();
        }

        return beregnetSkattList;
    }

    public void commit() {
        hendelseConsumer.commitAsync();
    }
}
