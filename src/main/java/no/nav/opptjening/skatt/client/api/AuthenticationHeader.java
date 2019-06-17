package no.nav.opptjening.skatt.client.api;

public interface AuthenticationHeader {
    String APIKEY_HEADER_NAME = "X-NAV-APIKEY";
    String value();
}
