package no.nav.opptjening.skatt.client;

import no.nav.opptjening.skatt.client.schema.FeilmeldingDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class FeilmeldingMapperTest {
    private final FeilmeldingMapper feilmeldingMapper = new FeilmeldingMapper();

    @Test
    void that_FeilmeldingDto_is_Mapped() {
        FeilmeldingDto feilmeldingDto = new FeilmeldingDto("DAS-001", "Det var en uventet feil på tjenesten. Vennligst ta kontakt med brukerstøtte, med applikasjon og korrelasjonsid fra denne meldingen!", "foobar");
        Feilmelding feilmelding = feilmeldingMapper.mapToFeilmelding(feilmeldingDto);

        assertEquals("DAS-001", feilmelding.getKode());
        assertEquals("Det var en uventet feil på tjenesten. Vennligst ta kontakt med brukerstøtte, med applikasjon og korrelasjonsid fra denne meldingen!", feilmelding.getMelding());
        assertEquals("foobar", feilmelding.getKorrelasjonsId());
    }

    @Test
    void that_FeilmeldingDto_is_Mapped_When_korrelasjonsid_Is_Null() {
        FeilmeldingDto feilmeldingDto = new FeilmeldingDto("DAS-001", "Det var en uventet feil på tjenesten. Vennligst ta kontakt med brukerstøtte, med applikasjon og korrelasjonsid fra denne meldingen!", null);
        Feilmelding feilmelding = feilmeldingMapper.mapToFeilmelding(feilmeldingDto);

        assertEquals("DAS-001", feilmelding.getKode());
        assertEquals("Det var en uventet feil på tjenesten. Vennligst ta kontakt med brukerstøtte, med applikasjon og korrelasjonsid fra denne meldingen!", feilmelding.getMelding());
        assertEquals("", feilmelding.getKorrelasjonsId());
    }

    @Test
    void that_NPE_When_kode_Is_Null() {
        FeilmeldingDto feilmeldingDto = new FeilmeldingDto(null, "Det var en uventet feil på tjenesten. Vennligst ta kontakt med brukerstøtte, med applikasjon og korrelasjonsid fra denne meldingen!", "foobar");
        assertThrows(NullPointerException.class, ()->feilmeldingMapper.mapToFeilmelding(feilmeldingDto));

    }

    @Test
    void that_NPE_When_melding_Is_Null() {
        FeilmeldingDto feilmeldingDto = new FeilmeldingDto("DAS-001", null, "foobar");
        assertThrows(NullPointerException.class, ()->feilmeldingMapper.mapToFeilmelding(feilmeldingDto));
    }
}
