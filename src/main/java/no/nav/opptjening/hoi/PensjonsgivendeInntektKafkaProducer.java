package no.nav.opptjening.hoi;

import io.prometheus.client.Counter;
import no.nav.opptjening.schema.PensjonsgivendeInntekt;

import no.nav.opptjening.skatt.client.BeregnetSkatt;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PensjonsgivendeInntektKafkaProducer {

    private static final Logger LOG = LoggerFactory.getLogger(PensjonsgivendeInntektKafkaProducer.class);

    private PensjonsgivendeInntektMapper pensjonsgivendeInntektMapper = new PensjonsgivendeInntektMapper();

    private final Producer<String, PensjonsgivendeInntekt> producer;

    private static final Counter producedCount = Counter.build()
            .name("pensjonsgivende_inntekter_produced")
            .help("Antall inntekter bekreftet sendt til Kafka.").register();
    private final BeregnetSkattMapper beregnetSkattMapper = new BeregnetSkattMapper();

    public PensjonsgivendeInntektKafkaProducer(@NotNull Producer<String, PensjonsgivendeInntekt> producer) {
        this.producer = producer;
    }

    public void send(@NotNull List<BeregnetSkatt> beregnetSkattList) {
        for (BeregnetSkatt beregnetSkatt : beregnetSkattList) {
            PensjonsgivendeInntekt pensjonsgivendeInntekt = pensjonsgivendeInntektMapper.toPensjonsgivendeInntekt(beregnetSkattMapper.mapToBeregnetSkatt(beregnetSkatt));
            LOG.info("Mapper beregnet skatt='{}' til pensjonsgivende inntekt='{}'", beregnetSkatt, pensjonsgivendeInntekt);
            producer.send(new ProducerRecord<>(KafkaConfiguration.PENSJONSGIVENDE_INNTEKT_TOPIC, pensjonsgivendeInntekt));
        }
        producer.flush();
        producedCount.inc(beregnetSkattList.size());
    }

    public void shutdown() {
        producer.close();
    }
}
