package no.nav.opptjening.skatt.client.api;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import no.nav.opptjening.skatt.client.SvalbardLoennsInntekt;

import java.util.List;
import java.util.Optional;

public class SvalbardLonnExtractor {
    private static final String SVALBARDLONN_ATTRIBUTE_NAME = "loennsinntektMedTrygdeavgiftspliktOmfattetAvLoennstrekkordningen";
    private static final String JSONPATH_SVALBARDLONN = "$.svalbardGrunnlag[?(@.tekniskNavn == \"" + SVALBARDLONN_ATTRIBUTE_NAME + "\")].beloep";

    private static final String JSONPATH_SKJERMET = "$.skjermet";

    private SvalbardLonnExtractor() {
    }

    public static SvalbardLoennsInntekt fetchLoennsinntektMedTrygdeavgiftspliktOmfattetAvLoennstrekkordningen(String jsonDocument) {
        var configuration = Configuration.builder().options(Option.SUPPRESS_EXCEPTIONS).build();
        var documentContext = JsonPath.using(configuration).parse(jsonDocument);
        return new SvalbardLoennsInntekt(fetchSvalbardLoenn(documentContext).orElse(null), documentContext.read(JSONPATH_SKJERMET));
    }

    private static Optional<Long> fetchSvalbardLoenn(DocumentContext documentContext) {
        List<Integer> beloep = documentContext.read(JSONPATH_SVALBARDLONN);
        return beloep.isEmpty() ? Optional.empty() : Optional.of(beloep.get(0).longValue());
    }
}
