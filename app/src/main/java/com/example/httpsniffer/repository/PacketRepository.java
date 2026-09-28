package com.example.httpsniffer.repository;

import com.example.httpsniffer.model.HttpRequestPacket;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

public final class PacketRepository {

    private static final PacketRepository INSTANCE = new PacketRepository();
    private final PublishSubject<HttpRequestPacket> packetSubject = PublishSubject.create();

    private PacketRepository() {}

    public static PacketRepository getInstance() {
        return INSTANCE;
    }

    public void emitPacket(HttpRequestPacket packet) {
        packetSubject.onNext(packet);
    }

    public Observable<HttpRequestPacket> getPacketStream() {
        return packetSubject.hide();
    }
}
