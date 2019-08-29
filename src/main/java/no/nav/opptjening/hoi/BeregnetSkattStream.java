package no.nav.opptjening.hoi;

import no.nav.opptjening.schema.skatt.hendelsesliste.Hendelse;
import no.nav.opptjening.schema.skatt.hendelsesliste.HendelseKey;
import no.nav.opptjening.skatt.client.api.beregnetskatt.BeregnetSkattClient;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BeregnetSkattStream {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);
    private static final StreamsBuilder streamBuilder = new StreamsBuilder();
    private KafkaStreams beregnetSkattStream;

    BeregnetSkattStream(BeregnetSkattClient beregnetSkattClient, HendelseFilter hendelseFilter, KafkaConfiguration kafkaConfiguration) {
        KStream<HendelseKey, Hendelse> stream = streamBuilder.stream(KafkaConfiguration.SKATTEOPPGJØRHENDELSE_TOPIC);
        stream.filter(hendelseFilter::testThatHendelseIsFromValidYear)
                .mapValues(new BeregnetSkattMapper(beregnetSkattClient))
                .mapValues(new PensjonsgivendeInntektMapper())
                .to(KafkaConfiguration.PENSJONSGIVENDE_INNTEKT_TOPIC);
        this.beregnetSkattStream = new KafkaStreams(streamBuilder.build(), kafkaConfiguration.streamsConfiguration());

        setStreamUncaughtExceptionHandler();
        setStreamStateListener();
    }

    private void setStreamUncaughtExceptionHandler() {
        beregnetSkattStream.setUncaughtExceptionHandler((t, e) -> {
            LOG.error("Uncaught exception in thread {}, closing beregnetSkattStream", t, e);
            beregnetSkattStream.close();
        });
    }

    private void setStreamStateListener() {
        beregnetSkattStream.setStateListener((newState, oldState) ->
                LOG.debug("State change from {} to {}", oldState, newState));
    }

    void start() {
        beregnetSkattStream.start();
    }

    void close() {
        beregnetSkattStream.close();
    }

    boolean isRunning() {
        return beregnetSkattStream.state().isRunning();
    }
}