package com.p2p.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Message model cho hệ thống P2P
 * Hỗ trợ:
 * - Private / Group chat
 * - Friend system (Add / Unfriend)
 * - Group system (Create / Delete)
 * - Read receipt (Đã gửi / Đã đọc)
 */
public class Message {

    // ================= MESSAGE TYPE =================
    public enum Type {

        // ----- SYSTEM -----
        HEARTBEAT,

        // ----- CHAT -----
        TEXT,
        IMAGE,
        FILE,
        GROUP_TEXT,

        // ----- FRIEND -----
        FRIEND_REQUEST,
        FRIEND_ACCEPT,
        FRIEND_REJECT,
        UNFRIEND,

        // ----- GROUP -----
        GROUP_CREATE,
        GROUP_INVITE,
        GROUP_ACCEPT,
        GROUP_REJECT,
        GROUP_DELETE,     // 🔥 xóa nhóm (all out)

        // ----- MESSAGE CONTROL -----
        READ_RECEIPT,     // 🔥 đã đọc
        RECALL,
        FORWARD,

        // ----- FILE -----
        FILE_META,
        FILE_ACCEPT,
        FILE_REJECT

    }

    // ================= MESSAGE STATUS =================
    public enum Status {
        SENT,       // đã gửi
        DELIVERED,  // đã nhận
        READ        // đã đọc
    }

    // ================= COMMON FIELDS =================
    private String id;
    private Type type;
    private Status status;

    private String from;
    private String fromName;
    private String target;

    private String content;
    private String timestamp;

    // ================= GROUP =================
    private String groupId;
    private String groupName;

    // ================= FILE / IMAGE =================
    private String fileName;
    private long fileSize;
    private String fileType;

    // ================= REPLY / CONTROL =================
    private String replyToMessageId;

    // ================= PORT INFO =================
    private int textPort;
    private int voicePort;
    private int signalPort;

    // ================= FILE =================
    private String fileId;
    private int filePort;

    // getters
    public String getFileId() { return fileId; }
    public int getFilePort() { return filePort; }
    public static Message fileMeta(
            Peer from,
            Peer target,
            String fileId,
            String fileName,
            long fileSize,
            int filePort
    ) {
        Message m = new Message(Type.FILE_META, from.getId(), from.getName());
        m.target = target != null ? target.getId() : null;
        m.fileId = fileId;
        m.fileName = fileName;
        m.fileSize = fileSize;
        m.filePort = filePort;

        // Chỉ để hiển thị, KHÔNG dùng làm logic
        m.content = "[FILE] " + fileName;

        return m;
    }

    // ================= FILE GETTERS =================
    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getFileType() {
        return fileType;
    }
    // ================= GROUP SETTERS =================
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    // ================= CONSTRUCTOR =================
    public Message() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("HH:mm dd/MM"));
        this.status = Status.SENT;
    }

    public Message(Type type, String from, String fromName) {
        this();
        this.type = type;
        this.from = from;
        this.fromName = fromName;
    }

    public Message(Type type, String from, String fromName, String content) {
        this(type, from, fromName);
        this.content = content;
    }

    // ================= HEARTBEAT =================
    public Message(Type type,
                   String from,
                   String fromName,
                   int textPort,
                   int voicePort,
                   int signalPort) {
        this(type, from, fromName);
        this.textPort = textPort;
        this.voicePort = voicePort;
        this.signalPort = signalPort;
    }

    // ================= FACTORY: FRIEND =================
    public static Message friendRequest(Peer from, Peer target) {
        Message m = new Message(Type.FRIEND_REQUEST, from.getId(), from.getName());
        m.target = target.getId();
        return m;
    }

    public static Message friendAccept(Peer from, Peer target) {
        Message m = new Message(Type.FRIEND_ACCEPT, from.getId(), from.getName());
        m.target = target.getId();
        return m;
    }

    public static Message unfriend(Peer from, Peer target) {
        Message m = new Message(Type.UNFRIEND, from.getId(), from.getName());
        m.target = target.getId();
        return m;
    }

    // ================= FACTORY: GROUP =================
    public static Message groupText(
            Peer from,
            String groupId,
            String groupName,
            String content
    ) {
        Message m = new Message(Type.GROUP_TEXT, from.getId(), from.getName(), content);
        m.groupId = groupId;
        m.groupName = groupName;
        return m;
    }

    public static Message groupDelete(Peer from, String groupId) {
        Message m = new Message(Type.GROUP_DELETE, from.getId(), from.getName());
        m.groupId = groupId;
        return m;
    }

    // ================= FACTORY: READ RECEIPT =================
    public static Message readReceipt(Peer from, String messageId) {
        Message m = new Message(Type.READ_RECEIPT, from.getId(), from.getName());
        m.replyToMessageId = messageId;
        return m;
    }

    // ================= GETTERS =================
    public String getId() { return id; }
    public Type getType() { return type; }
    public Status getStatus() { return status; }
    public String getFrom() { return from; }
    public String getFromName() { return fromName; }
    public String getTarget() { return target; }
    public String getContent() { return content; }
    public String getTimestamp() { return timestamp; }

    public String getGroupId() { return groupId; }
    public String getGroupName() { return groupName; }

    public String getReplyToMessageId() { return replyToMessageId; }

    public int getTextPort() { return textPort; }
    public int getVoicePort() { return voicePort; }
    public int getSignalPort() { return signalPort; }
    private boolean recalled;
    // ===== IMAGE LOCAL PATH =====
    private String localImagePath;

    public String getLocalImagePath() {
        return localImagePath;
    }

    public void setLocalImagePath(String path) {
        this.localImagePath = path;
    }

    public boolean isRecalled() {
        return recalled;
    }

    public void setRecalled(boolean recalled) {
        this.recalled = recalled;
    }
    // ================= TARGET SETTER =================
    public void setTarget(String target) {
        this.target = target;
    }

    // ================= SETTERS =================
    public void setStatus(Status status) {
        this.status = status;
    }
    // ================= FILE SETTERS =================
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    // ================= IMAGE =================
    public static Message imageMeta(
            Peer from,
            Peer target,
            String fileId,
            String fileName,
            long fileSize,
            int filePort
    ) {
        Message m = new Message(Type.IMAGE, from.getId(), from.getName());
        m.target = target != null ? target.getId() : null;
        m.fileId = fileId;
        m.fileName = fileName;
        m.fileSize = fileSize;
        m.filePort = filePort;

        m.content = "[IMAGE]";

        return m;
    }

    // ================= HELPERS =================
    public boolean isPrivateMessage() {
        return groupId == null && target != null;
    }

    public boolean isGroupMessage() {
        return groupId != null;
    }

    public boolean isRead() {
        return status == Status.READ;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public void setReplyToMessageId(String id) {
        this.replyToMessageId = id;
    }

    @Override
    public String toString() {

        if (type == Type.FILE_META) {
            return fromName + " sent a file: " + fileName;
        }

        if (content == null) return "";

        if (groupName != null) {
            return "[" + groupName + "] " + fromName + ": " + content;
        }

        return fromName + ": " + content;
    }

    // ================= FACTORY: GROUP INVITE =================
    public static Message groupInvite(
            Peer from,
            Peer target,
            String groupId,
            String groupName
    ) {
        Message m = new Message(Type.GROUP_INVITE, from.getId(), from.getName());
        m.target = target.getId();
        m.groupId = groupId;
        m.groupName = groupName;
        m.signalPort = from.getSignalPort();
        return m;
    }

}
