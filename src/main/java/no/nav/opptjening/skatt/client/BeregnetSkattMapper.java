package no.nav.opptjening.skatt.client;

import no.nav.opptjening.skatt.client.schema.BeregnetSkattDto;

public class BeregnetSkattMapper {

    public BeregnetSkatt mapToBeregnetSkatt(BeregnetSkattDto beregnetSkattDto) {
        String personidentifikator = beregnetSkattDto.getPersonidentifikator();
        String inntektsaar = beregnetSkattDto.getInntektsaar();
        if (personidentifikator == null) {
            throw new NullPointerException("Personidentifikator is null");
        }
        if (inntektsaar == null) {
            throw new NullPointerException("Inntektsaar is null");
        }
        return new BeregnetSkatt(personidentifikator, inntektsaar,
                calculatePersoninntektLoenn(beregnetSkattDto),
                beregnetSkattDto.getPersoninntektFiskeFangstFamiliebarnehage().orElse(null),
                beregnetSkattDto.getPersoninntektNaering().orElse(null),
                beregnetSkattDto.getPersoninntektBarePensjonsdel().orElse(null),
                beregnetSkattDto.getSvalbardLoennLoennstrekkordningen().orElse(null),
                beregnetSkattDto.getSvalbardPersoninntektNaering().orElse(null),
                beregnetSkattDto.isSkjermet());
    }

    private Long calculatePersoninntektLoenn(BeregnetSkattDto beregnetSkattDto) {
        Long personinntektLoenn = beregnetSkattDto.getPersoninntektLoenn().orElse(null);
        Long kildeskattPaaLoennPersoninntektLoenn = beregnetSkattDto.getKildeskattPaaLoennPersoninntektLoenn().orElse(null);
        Long kildeskattPaaLoennPersoninntektBarePensjonsdel = beregnetSkattDto.getKildeskattPaaLoennPersoninntektBarePensjonsdel().orElse(null);
        Long totalKildeskatt = sumNullable(kildeskattPaaLoennPersoninntektLoenn, kildeskattPaaLoennPersoninntektBarePensjonsdel);
        return sumNullable(personinntektLoenn, totalKildeskatt);
    }

    private Long sumNullable(Long a, Long b) {
        if (a != null) {
            if (b != null) return a + b;
            else return a;
        } else return b;
    }
}
