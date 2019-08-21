package no.nav.opptjening.skatt.client.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

class JsonDeserializer {
    private final ObjectMapper objectMapper = new ObjectMapper();

    JsonDeserializer() {
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    <T> T toObject(String jsonDocument, Class<T> clazz) {
        try {
            return objectMapper.readValue(jsonDocument, clazz);
        } catch (IOException e) {
            throw new ResponseUnmappableException("Could not unmarshal JSON to " + clazz.getName(), e);
        }
    }
}
