package no.nav.opptjening.hoi;

import no.nav.opptjening.skatt.client.api.AuthenticationHeader;

import java.util.Map;

public class AuthenticationFromEnv implements AuthenticationHeader {

    private final String apiKey;

    public AuthenticationFromEnv(Map<String, String> environment) {
        this.apiKey = environment.get("SKATT_API_KEY");
    }

    @Override
    public String value() {
        return apiKey;
    }
}
