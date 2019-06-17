package no.nav.opptjening.hoi;

import io.prometheus.client.Counter;
import no.nav.opptjening.schema.skatt.hendelsesliste.Hendelse;
import no.nav.opptjening.schema.skatt.hendelsesliste.HendelseKey;
import no.nav.opptjening.skatt.client.BeregnetSkatt;
import no.nav.opptjening.skatt.client.api.beregnetskatt.BeregnetSkattClient;
import no.nav.opptjening.skatt.client.api.beregnetskatt.FantIkkeBeregnetSkattException;
import org.apache.kafka.streams.kstream.ValueMapperWithKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class BeregnetSkattMapper implements ValueMapperWithKey<HendelseKey, Hendelse, BeregnetSkatt> {
    private static final Logger LOG = LoggerFactory.getLogger(BeregnetSkattMapper.class);
    private final BeregnetSkattClient beregnetSkattClient;

    private static final Counter inntektsHendelserProcessedTotal = Counter.build()
            .name("beregnet_skatt_hendelser_processed_total")
            .help("Antall hendelser prosessert").register();
    private static final Counter inntektsHendelserProcessed = Counter.build()
            .name("beregnet_skatt_hendelser_processed")
            .labelNames("year")
            .help("Antall hendelser prosessert").register();


    public BeregnetSkattMapper(BeregnetSkattClient beregnetSkattClient) {
        this.beregnetSkattClient = beregnetSkattClient;
    }

    @Override
    public BeregnetSkatt apply(HendelseKey key, Hendelse hendelse) {
        LOG.trace("HOI haandterer hendelse={}", hendelse);
        inntektsHendelserProcessedTotal.inc();
        inntektsHendelserProcessed.labels(key.getGjelderPeriode()).inc();

        try {
            return beregnetSkattClient.getBeregnetSkatt("nav", key.getGjelderPeriode(), key.getIdentifikator());
        } catch (FantIkkeBeregnetSkattException e) {
            LOG.info("Fant ikke beregnet skatt, returnerer null", e);
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
