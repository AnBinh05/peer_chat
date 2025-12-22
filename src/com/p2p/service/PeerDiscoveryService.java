package com.p2p.service;

import com.p2p.model.Message;
import com.p2p.model.Peer;
import com.p2p.util.JsonUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * PeerDiscoveryService
 * - UDP Multicast LAN discovery
 * - Heartbeat mechanism
 * - Auto remove offline peers
 */
public class PeerDiscoveryService {

    private static final String MULTICAST_GROUP = "230.0.0.1";
    private static final int MULTICAST_PORT = 4446;

    private static final int HEARTBEAT_INTERVAL = 2; // seconds
    private static final int PEER_TIMEOUT = 6;       // seconds

    private final Peer localPeer;
    private final ObservableList<Peer> peers = FXCollections.observableArrayList();

    private MulticastSocket socket;
    private InetAddress group;

    private volatile boolean running = false;

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(3);

    public PeerDiscoveryService(Peer localPeer) {
        this.localPeer = localPeer;
    }

    // ================= START =================
    public void start() throws Exception {
        if (running) return;
        running = true;

        group = InetAddress.getByName(MULTICAST_GROUP);

        socket = new MulticastSocket(MULTICAST_PORT);
        socket.setReuseAddress(true);
        socket.setTimeToLive(1);

        // 🔥 FIX QUAN TRỌNG: JOIN DEFAULT INTERFACE
        socket.joinGroup(group);

        System.out.println("✅ Peer Discovery started (default interface) "
                + MULTICAST_GROUP + ":" + MULTICAST_PORT);

        startListening();
        startHeartbeat();
        startPeerCleanup();
    }

    // ================= HEARTBEAT =================
    private void startHeartbeat() {
        scheduler.scheduleAtFixedRate(() -> {
            if (!running) return;

            try {
                Message heartbeat = new Message(
                        Message.Type.HEARTBEAT,
                        localPeer.getId(),
                        localPeer.getName(),
                        localPeer.getTextPort(),
                        localPeer.getVoicePort(),
                        localPeer.getSignalPort()
                );

                byte[] data = JsonUtil.toJson(heartbeat)
                        .getBytes(StandardCharsets.UTF_8);

                DatagramPacket packet = new DatagramPacket(
                        data, data.length, group, MULTICAST_PORT
                );

                socket.send(packet);

            } catch (Exception e) {
                System.err.println("❌ Heartbeat error: " + e.getMessage());
            }

        }, 0, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
    }

    // ================= LISTEN =================
    private void startListening() {
        Thread listener = new Thread(() -> {

            byte[] buffer = new byte[4096];

            while (running) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    String json = new String(
                            packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8
                    );

                    Message message = JsonUtil.fromJson(json);

                    if (message != null
                            && message.getType() == Message.Type.HEARTBEAT) {
                        handleHeartbeat(message, packet.getAddress());
                    }

                } catch (Exception e) {
                    if (running) {
                        System.err.println("❌ Discovery receive error: " + e.getMessage());
                    }
                }
            }
        });

        listener.setDaemon(true);
        listener.setName("PeerDiscoveryListener");
        listener.start();
    }

    // ================= HANDLE HEARTBEAT =================
    private void handleHeartbeat(Message msg, InetAddress address) {

        // Ignore myself
        if (msg.getFrom().equals(localPeer.getId())) return;

        Peer existing = peers.stream()
                .filter(p -> p.getId().equals(msg.getFrom()))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.updateLastSeen();

            if (!existing.getAddress().equals(address)) {
                existing.setAddress(address);
            }
            return;
        }

        Peer newPeer = new Peer(
                msg.getFromName(),
                address,
                msg.getTextPort(),
                msg.getVoicePort(),
                msg.getSignalPort()
        );
        newPeer.setId(msg.getFrom());

        Platform.runLater(() -> {
            peers.add(newPeer);
            System.out.println("🆕 Peer discovered: "
                    + newPeer.getName() + " (" + address.getHostAddress() + ")");
        });
    }

    // ================= CLEANUP =================
    private void startPeerCleanup() {
        scheduler.scheduleAtFixedRate(() -> {

            Platform.runLater(() -> peers.removeIf(peer -> {
                boolean alive = peer.isAlive(PEER_TIMEOUT);
                if (!alive) {
                    System.out.println("❌ Peer offline: " + peer.getName());
                }
                return !alive;
            }));

        }, PEER_TIMEOUT, PEER_TIMEOUT, TimeUnit.SECONDS);
    }

    // ================= GETTERS =================
    public ObservableList<Peer> getPeers() {
        return peers;
    }

    public Peer getLocalPeer() {
        return localPeer;
    }

    // ================= STOP =================
    public void stop() {
        running = false;

        try {
            if (socket != null) {
                socket.leaveGroup(group);
                socket.close();
            }
        } catch (Exception ignored) {}

        scheduler.shutdownNow();
        System.out.println("🛑 Peer Discovery stopped");
    }
}
