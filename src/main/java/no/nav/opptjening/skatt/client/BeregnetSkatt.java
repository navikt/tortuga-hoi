package no.nav.opptjening.skatt.client;


import java.util.Objects;
import java.util.Optional;

public final class BeregnetSkatt {

    private final String personidentifikator;
    private final String inntektsaar;
    private final Long personinntektLoenn;
    private final Long personinntektFiskeFangstFamiliebarnehage;
    private final Long personinntektNaering;
    private final Long personinntektBarePensjonsdel;
    private final Long svalbardLoennLoennstrekkordningen;
    private final Long svalbardPersoninntektNaering;
    private final boolean skjermet;

    public BeregnetSkatt(String personidentifikator, String inntektsaar,
                         Long personinntektLoenn, Long personinntektFiskeFangstFamiliebarnehage,
                         Long personinntektNaering, Long personinntektBarePensjonsdel,
                         Long svalbardLoennLoennstrekkordningen, Long svalbardPersoninntektNaering,
                         boolean skjermet) {
        this.personidentifikator = personidentifikator;
        this.inntektsaar = inntektsaar;
        this.personinntektLoenn = personinntektLoenn;
        this.personinntektFiskeFangstFamiliebarnehage = personinntektFiskeFangstFamiliebarnehage;
        this.personinntektNaering = personinntektNaering;
        this.personinntektBarePensjonsdel = personinntektBarePensjonsdel;
        this.svalbardLoennLoennstrekkordningen = svalbardLoennLoennstrekkordningen;
        this.svalbardPersoninntektNaering = svalbardPersoninntektNaering;
        this.skjermet = skjermet;
    }

    public String getPersonidentifikator() {
        return personidentifikator;
    }

    public String getInntektsaar() {
        return inntektsaar;
    }

    public Optional<Long> getPersoninntektLoenn() {
        return Optional.ofNullable(personinntektLoenn);
    }

    public Optional<Long> getPersoninntektFiskeFangstFamiliebarnehage() {
        return Optional.ofNullable(personinntektFiskeFangstFamiliebarnehage);
    }

    public Optional<Long> getPersoninntektNaering() {
        return Optional.ofNullable(personinntektNaering);
    }

    public Optional<Long> getPersoninntektBarePensjonsdel() {
        return Optional.ofNullable(personinntektBarePensjonsdel);
    }

    public Optional<Long> getSvalbardLoennLoennstrekkordningen() {
        return Optional.ofNullable(svalbardLoennLoennstrekkordningen);
    }

    public Optional<Long> getSvalbardPersoninntektNaering() {
        return Optional.ofNullable(svalbardPersoninntektNaering);
    }

    public boolean isSkjermet() {
        return skjermet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BeregnetSkatt that = (BeregnetSkatt) o;
        return skjermet == that.skjermet &&
                Objects.equals(personidentifikator, that.personidentifikator) &&
                Objects.equals(inntektsaar, that.inntektsaar) &&
                Objects.equals(personinntektLoenn, that.personinntektLoenn) &&
                Objects.equals(personinntektFiskeFangstFamiliebarnehage, that.personinntektFiskeFangstFamiliebarnehage) &&
                Objects.equals(personinntektNaering, that.personinntektNaering) &&
                Objects.equals(personinntektBarePensjonsdel, that.personinntektBarePensjonsdel) &&
                Objects.equals(svalbardLoennLoennstrekkordningen, that.svalbardLoennLoennstrekkordningen) &&
                Objects.equals(svalbardPersoninntektNaering, that.svalbardPersoninntektNaering);
    }

    @Override
    public int hashCode() {
        return Objects.hash(personidentifikator, inntektsaar, personinntektLoenn, personinntektFiskeFangstFamiliebarnehage, personinntektNaering, personinntektBarePensjonsdel, svalbardLoennLoennstrekkordningen, svalbardPersoninntektNaering, skjermet);
    }

    @Override
    public String toString() {
        return "BeregnetSkatt{" +
                "personidentifikator='" + personidentifikator + '\'' +
                ", inntektsaar='" + inntektsaar + '\'' +
                ", personinntektLoenn=" + personinntektLoenn +
                ", personinntektFiskeFangstFamiliebarnehage=" + personinntektFiskeFangstFamiliebarnehage +
                ", personinntektNaering=" + personinntektNaering +
                ", personinntektBarePensjonsdel=" + personinntektBarePensjonsdel +
                ", svalbardLoennLoennstrekkordningen=" + svalbardLoennLoennstrekkordningen +
                ", svalbardPersoninntektNaering=" + svalbardPersoninntektNaering +
                ", skjermet=" + skjermet +
                '}';
    }

    public BeregnetSkatt withSvalbardLoenn(Long svalbardLoenn) {
        return new BeregnetSkatt(
                this.getPersonidentifikator(),
                this.getInntektsaar(),
                this.getPersoninntektLoenn().orElse(null),
                this.getPersoninntektFiskeFangstFamiliebarnehage().orElse(null),
                this.getPersoninntektNaering().orElse(null),
                this.getPersoninntektBarePensjonsdel().orElse(null),
                svalbardLoenn,
                this.getSvalbardPersoninntektNaering().orElse(null),
                this.isSkjermet()
        );
    }
}
