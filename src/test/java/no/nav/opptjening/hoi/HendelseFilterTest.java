package no.nav.opptjening.hoi;

import no.nav.opptjening.schema.skatt.hendelsesliste.Hendelse;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HendelseFilterTest {

    private HendelseFilter hendelseFilter;

    @Before
    public void setUp() {
        hendelseFilter = new HendelseFilter();
    }

    @Test
    public void testReturnsFalseWhenHendelserIsFromInvalidYear() {
        Hendelse hendelseFromInvalidYear = new Hendelse(1L, "123456789", "2015");
        Hendelse hendelseFromInvalidYear2 = new Hendelse(2L, "234567890", "2016");
        assertFalse(HendelseFilter.testThatHendelseIsFromValidYear("key1", hendelseFromInvalidYear));
        assertFalse(HendelseFilter.testThatHendelseIsFromValidYear("key2", hendelseFromInvalidYear2));
    }

    @Test
    public void testReturnsTrueWhenHendelserIsFromValidYear() {
        Hendelse hendelseFromValidYear = new Hendelse(1L, "123456789", "2017");
        Hendelse hendelseFromValidYear2 = new Hendelse(2L, "234567890", "2018");
        assertTrue(HendelseFilter.testThatHendelseIsFromValidYear("key1", hendelseFromValidYear));
        assertTrue(HendelseFilter.testThatHendelseIsFromValidYear("key2", hendelseFromValidYear2));
    }
}