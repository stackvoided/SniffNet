package com.example.httpsniffer.service;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;

import com.example.httpsniffer.model.HttpRequestPacket;
import com.example.httpsniffer.net.IpPacketParser;
import com.example.httpsniffer.repository.PacketRepository;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class HttpSnifferVpnService extends VpnService implements Runnable {

    public static final String ACTION_STOP_SERVICE = "com.example.httpsniffer.STOP_SERVICE";

    private ParcelFileDescriptor vpnInterface;
    private Thread workerThread;
    private volatile boolean isRunning = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP_SERVICE.equals(intent.getAction())) {
            stopVpn();
            return START_NOT_STICKY;
        }

        if (!isRunning) {
            startVpn();
        }

        return START_STICKY;
    }

    private void startVpn() {
        try {
            Builder builder = new Builder();
            builder.setSession("HttpSnifferService")
                    .addAddress("10.0.0.2", 32)
                    .addRoute("0.0.0.0", 0);

            try {
                builder.addDisallowedApplication(getPackageName());
            } catch (PackageManager.NameNotFoundException ignored) {}

            vpnInterface = builder.establish();
            if (vpnInterface == null) return;

            isRunning = true;
            workerThread = new Thread(this, "HttpSniffer-VpnWorker");
            workerThread.start();
        } catch (Exception e) {
            stopVpn();
        }
    }

    @Override
    public void run() {
        FileInputStream in = new FileInputStream(vpnInterface.getFileDescriptor());
        FileOutputStream out = new FileOutputStream(vpnInterface.getFileDescriptor());

        ByteBuffer packetBuffer = ByteBuffer.allocateDirect(32767);
        byte[] rawBuffer = packetBuffer.array();

        try {
            while (isRunning && !Thread.currentThread().isInterrupted()) {
                int length = in.read(rawBuffer);
                if (length > 0) {
                    packetBuffer.limit(length);
                    packetBuffer.position(0);

                    HttpRequestPacket httpRequest = IpPacketParser.processIpPacket(packetBuffer, length);
                    if (httpRequest != null) {
                        PacketRepository.getInstance().emitPacket(httpRequest);
                    }

                    out.write(rawBuffer, 0, length);
                }
            }
        } catch (IOException ignored) {
        } finally {
            closeStreams(in, out);
        }
    }

    private void stopVpn() {
        isRunning = false;
        if (workerThread != null) {
            workerThread.interrupt();
            workerThread = null;
        }
        if (vpnInterface != null) {
            try {
                vpnInterface.close();
            } catch (IOException ignored) {}
            vpnInterface = null;
        }
        stopSelf();
    }

    private void closeStreams(FileInputStream in, FileOutputStream out) {
        try {
            in.close();
        } catch (IOException ignored) {}
        try {
            out.close();
        } catch (IOException ignored) {}
    }

    @Override
    public void onDestroy() {
        stopVpn();
        super.onDestroy();
    }
}
