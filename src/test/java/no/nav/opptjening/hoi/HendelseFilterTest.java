package no.nav.opptjening.hoi;

import no.nav.opptjening.schema.skatt.hendelsesliste.Hendelse;
import no.nav.opptjening.schema.skatt.hendelsesliste.HendelseKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class HendelseFilterTest {

    private HendelseFilter hendelseFilter;
    private static final String EARLIEST_VALID_HENDELSE_YEAR = "2017";

    @BeforeEach
    void setUp() {
        hendelseFilter = new HendelseFilter(EARLIEST_VALID_HENDELSE_YEAR);
    }

    @Test
    void testReturnsFalseWhenHendelserIsFromInvalidYear() {
        Hendelse hendelseFromInvalidYear = new Hendelse(1L, "123456789", "2015");
        Hendelse hendelseFromInvalidYear2 = new Hendelse(2L, "234567890", "2016");
        assertFalse(hendelseFilter.testThatHendelseIsFromValidYear(HendelseKey.newBuilder()
                .setIdentifikator("123456789")
                .setGjelderPeriode("2015")
                .build(), hendelseFromInvalidYear));
        assertFalse(hendelseFilter.testThatHendelseIsFromValidYear(HendelseKey.newBuilder()
                .setIdentifikator("234567890")
                .setGjelderPeriode("2016")
                .build(), hendelseFromInvalidYear2));
    }

    @Test
    void testReturnsTrueWhenHendelserIsFromValidYear() {
        Hendelse hendelseFromValidYear = new Hendelse(1L, "123456789", "2017");
        Hendelse hendelseFromValidYear2 = new Hendelse(2L, "234567890", "2018");
        assertTrue(hendelseFilter.testThatHendelseIsFromValidYear(HendelseKey.newBuilder()
                .setIdentifikator("123456789")
                .setGjelderPeriode("2017")
                .build(), hendelseFromValidYear));
        assertTrue(hendelseFilter.testThatHendelseIsFromValidYear(HendelseKey.newBuilder()
                .setIdentifikator("234567890")
                .setGjelderPeriode("2018")
                .build(), hendelseFromValidYear2));
    }
}
