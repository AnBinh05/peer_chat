package com.p2p.service;

import com.p2p.model.Message;
import com.p2p.model.Peer;
import com.p2p.util.JsonUtil;
import javafx.application.Platform;

import java.net.*;
import java.util.function.Consumer;

public class MessageService {

    private static final int BUFFER_SIZE = 65536;

    private final Peer localPeer;

    private DatagramSocket textSocket;
    private DatagramSocket signalSocket;

    private volatile boolean running = false;

    private Consumer<Message> onPrivateMessage;
    private Consumer<Message> onSignalMessage;

    public MessageService(Peer localPeer) {
        this.localPeer = localPeer;
    }

    public void start() throws Exception {
        textSocket = new DatagramSocket(localPeer.getTextPort());
        signalSocket = new DatagramSocket(localPeer.getSignalPort());
        running = true;

        startTextListener();
        startSignalListener();

        System.out.println("📡 MessageService started");
    }

    public void stop() {
        running = false;
        if (textSocket != null) textSocket.close();
        if (signalSocket != null) signalSocket.close();
    }

    // ================= LISTENERS =================

    private void startTextListener() {
        new Thread(() -> {
            byte[] buf = new byte[BUFFER_SIZE];
            while (running) {
                try {
                    DatagramPacket p = new DatagramPacket(buf, buf.length);
                    textSocket.receive(p);

                    Message msg = JsonUtil.fromJson(
                            new String(p.getData(), 0, p.getLength())
                    );

                    if (msg != null && onPrivateMessage != null) {
                        Platform.runLater(() -> onPrivateMessage.accept(msg));
                    }
                } catch (Exception e) {
                    if (running) e.printStackTrace();
                }
            }
        }, "TextListener").start();
    }

    private void startSignalListener() {
        new Thread(() -> {
            byte[] buf = new byte[BUFFER_SIZE];
            while (running) {
                try {
                    DatagramPacket p = new DatagramPacket(buf, buf.length);
                    signalSocket.receive(p);

                    Message msg = JsonUtil.fromJson(
                            new String(p.getData(), 0, p.getLength())
                    );

                    if (msg != null && onSignalMessage != null) {
                        Platform.runLater(() -> onSignalMessage.accept(msg));
                    }

                } catch (Exception e) {
                    if (running) e.printStackTrace();
                }
            }
        }, "SignalListener").start();
    }

    // ================= SEND =================

    public void sendPrivateText(String text, Peer target) {
        Message msg = new Message(
                Message.Type.TEXT,
                localPeer.getId(),
                localPeer.getName(),
                text
        );
        sendPrivateMessage(msg, target);
    }

    public void sendPrivateMessage(Message msg, Peer target) {
        try {
            byte[] data = JsonUtil.toJson(msg).getBytes();
            DatagramPacket p = new DatagramPacket(
                    data, data.length,
                    target.getAddress(), target.getTextPort()
            );
            textSocket.send(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendSignalMessage(Message msg, Peer target) {
        try {
            byte[] data = JsonUtil.toJson(msg).getBytes();
            DatagramPacket p = new DatagramPacket(
                    data, data.length,
                    target.getAddress(), target.getSignalPort()
            );
            signalSocket.send(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= CALLBACK =================

    public void setOnPrivateMessage(Consumer<Message> cb) {
        this.onPrivateMessage = cb;
    }

    public void setOnSignalMessage(Consumer<Message> cb) {
        this.onSignalMessage = cb;
    }

}
