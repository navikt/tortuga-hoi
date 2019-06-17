package no.nav.opptjening.skatt.client.schema;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Optional;

@JsonDeserialize(builder = BeregnetSkattDto.Builder.class)
public final class BeregnetSkattDto {

    private final String personidentifikator;
    private final String inntektsaar;
    private final Long personinntektLoenn;
    private final Long personinntektFiskeFangstFamiliebarnehage;
    private final Long personinntektNaering;
    private final Long personinntektBarePensjonsdel;
    private final Long svalbardLoennLoennstrekkordningen;
    private final Long svalbardPersoninntektNaering;
    private final boolean skjermet;

    public BeregnetSkattDto(String personidentifikator, String inntektsaar,
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

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private String personidentifikator;
        private String inntektsaar;
        private Long personinntektLoenn;
        private Long personinntektFiskeFangstFamiliebarnehage;
        private Long personinntektNaering;
        private Long personinntektBarePensjonsdel;
        private Long svalbardLoennLoennstrekkordningen;
        private Long svalbardPersoninntektNaering;
        private boolean skjermet;

        private Builder() {
        }

        public Builder withPersonidentifikator(String personidentifikator) {
            this.personidentifikator = personidentifikator;
            return this;
        }

        public Builder withInntektsaar(String inntektsaar) {
            this.inntektsaar = inntektsaar;
            return this;
        }

        public Builder withPersoninntektLoenn(Long personinntektLoenn) {
            this.personinntektLoenn = personinntektLoenn;
            return this;
        }

        public Builder withPersoninntektFiskeFangstFamiliebarnehage(Long personinntektFiskeFangstFamiliebarnehage) {
            this.personinntektFiskeFangstFamiliebarnehage = personinntektFiskeFangstFamiliebarnehage;
            return this;
        }

        public Builder withPersoninntektNaering(Long personinntektNaering) {
            this.personinntektNaering = personinntektNaering;
            return this;
        }

        public Builder withPersoninntektBarePensjonsdel(Long personinntektBarePensjonsdel) {
            this.personinntektBarePensjonsdel = personinntektBarePensjonsdel;
            return this;
        }

        public Builder withSvalbardLoennLoennstrekkordningen(Long svalbardLoennLoennstrekkordningen) {
            this.svalbardLoennLoennstrekkordningen = svalbardLoennLoennstrekkordningen;
            return this;
        }

        public Builder withSvalbardPersoninntektNaering(Long svalbardPersoninntektNaering) {
            this.svalbardPersoninntektNaering = svalbardPersoninntektNaering;
            return this;
        }

        public Builder withSkjermet(boolean skjermet) {
            this.skjermet = skjermet;
            return this;
        }

        public BeregnetSkattDto build() {
            return new BeregnetSkattDto(personidentifikator, inntektsaar, personinntektLoenn, personinntektFiskeFangstFamiliebarnehage, personinntektNaering, personinntektBarePensjonsdel, svalbardLoennLoennstrekkordningen, svalbardPersoninntektNaering, skjermet);
        }
    }
}
