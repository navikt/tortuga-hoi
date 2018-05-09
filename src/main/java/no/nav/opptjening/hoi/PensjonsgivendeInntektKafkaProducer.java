package no.nav.opptjening.hoi;

import io.prometheus.client.Counter;
import no.nav.opptjening.schema.PensjonsgivendeInntekt;
import no.nav.opptjening.skatt.schema.BeregnetSkatt;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
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

    public PensjonsgivendeInntektKafkaProducer(Producer<String, PensjonsgivendeInntekt> producer) {
        this.producer = producer;
    }

    public void send(List<BeregnetSkatt> beregnetSkattList) {
        for (BeregnetSkatt beregnetSkatt : beregnetSkattList) {
            PensjonsgivendeInntekt pensjonsgivendeInntekt = pensjonsgivendeInntektMapper.toPensjonsgivendeInntekt(beregnetSkatt);
            LOG.info("Mapper beregnet skatt='{}' til pensjonsgivende inntekt='{}'", beregnetSkatt, pensjonsgivendeInntekt);
            producer.send(new ProducerRecord<>(KafkaConfiguration.PENSJONSGIVENDE_INNTEKT_TOPIC, pensjonsgivendeInntekt));
        }
        producer.flush();
        producedCount.inc(beregnetSkattList.size());
    }

    public void close() {
        producer.close();
    }
}
