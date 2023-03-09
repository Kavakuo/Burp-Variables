package de.nieting.burpVars.model.serializer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

public class DeflateCompressor {
    private static final Logger LOGGER = LogManager.getLogger(DeflateCompressor.class);

    public static byte[] decompress(byte[] in) {
        if (in == null) return null;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InflaterOutputStream infl = new InflaterOutputStream(out);
            infl.write(in);
            infl.flush();
            infl.close();

            return out.toByteArray();
        } catch (Exception e) {
            LOGGER.error("Failed to decompress", e);
            return null;
        }
    }

    public static byte[] compress(byte[] in) {
        if (in == null) return null;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DeflaterOutputStream defl = new DeflaterOutputStream(out);
            defl.write(in);
            defl.flush();
            defl.close();

            return out.toByteArray();
        } catch (Exception e) {
            LOGGER.error("Failed to compress", e);
            return null;
        }
    }
}
