package de.nieting.burpVars.model.serializer;

import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.message.requests.HttpRequest;
import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class HttpRequestDeserializer extends JsonDeserializer<HttpRequest> {
    private static final Logger LOGGER = LogManager.getLogger(HttpRequestDeserializer.class);

    @Override
    public HttpRequest deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        var bytes = DeflateCompressor.decompress(p.getBinaryValue());
        if (bytes == null) return null;
        return HttpRequest.httpRequest(ByteArray.byteArray(bytes));
    }
}
