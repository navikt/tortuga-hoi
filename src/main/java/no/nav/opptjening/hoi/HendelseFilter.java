package no.nav.opptjening.hoi;

import io.prometheus.client.Counter;
import no.nav.opptjening.schema.skatt.hendelsesliste.Hendelse;

public class HendelseFilter  {

    private static final Counter inntektsHendelserRecievedTotal = Counter.build()
            .name("beregnet_skatt_hendelser_mottatt_totalt")
            .help("Antall hendelser mottatt").register();

    private static final Counter inntektsHendelserRecieved = Counter.build()
            .name("beregnet_skatt_hendelser_mottatt")
            .labelNames("year")
            .help("Antall hendelser mottatt").register();

    private static final int EARLIEST_VALID_HENDELSE_YEAR = 2017;

    public static boolean testThatHendelseIsFromValidYear(String key, Hendelse hendelse) {
        inntektsHendelserRecievedTotal.inc();
        inntektsHendelserRecieved.labels(hendelse.getGjelderPeriode()).inc();
        return Integer.parseInt(hendelse.getGjelderPeriode()) >= EARLIEST_VALID_HENDELSE_YEAR;
    }
}
