package com.example.httpsniffer.model;

import java.util.Collections;
import java.util.Map;

public final class HttpRequestPacket {
    private final String method;
    private final String uri;
    private final String host;
    private final String sourceIp;
    private final int sourcePort;
    private final String destinationIp;
    private final int destinationPort;
    private final Map<String, String> headers;
    private final byte[] payload;
    private final long timestamp;

    public HttpRequestPacket(
            String method,
            String uri,
            String host,
            String sourceIp,
            int sourcePort,
            String destinationIp,
            int destinationPort,
            Map<String, String> headers,
            byte[] payload,
            long timestamp
    ) {
        this.method = method;
        this.uri = uri;
        this.host = host;
        this.sourceIp = sourceIp;
        this.sourcePort = sourcePort;
        this.destinationIp = destinationIp;
        this.destinationPort = destinationPort;
        this.headers = headers != null ? Collections.unmodifiableMap(headers) : Collections.emptyMap();
        this.payload = payload;
        this.timestamp = timestamp;
    }

    public String getMethod() { return method; }
    public String getUri() { return uri; }
    public String getHost() { return host; }
    public String getSourceIp() { return sourceIp; }
    public int getSourcePort() { return sourcePort; }
    public String getDestinationIp() { return destinationIp; }
    public int getDestinationPort() { return destinationPort; }
    public Map<String, String> getHeaders() { return headers; }
    public byte[] getPayload() { return payload; }
    public long getTimestamp() { return timestamp; }
}
