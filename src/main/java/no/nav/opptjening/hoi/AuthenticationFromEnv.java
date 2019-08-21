package no.nav.opptjening.hoi;

import no.nav.opptjening.skatt.client.api.AuthenticationHeader;

import java.util.Map;

public class AuthenticationFromEnv implements AuthenticationHeader {

    private static final String beregnetSkattApiKey = "BEREGNETSKATT_API_KEY";
    private static final String summertSkattegrunnlagApiKey = "SUMMERTSKATTEGRUNNLAG_API_KEY";

    private final String apiKey;

    private AuthenticationFromEnv(Map<String, String> environment, String keyName) {
        this.apiKey = environment.get(keyName);
    }

    static AuthenticationHeader forSummertSkatt(Map<String, String> environment){
        return new AuthenticationFromEnv(environment, summertSkattegrunnlagApiKey);
    }

    static AuthenticationHeader forBeregnetSkatt(Map<String, String> environment){
        return new AuthenticationFromEnv(environment, beregnetSkattApiKey);
    }

    @Override
    public String value() {
        return apiKey;
    }
}
