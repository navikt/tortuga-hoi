package no.nav.opptjening.hoi;

import no.nav.opptjening.schema.skatt.hendelsesliste.Hendelse;
import no.nav.opptjening.skatt.client.api.beregnetskatt.BeregnetSkattClient;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class PensjonsgivendeInntektTask implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(PensjonsgivendeInntektTask.class);

    private final BeregnetSkattClient beregnetSkattClient;
    private final Properties props;
    private final KafkaStreams streams;

    public PensjonsgivendeInntektTask(@NotNull BeregnetSkattClient beregnetSkattClient, @NotNull Properties props) {
        this.beregnetSkattClient = beregnetSkattClient;
        this.props = props;

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Hendelse> stream = builder.stream(KafkaConfiguration.BEREGNET_SKATT_HENDELSE_TOPIC);
        stream.filter(new HendelseFilter())
                .transformValues(() -> new BeregnetSkattMapper(beregnetSkattClient))
                .mapValues(new PensjonsgivendeInntektMapper())
                .to(KafkaConfiguration.PENSJONSGIVENDE_INNTEKT_TOPIC);

        streams = new KafkaStreams(builder.build(), props);
        streams.setUncaughtExceptionHandler((t, e) -> LOG.error("Uncaught exception in thread {}", t, e));
    }

    public void run() {
        streams.start();

        try {
            while (!Thread.currentThread().isInterrupted()) {
                Thread.sleep(10000);
            }
        } catch (InterruptedException e) {
            LOG.info("PensjonsgivendeInntektTask interrupted during sleep, exiting");
        }
        LOG.info("PensjonsgivendeInntektTask stopped");
    }

    public void shutdown() {
        streams.close();
    }
}
