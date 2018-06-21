package no.nav.opptjening.hoi;

import no.nav.opptjening.schema.skatt.hendelsesliste.Hendelse;

public class HendelseFilter  {

    private static final int EARLIEST_VALID_HENDELSE_YEAR = 2017;

    public static boolean testThatHendelseIsFromValidYear(String key, Hendelse hendelse) {
        return Integer.parseInt(hendelse.getGjelderPeriode()) >= EARLIEST_VALID_HENDELSE_YEAR;
    }
}
