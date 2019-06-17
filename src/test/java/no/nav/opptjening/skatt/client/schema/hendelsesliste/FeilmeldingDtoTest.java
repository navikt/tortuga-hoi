package no.nav.opptjening.skatt.client.schema.hendelsesliste;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.opptjening.skatt.client.schema.FeilmeldingDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FeilmeldingDtoTest {

    @Test
    void that_Mapping_Works() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "{\"kode\": \"FA-001\", \"melding\": \"fraSekvensnummer må være satt\", \"korrelasjonsid\": \"52e3ce7eb8df80fa6a135dc6eae475f6\"}";

        FeilmeldingDto feilmelding = mapper.readValue(jsonString, FeilmeldingDto.class);
        Assertions.assertEquals("FA-001", feilmelding.getKode());
        Assertions.assertEquals("fraSekvensnummer må være satt", feilmelding.getMelding());
        Assertions.assertEquals("52e3ce7eb8df80fa6a135dc6eae475f6", feilmelding.getKorrelasjonsId());
    }

    @Test
    void that_Mapping_Works_Without_korrelasjonsid() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "{\"kode\": \"FA-001\", \"melding\": \"fraSekvensnummer må være satt\"}";

        FeilmeldingDto feilmelding = mapper.readValue(jsonString, FeilmeldingDto.class);
        Assertions.assertEquals("FA-001", feilmelding.getKode());
        Assertions.assertEquals("fraSekvensnummer må være satt", feilmelding.getMelding());
        Assertions.assertNull(feilmelding.getKorrelasjonsId());
    }
}
