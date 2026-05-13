package ru.mfa.signature;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JsonCanonicalizer {

    private final byte[] encodedBytes;

    public JsonCanonicalizer(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(json);
            StringBuilder sb = new StringBuilder();
            serialize(node, sb);
            this.encodedBytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to canonicalize JSON: " + e.getMessage(), e);
        }
    }

    public byte[] getEncodedUTF8() {
        return encodedBytes;
    }

    private void serialize(JsonNode node, StringBuilder sb) {
        if (node.isObject()) {
            sb.append('{');
            List<String> keys = new ArrayList<>();
            node.fieldNames().forEachRemaining(keys::add);
            Collections.sort(keys);
            boolean first = true;
            for (String key : keys) {
                if (!first) sb.append(',');
                first = false;
                appendEscaped(key, sb);
                sb.append(':');
                serialize(node.get(key), sb);
            }
            sb.append('}');
        } else if (node.isArray()) {
            sb.append('[');
            boolean first = true;
            for (JsonNode element : node) {
                if (!first) sb.append(',');
                first = false;
                serialize(element, sb);
            }
            sb.append(']');
        } else if (node.isTextual()) {
            appendEscaped(node.textValue(), sb);
        } else if (node.isNull()) {
            sb.append("null");
        } else if (node.isBoolean()) {
            sb.append(node.booleanValue());
        } else if (node.isNumber()) {
            sb.append(node.numberValue());
        } else {
            sb.append(node);
        }
    }

    // RFC 8785 §3.2.2.2 string escaping
    private void appendEscaped(String value, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b");  break;
                case '\f': sb.append("\\f");  break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
    }
}
