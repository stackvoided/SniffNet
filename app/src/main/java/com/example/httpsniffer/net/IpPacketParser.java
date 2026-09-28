package com.example.httpsniffer.net;

import com.example.httpsniffer.model.HttpRequestPacket;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public final class IpPacketParser {

    private static final byte PROTOCOL_TCP = 6;

    private IpPacketParser() {}

    public static HttpRequestPacket processIpPacket(ByteBuffer buffer, int length) {
        if (length < 20) return null;

        buffer.position(0);
        byte versionAndIhl = buffer.get();
        int version = (versionAndIhl >> 4) & 0x0F;

        if (version != 4) return null;

        int ihl = (versionAndIhl & 0x0F) * 4;
        if (length < ihl) return null;

        buffer.position(9);
        byte protocol = buffer.get();

        if (protocol != PROTOCOL_TCP) return null;

        byte[] srcIpBytes = new byte[4];
        byte[] dstIpBytes = new byte[4];

        buffer.position(12);
        buffer.get(srcIpBytes);
        buffer.get(dstIpBytes);

        String srcIp = formatIp(srcIpBytes);
        String dstIp = formatIp(dstIpBytes);

        int tcpOffset = ihl;
        if (length < tcpOffset + 20) return null;

        buffer.position(tcpOffset);
        int srcPort = buffer.getShort() & 0xFFFF;
        int dstPort = buffer.getShort() & 0xFFFF;

        buffer.position(tcpOffset + 12);
        byte dataOffsetByte = buffer.get();
        int dataOffset = ((dataOffsetByte >> 4) & 0x0F) * 4;

        int payloadOffset = tcpOffset + dataOffset;
        int payloadLength = length - payloadOffset;

        if (payloadLength <= 0) return null;

        if (HttpProtocolParser.isHttpRequest(buffer, payloadOffset, payloadLength)) {
            return HttpProtocolParser.parse(buffer, payloadOffset, payloadLength, srcIp, srcPort, dstIp, dstPort);
        }

        return null;
    }

    private static String formatIp(byte[] ip) {
        try {
            return InetAddress.getByAddress(ip).getHostAddress();
        } catch (UnknownHostException e) {
            return "0.0.0.0";
        }
    }
}
