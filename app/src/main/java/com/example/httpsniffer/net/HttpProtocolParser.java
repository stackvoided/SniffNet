package com.example.httpsniffer.net;

import com.example.httpsniffer.model.HttpRequestPacket;

import java.nio.ByteBuffer;

public final class HttpProtocolParser {

    private static final byte[] GET = {'G', 'E', 'T'};
    private static final byte[] POST = {'P', 'O', 'S', 'T'};
    private static final byte[] PUT = {'P', 'U', 'T'};
    private static final byte[] DELETE = {'D', 'E', 'L', 'E', 'T', 'E'};
    private static final byte[] HEAD = {'H', 'E', 'A', 'D'};
    private static final byte[] OPTIONS = {'O', 'P', 'T', 'I', 'O', 'N', 'S'};
    private static final byte[] PATCH = {'P', 'A', 'T', 'C', 'H'};

    private HttpProtocolParser() {}

    public static boolean isHttpRequest(ByteBuffer buffer, int payloadOffset, int payloadLength) {
        if (payloadLength < 10) return false;

        int oldPosition = buffer.position();
        buffer.position(payloadOffset);

        boolean isHttp = matchPrefix(buffer, GET)
                || matchPrefix(buffer, POST)
                || matchPrefix(buffer, PUT)
                || matchPrefix(buffer, DELETE)
                || matchPrefix(buffer, HEAD)
                || matchPrefix(buffer, OPTIONS)
                || matchPrefix(buffer, PATCH);

        buffer.position(oldPosition);
        return isHttp;
    }

    public static HttpRequestPacket parse(
            ByteBuffer buffer,
            int payloadOffset,
            int payloadLength,
            String srcIp,
            int srcPort,
            String dstIp,
            int dstPort
    ) {
        byte[] payloadBytes = new byte[payloadLength];
        int oldPosition = buffer.position();
        buffer.position(payloadOffset);
        buffer.get(payloadBytes, 0, payloadLength);
        buffer.position(oldPosition);

        String raw = new String(payloadBytes, 0, Math.min(payloadLength, 2048));
        String[] lines = raw.split("\r\n");
        if (lines.length == 0) return null;

        String[] requestLine = lines[0].split(" ");
        if (requestLine.length < 2) return null;

        String method = requestLine[0];
        String uri = requestLine[1];
        String host = dstIp;

        java.util.Map<String, String> headers = new java.util.HashMap<>();
        for (int i = 1; i < lines.length; i++) {
            if (lines[i].isEmpty()) break;
            int colonIdx = lines[i].indexOf(':');
            if (colonIdx > 0) {
                String key = lines[i].substring(0, colonIdx).trim();
                String value = lines[i].substring(colonIdx + 1).trim();
                headers.put(key, value);
                if ("Host".equalsIgnoreCase(key)) {
                    host = value;
                }
            }
        }

        return new HttpRequestPacket(
                method,
                uri,
                host,
                srcIp,
                srcPort,
                dstIp,
                dstPort,
                headers,
                payloadBytes,
                System.currentTimeMillis()
        );
    }

    private static boolean matchPrefix(ByteBuffer buffer, byte[] prefix) {
        if (buffer.remaining() < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) {
            if (buffer.get(buffer.position() + i) != prefix[i]) return false;
        }
        return true;
    }
}
