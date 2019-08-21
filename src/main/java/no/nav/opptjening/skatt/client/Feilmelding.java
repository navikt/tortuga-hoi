package no.nav.opptjening.skatt.client;

import java.util.Objects;

public final class Feilmelding {
    private final String kode;
    private final String melding;
    private final String korrelasjonsId;

    public Feilmelding(String kode, String melding, String korrelasjonsId) {
        this.kode = kode;
        this.melding = melding;
        this.korrelasjonsId = korrelasjonsId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Feilmelding that = (Feilmelding) o;
        return Objects.equals(kode, that.kode) &&
                Objects.equals(melding, that.melding) &&
                Objects.equals(korrelasjonsId, that.korrelasjonsId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kode, melding, korrelasjonsId);
    }

    String getKode() {
        return kode;
    }

    String getMelding() {
        return melding;
    }

    String getKorrelasjonsId() {
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
