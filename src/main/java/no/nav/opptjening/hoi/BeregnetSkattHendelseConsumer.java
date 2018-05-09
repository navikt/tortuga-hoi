package no.nav.opptjening.hoi;

import io.prometheus.client.Counter;
import no.nav.opptjening.skatt.api.beregnetskatt.BeregnetSkattClient;
import no.nav.opptjening.skatt.exceptions.HttpException;
import no.nav.opptjening.skatt.schema.BeregnetSkatt;
import no.nav.opptjening.skatt.schema.hendelsesliste.Hendelse;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class BeregnetSkattHendelseConsumer implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(BeregnetSkattHendelseConsumer.class);

    private static final Counter antallHendelserRecieved = Counter.build()
            .name("beregnet_skatt_hendelser_received")
            .help("Antall hendelser hentet.").register();

    private static final Counter inntektsHendelserProcessed = Counter.build()
            .name("beregnet_skatt_hendelser_processed")
            .help("Antall hendelser prosessert").register();

    private final Consumer<String, Hendelse> hendelseConsumer;
    private final PensjonsgivendeInntektKafkaProducer inntektProducer;
    private BeregnetSkattClient beregnetSkattClient;

    public BeregnetSkattHendelseConsumer(BeregnetSkattClient beregnetSkattClient, PensjonsgivendeInntektKafkaProducer inntektProducer, Consumer<String, Hendelse> consumer) {
        this.beregnetSkattClient = beregnetSkattClient;
        this.hendelseConsumer = consumer;
        this.inntektProducer = inntektProducer;

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

    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
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

                inntektProducer.send(beregnetSkattList);
                hendelseConsumer.commitAsync();
            }
        } catch (HttpException e) {
            LOG.error("Error while contacting Skatteetaten", e);
        } catch (Exception e) {
            LOG.error("Error during processing of Hendelse/Inntekt", e);
        } finally {
            hendelseConsumer.close();
            inntektProducer.close();
        }

        LOG.info("BeregnetSkattHendelseConsumer task stopped");
    }
}
