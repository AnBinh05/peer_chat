package com.p2p.model;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Peer model đại diện cho 1 node trong P2P network.
 * Dùng cho:
 * - Peer Discovery
 * - Chat 1-1
 * - Voice Call
 * - Friend / Group
 */
public class Peer {

    // ----------------------------------------------------------
    // Fields
    // ----------------------------------------------------------
    private String id;                 // UUID duy nhất
    private String name;               // username hiển thị
    private InetAddress address;       // IP LAN

    private int textPort;              // chat text
    private int voicePort;             // voice call
    private int signalPort;            // signal (friend / call)

    private LocalDateTime lastSeen;    // heartbeat timestamp

    // ----------------------------------------------------------
    // Constructor mặc định (BẮT BUỘC cho Gson / JavaFX)
    // ----------------------------------------------------------
    public Peer() {
        this.id = UUID.randomUUID().toString();
        this.lastSeen = LocalDateTime.now();
    }

    // ----------------------------------------------------------
    // Constructor FULL (DÙNG CHO DISCOVERY)
    // ----------------------------------------------------------
    public Peer(String name,
                InetAddress address,
                int textPort,
                int voicePort,
                int signalPort) {

        this();
        this.name = name;
        this.address = address;
        this.textPort = textPort;
        this.voicePort = voicePort;
        this.signalPort = signalPort;
    }

    // ----------------------------------------------------------
    // Constructor rút gọn
    // ----------------------------------------------------------
    public Peer(String name,
                InetAddress address,
                int textPort,
                int voicePort) {

        this(name, address, textPort, voicePort, -1);
    }

    // ----------------------------------------------------------
    // Heartbeat
    // ----------------------------------------------------------
    public void updateLastSeen() {
        this.lastSeen = LocalDateTime.now();
    }

    /** Mặc định timeout = 10s (backward compatibility) */
    public boolean isAlive() {
        return isAlive(10);
    }

    /** ✅ DÙNG CHO PeerDiscoveryService */
    public boolean isAlive(int timeoutSeconds) {
        return LocalDateTime.now()
                .minusSeconds(timeoutSeconds)
                .isBefore(lastSeen);
    }

    // ----------------------------------------------------------
    // Getters / Setters
    // ----------------------------------------------------------
    public String getId() { return id; }

    /**
     * ⚠️ CHỈ dùng cho PeerDiscovery
     * set ID từ heartbeat sender
     */
    public void setId(String id) {
        if (id != null && !id.isBlank()) {
            this.id = id;
        }
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public InetAddress getAddress() { return address; }
    public void setAddress(InetAddress address) { this.address = address; }

    public int getTextPort() { return textPort; }
    public void setTextPort(int textPort) { this.textPort = textPort; }

    public int getVoicePort() { return voicePort; }
    public void setVoicePort(int voicePort) { this.voicePort = voicePort; }

    public int getSignalPort() { return signalPort; }
    public void setSignalPort(int signalPort) { this.signalPort = signalPort; }

    public LocalDateTime getLastSeen() { return lastSeen; }
    public void setLastSeen(LocalDateTime lastSeen) { this.lastSeen = lastSeen; }

    // ----------------------------------------------------------
    // equals & hashCode
    // ----------------------------------------------------------
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Peer)) return false;
        Peer other = (Peer) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // ----------------------------------------------------------
    // UI display (ListView)
    // ----------------------------------------------------------
    @Override
    public String toString() {

        String displayName = (name != null && !name.isBlank())
                ? name
                : "Unknown";

        String ip = (address != null)
                ? address.getHostAddress()
                : "N/A";

        return displayName + " (" + ip + ")";
    }
}
