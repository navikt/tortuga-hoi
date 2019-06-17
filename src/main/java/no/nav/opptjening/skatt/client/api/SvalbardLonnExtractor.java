package no.nav.opptjening.skatt.client.api;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

import java.util.List;
import java.util.Optional;

public class SvalbardLonnExtractor {
    private SvalbardLonnExtractor() {
    }

    private static final String SVALBARDLONN_ATTRIBUTE_NAME = "loennsinntektMedTrygdeavgiftspliktOmfattetAvLoennstrekkordningen";
    private static final String JSONPATH_SVALBARDLONN = "$.svalbardGrunnlag[?(@.tekniskNavn == \"" + SVALBARDLONN_ATTRIBUTE_NAME + "\")].beloep";



    public static Optional<Long> finnLoennsinntektMedTrygdeavgiftspliktOmfattetAvLoennstrekkordningen(String jsonDocument){
        var configuration = Configuration.builder().options(Option.SUPPRESS_EXCEPTIONS).build();
        var documentContext = JsonPath.using(configuration).parse(jsonDocument);
        List<Integer> beloep = documentContext.read(JSONPATH_SVALBARDLONN);
        return beloep.isEmpty() ? Optional.empty(): Optional.of(beloep.get(0).longValue());
    }
}
