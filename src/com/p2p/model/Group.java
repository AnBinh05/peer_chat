package com.p2p.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Group {

    private String id;
    private String name;
    private String ownerId;                  // 🔥 THÊM ownerId
    private Set<String> members = new HashSet<>(); // peerId

    // ================= CONSTRUCTOR =================
    public Group(String name, String ownerId) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.ownerId = ownerId;              // 🔥 GÁN owner
        this.members.add(ownerId);           // owner cũng là member
    }

    // ================= GETTERS =================
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getOwnerId() {              // 🔥 BẮT BUỘC
        return ownerId;
    }

    public Set<String> getMembers() {
        return members;
    }

    // ================= SETTERS =================
    public void setId(String id) {
        this.id = id;
    }

    public void addMember(String peerId) {
        members.add(peerId);
    }

    // ================= HELPERS =================
    public boolean isMember(String peerId) {
        return members.contains(peerId);
    }
}
