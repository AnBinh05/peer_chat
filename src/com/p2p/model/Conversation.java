package com.p2p.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.p2p.dao.UserDAO;

public class Conversation {

    private final String id;
    private final String name;
    private final boolean isGroup;

    private Peer peer;
    private Group group;

    private String lastMessage = "";
    private LocalDateTime lastMessageTime;
    private int unreadCount = 0;

    private final List<Message> messages = new ArrayList<>();

    // ===== PRIVATE CHAT =====
    public Conversation(Peer peer) {
        this.peer = peer;
        this.id = peer.getId();
        this.name = peer.getName();
        this.isGroup = false;
    }

    // ===== GROUP CHAT =====
    public Conversation(Group group) {
        this.group = group;
        this.id = group.getId();
        this.name = group.getName();
        this.isGroup = true;
    }

    // ===== GETTERS =====
    public String getId() { return id; }
    public String getName() {

        if (isGroup()) {
            return group.getName();
        }

        // ===== PRIVATE CHAT =====
        String peerId = peer.getId();

        // 🔥 LUÔN load username từ DB
        String username = UserDAO.getUsernameByPeerId(peerId);

        return username != null ? username : peerId;
    }

    public boolean isGroup() { return isGroup; }

    public Peer getPeer() { return peer; }
    public Group getGroup() { return group; }

    public String getLastMessage() { return lastMessage; }
    public LocalDateTime getLastMessageTime() { return lastMessageTime; }
    public int getUnreadCount() { return unreadCount; }

    public List<Message> getMessages() { return messages; }

    // ===== STATE =====
    public void addMessage(Message msg) {
        messages.add(msg);
    }

    public void setLastMessage(String msg) {
        this.lastMessage = msg;
    }

    public void setLastMessageTime(LocalDateTime time) {
        this.lastMessageTime = time;
    }

    public void incrementUnreadCount() {
        unreadCount++;
    }

    public void resetUnreadCount() {
        unreadCount = 0;
    }

    public boolean isOnline() {
        return !isGroup && peer != null;
    }
}
