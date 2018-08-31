package no.nav.opptjening.hoi;

import io.prometheus.client.Counter;
import no.nav.opptjening.schema.skatt.hendelsesliste.Hendelse;
import no.nav.opptjening.skatt.client.BeregnetSkatt;
import no.nav.opptjening.skatt.client.api.beregnetskatt.BeregnetSkattClient;
import no.nav.opptjening.skatt.client.api.beregnetskatt.exceptions.FantIkkeBeregnetSkattException;
import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BeregnetSkattMapper implements ValueTransformer<Hendelse, BeregnetSkatt> {
    private static final Logger LOG = LoggerFactory.getLogger(BeregnetSkattMapper.class);
    private final BeregnetSkattClient beregnetSkattClient;

    private static final Counter inntektsHendelserProcessed = Counter.build()
            .name("beregnet_skatt_hendelser_processed")
            .help("Antall hendelser prosessert").register();


    public BeregnetSkattMapper(@NotNull BeregnetSkattClient beregnetSkattClient) {
        this.beregnetSkattClient = beregnetSkattClient;
    }

    @Override
    public BeregnetSkatt transform(Hendelse hendelse) {
        LOG.trace("HOI haandterer hendelse={}", hendelse);
        inntektsHendelserProcessed.inc();

        try {
            return beregnetSkattClient.getBeregnetSkatt("nav", hendelse.getGjelderPeriode(), hendelse.getIdentifikator());
        } catch (FantIkkeBeregnetSkattException e) {
            LOG.info("Fant ikke beregnet skatt, returnerer null", e);
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void init(ProcessorContext processorContext) { }

    @Override
    public BeregnetSkatt punctuate(long l) { return null; }

    @Override
    public void close() { }
}
