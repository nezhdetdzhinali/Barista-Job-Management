package be.hogent.baristajob2026.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class GeboortedatumDeserializer extends JsonDeserializer<LocalDate> {

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String valueAsString = p.getValueAsString();
        if (valueAsString == null || valueAsString.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(valueAsString, DateFormats.FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IOException("Ongeldige datum formaat: %s".formatted(valueAsString), e);
        }
    }
}