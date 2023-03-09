package de.nieting.burpVars.model.serializer;

import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class HttpResponseDeserializer extends JsonDeserializer<HttpResponse> {
    private static final Logger LOGGER = LogManager.getLogger(HttpResponseDeserializer.class);

    @Override
    public HttpResponse deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        var bytes = DeflateCompressor.decompress(p.getBinaryValue());
        if (bytes == null) return null;
        return HttpResponse.httpResponse(ByteArray.byteArray(bytes));
    }
}
