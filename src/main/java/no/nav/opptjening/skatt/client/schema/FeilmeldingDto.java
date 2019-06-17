package no.nav.opptjening.skatt.client.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class FeilmeldingDto {
    private final String kode;
    private final String melding;
    private final String korrelasjonsId;

    @JsonCreator
    public FeilmeldingDto(@JsonProperty("kode") String kode, @JsonProperty("melding") String melding, @JsonProperty("korrelasjonsid") String korrelasjonsId) {
        this.kode = kode;
        this.melding = melding;
        this.korrelasjonsId = korrelasjonsId;
    }

    public String getKode() {
        return kode;
    }

    public String getMelding() {
        return melding;
    }

    public String getKorrelasjonsId() {
        return korrelasjonsId;
    }

    @Override
    public String toString() {
        return "Feilmelding{" +
                "kode='" + kode + '\'' +
                ", melding='" + melding + '\'' +
                ", korrelasjonsId='" + korrelasjonsId + '\'' +
                '}';
    }
}
