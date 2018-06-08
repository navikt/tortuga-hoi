package no.nav.opptjening.hoi;

import no.nav.opptjening.schema.skatt.hendelsesliste.Hendelse;
import org.apache.kafka.streams.kstream.Predicate;

public class HendelseFilter implements Predicate<String, Hendelse> {

    private final int EARLIEST_VALID_HENDELSE_YEAR = 2017;

    @Override
    public boolean test(String key, Hendelse hendelse) {
        return Integer.parseInt(hendelse.getGjelderPeriode()) >= EARLIEST_VALID_HENDELSE_YEAR;
    }
}
