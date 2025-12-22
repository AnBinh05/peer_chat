package com.p2p.service;

import com.p2p.model.Message;
import com.p2p.model.Peer;
import com.p2p.util.JsonUtil;
import javafx.application.Platform;

import javax.sound.sampled.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class VoiceCallService {

    private final Peer localPeer;

    private Thread signalListenerThread;

    public VoiceCallService(Peer peer) {
        this.localPeer = peer;
    }

    public void start() {
        startSignalListener();
        System.out.println("🔊 VoiceCallService started on signalPort " + localPeer.getSignalPort());
    }

    private void startSignalListener() {

        signalListenerThread = new Thread(() -> {

            try (DatagramSocket socket = new DatagramSocket(localPeer.getVoicePort())) {

                byte[] buffer = new byte[4096];

                while (!Thread.interrupted()) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    String json = new String(packet.getData(), 0, packet.getLength());
                    Message msg = JsonUtil.fromJson(json);

                    if (msg != null) handleSignal(msg);
                }

            } catch (Exception e) {
                System.err.println("❌ Signal listener closed: " + e.getMessage());
            }

        });

        signalListenerThread.setDaemon(true);
        signalListenerThread.start();
    }

    private void handleSignal(Message msg) {
        System.out.println("📨 Signal received: " + msg.getType());
    }

    public void stop() {
        if (signalListenerThread != null) signalListenerThread.interrupt();
    }
}
