package de.nieting.burpVars.model.serializer;

import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class HttpResponseSerializer extends JsonSerializer<HttpResponse> {
    private static final Logger LOGGER = LogManager.getLogger(HttpResponseSerializer.class);
    @Override
    public void serialize(HttpResponse value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        var req = DeflateCompressor.compress(value.toByteArray().getBytes());
        if (req == null) {
            gen.writeNull();
            return;
        }
        gen.writeBinary(req);
    }

}
