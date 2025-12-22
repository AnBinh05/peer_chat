package com.p2p.controller;

import com.p2p.dao.FriendDAO;
import com.p2p.dao.GroupDAO;
import com.p2p.db.Database;
import com.p2p.model.*;
import com.p2p.service.*;
import javafx.fxml.FXMLLoader;
import com.p2p.dao.UserDAO;
import javafx.scene.Cursor;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
// ===== JAVA NET =====
import java.net.URL;

// ===== JAVA NIO FILE =====
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.DirectoryStream;

public class MainController {

    // ================= FXML =================
    @FXML
    private ListView<Conversation> conversationListView;
    @FXML
    private VBox chatMessagesArea;
    @FXML
    private ScrollPane chatScrollPane;
    @FXML
    private TextArea messageInput;
    @FXML
    private Button sendButton;
    @FXML
    private Button settingsButton;
    @FXML
    private TextField searchField;

    @FXML
    private Label chatHeaderName;
    @FXML
    private Label chatHeaderStatus;

    @FXML
    private Button callButton;
    @FXML
    private Button videoCallButton;
    @FXML
    private Button infoButton;
    // ================= ICONS =================
    @FXML
    private ImageView settingsIcon;
    @FXML
    private ImageView callIcon;
    @FXML
    private ImageView videoIcon;
    @FXML
    private ImageView infoIcon;

    @FXML
    private ImageView attachIcon;
    @FXML
    private ImageView imageIcon;
    @FXML
    private ImageView emojiIcon;
    @FXML
    private ImageView sendIcon;
    @FXML
    private Button addFriendButton;
    @FXML
    private Button createGroupButton;


    // ================= STATE =================
    private Peer localPeer;
    private Conversation selectedConversation;

    private final Map<String, Conversation> conversations = new HashMap<>();
    private final Set<String> friends = new HashSet<>();

    private PeerDiscoveryService discoveryService;
    private MessageService messageService;
    private VoiceCallService voiceCallService;

    private final UserSettings userSettings = new UserSettings();
    private Message replyTarget = null;

    // ================= INIT =================

    @FXML
    private void initialize() {

        conversationListView.setCellFactory(lv -> new ConversationCell());
        conversationListView.getSelectionModel().selectedItemProperty()
                .addListener((obs, o, n) -> openConversation(n));

        sendButton.setOnAction(e -> sendMessage());
        settingsButton.setOnAction(e -> openSettings());
        callButton.setOnAction(e -> initiateCall(false));
        videoCallButton.setOnAction(e -> initiateCall(true));
        infoButton.setOnAction(e -> showConversationInfo());

        searchField.textProperty().addListener((o, a, b) -> filterConversations(b));
        //    attachIcon.setOnMouseClicked(e -> chooseFile());

        chatMessagesArea.heightProperty()
                .addListener((o, a, b) -> chatScrollPane.setVvalue(1.0));

        // 🔥 ADD THIS
        loadIcons();
    }

    // ================= ADD FRIEND =================
    @FXML
    private void openAddFriendDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/p2p/view/add_friend.fxml")
            );

            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Add Friend");
            stage.initOwner(conversationListView.getScene().getWindow());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            AddFriendController controller = loader.getController();
            controller.setContext(localPeer, discoveryService, messageService, stage);

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= SEND FILE =================
    @FXML
    private void chooseFile() {

        if (selectedConversation == null || selectedConversation.isGroup()) {
            showAlert("File", "Only private chat supports file transfer");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Documents & Files",
                        "*.txt", "*.pdf",
                        "*.doc", "*.docx",
                        "*.xls", "*.xlsx",
                        "*.zip"
                )
        );

        File file = fc.showOpenDialog(chatMessagesArea.getScene().getWindow());
        if (file == null) return;

        sendFile(file);
    }

    private void sendFile(File file) {

        String fileId = UUID.randomUUID().toString();
        int port = 54000 + new Random().nextInt(1000);

        Message meta;

        // ===== GROUP FILE =====
        if (selectedConversation.isGroup()) {

            Group g = selectedConversation.getGroup();

            meta = Message.fileMeta(
                    localPeer,
                    null,
                    fileId,
                    file.getName(),
                    file.length(),
                    port
            );

            meta.setGroupId(g.getId());
            meta.setGroupName(g.getName());

            for (String memberId : g.getMembers()) {
                if (memberId.equals(localPeer.getId())) continue;
                Peer p = findPeerById(memberId);
                if (p != null && p.getAddress() != null) {
                    messageService.sendSignalMessage(meta, p);
                }
            }

        }
        // ===== PRIVATE FILE =====
        else {

            Peer target = findPeerById(selectedConversation.getId());
            if (target == null || target.getAddress() == null) {
                showAlert("Send file failed", "User is offline");
                return;
            }

            meta = Message.fileMeta(
                    localPeer,
                    target,
                    fileId,
                    file.getName(),
                    file.length(),
                    port
            );

            messageService.sendSignalMessage(meta, target);
        }

        FileTransferService.sendFile(file, port);

        selectedConversation.addMessage(meta);
        displayFileMessage(meta, true);
    }

    // ================= SEND IMAGE =================
    @FXML
    private void chooseImage() {

        if (selectedConversation == null) return;

        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Images", "*.png", "*.jpg", "*.jpeg", "*.gif"
                )
        );

        File image = fc.showOpenDialog(chatMessagesArea.getScene().getWindow());
        if (image == null) return;

        sendImage(image);
    }

    private void sendImage(File image) {

        String fileId = UUID.randomUUID().toString();
        int port = 55000 + new Random().nextInt(1000);

        Message imgMsg;

        // ===== GROUP IMAGE =====
        if (selectedConversation.isGroup()) {

            Group g = selectedConversation.getGroup();

            imgMsg = Message.imageMeta(
                    localPeer,
                    null,
                    fileId,
                    image.getName(),
                    image.length(),
                    port
            );

            imgMsg.setGroupId(g.getId());
            imgMsg.setGroupName(g.getName());

            for (String memberId : g.getMembers()) {
                if (memberId.equals(localPeer.getId())) continue;
                Peer p = findPeerById(memberId);
                if (p != null && p.getAddress() != null) {
                    messageService.sendSignalMessage(imgMsg, p);
                }
            }

        }
        // ===== PRIVATE IMAGE =====
        else {

            Peer target = findPeerById(selectedConversation.getId());
            if (target == null || target.getAddress() == null) {
                showAlert("Send image failed", "User is offline");
                return;
            }

            imgMsg = Message.imageMeta(
                    localPeer,
                    target,
                    fileId,
                    image.getName(),
                    image.length(),
                    port
            );

            messageService.sendSignalMessage(imgMsg, target);
        }

        FileTransferService.sendFile(image, port);
        imgMsg.setFileName(image.getAbsolutePath());
        selectedConversation.addMessage(imgMsg);
        displayImageMessage(imgMsg, true);
    }

    private void displayImageMessage(Message msg, boolean mine) {

        VBox box = new VBox(4);
        box.setStyle("""
        -fx-background-color: %s;
        -fx-padding:8;
        -fx-background-radius:12;
    """.formatted(mine ? "#0084ff" : "#f0f2f5"));

        ImageView img = new ImageView();
        img.setFitWidth(160);
        img.setPreserveRatio(true);

        try {
            File imgFile = new File(msg.getFileName());
            if (imgFile.exists()) {
                img.setImage(new Image(imgFile.toURI().toString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Label time = new Label(msg.getTimestamp());
        time.setStyle("-fx-font-size:10; -fx-text-fill:#888;");

        box.getChildren().addAll(img, time);

        HBox wrap = new HBox(box);
        wrap.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        wrap.setPadding(new Insets(4));

        chatMessagesArea.getChildren().add(wrap);
    }

    // ================= EMOJI =================
    @FXML
    private void openEmojiPicker() {

        ContextMenu menu = new ContextMenu();

        // ===== GRID CHỨA EMOJI =====
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(8));

        // ===== SCROLL =====
        ScrollPane scroll = new ScrollPane(grid);
        scroll.setPrefWidth(240);
        scroll.setPrefHeight(200);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        int col = 0;
        int row = 0;

        try {
            // ===== LOAD TẤT CẢ FILE TRONG FOLDER EMOJI =====
            URL dirURL = getClass().getResource("/com/p2p/view/emoji");

            if (dirURL == null) {
                System.err.println("❌ Emoji folder not found");
                return;
            }

            Path emojiPath;

            // 👉 chạy trong IDE
            if (dirURL.getProtocol().equals("file")) {
                emojiPath = Paths.get(dirURL.toURI());
            }
            // 👉 chạy khi build JAR
            else {
                FileSystem fs = FileSystems.newFileSystem(dirURL.toURI(), Map.of());
                emojiPath = fs.getPath("/com/p2p/view/emoji");
            }

            try (DirectoryStream<Path> stream =
                         Files.newDirectoryStream(emojiPath, "*.png")) {

                for (Path p : stream) {

                    String fileName = p.getFileName().toString();

                    InputStream is = getClass()
                            .getResourceAsStream("/com/p2p/view/emoji/" + fileName);

                    if (is == null) continue;

                    ImageView iv = new ImageView(new Image(is));
                    iv.setFitWidth(32);
                    iv.setFitHeight(32);
                    iv.setPreserveRatio(true);
                    iv.setCursor(Cursor.HAND);

                    iv.setOnMouseClicked(e -> {
                        sendEmoji(fileName); // 🔥 GỬI EMOJI
                        menu.hide();
                    });

                    grid.add(iv, col, row);
                    col++;

                    if (col == 6) { // 6 emoji / hàng
                        col = 0;
                        row++;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        CustomMenuItem item = new CustomMenuItem(scroll, false);
        menu.getItems().add(item);

        menu.show(emojiIcon, Side.TOP, 0, 0);
    }

    private void sendEmoji(String emojiFile) {

        if (selectedConversation == null) return;

        String content = ":" + emojiFile + ":";

        if (selectedConversation.isGroup()) {
            sendGroup(content);
        } else {
            sendPrivate(content);
        }
    }

    // ================= CREATE GROUP =================
    @FXML
    private void openCreateGroupDialog() {

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create Group");

        ButtonType createBtn = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createBtn, ButtonType.CANCEL);

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        TextField groupNameField = new TextField();
        groupNameField.setPromptText("Group name");

        ListView<Peer> friendList = new ListView<>();
        friendList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        List<Peer> friendPeers = friends.stream()
                .map(this::findPeerById)
                .filter(Objects::nonNull)
                .toList();

        friendList.getItems().addAll(friendPeers);

        root.getChildren().addAll(
                new Label("Group name"),
                groupNameField,
                new Label("Select members"),
                friendList
        );

        dialog.getDialogPane().setContent(root);

        dialog.showAndWait().ifPresent(bt -> {
            if (bt == createBtn) {

                String groupName = groupNameField.getText().trim();
                if (groupName.isEmpty()) {
                    showAlert("Invalid", "Group name is required");
                    return;
                }

                List<Peer> members =
                        new ArrayList<>(friendList.getSelectionModel().getSelectedItems());

                createGroup(groupName, members);
            }
        });
    }

    private void createGroup(String name, List<Peer> members) {

        Group g = new Group(name, localPeer.getId());
        g.getMembers().add(localPeer.getId());

        for (Peer p : members) {
            g.getMembers().add(p.getId());
        }

        GroupDAO.saveGroup(g, localPeer.getId());
        for (Peer p : members) {
            GroupDAO.saveMember(g.getId(), p.getId());
        }

        Conversation c = new Conversation(g);
        conversations.put(g.getId(), c);
        conversationListView.getItems().add(c);

        for (Peer p : members) {
            Message invite = Message.groupInvite(localPeer, p, g.getId(), g.getName());
            messageService.sendSignalMessage(invite, p);
        }

        showNotification("Group Created", g.getName());
    }

    // ================= ICON LOADER =================
    private void safeSet(ImageView iv, String name) {
        if (iv != null) iv.setImage(loadIcon(name));
    }

    private void loadIcons() {
        safeSet(settingsIcon, "setting.png");
        safeSet(callIcon, "phone.png");
        safeSet(videoIcon, "video.png");
        safeSet(infoIcon, "info.png");
        safeSet(attachIcon, "attach.png");
        safeSet(imageIcon, "image.png");
        safeSet(emojiIcon, "emoji.png");
        safeSet(sendIcon, "send.png");
    }


    private Image loadIcon(String name) {
        return new Image(
                Objects.requireNonNull(
                        getClass().getResourceAsStream("/com/p2p/view/img/" + name),
                        "Icon not found: " + name
                )
        );
    }

    // ================= CONTEXT =================
    public void setContext(Peer localPeer,
                           PeerDiscoveryService discoveryService,
                           MessageService messageService,
                           VoiceCallService voiceCallService) {

        this.localPeer = localPeer;
        this.discoveryService = discoveryService;
        this.messageService = messageService;
        this.voiceCallService = voiceCallService;

        // 🔥🔥🔥 START DISCOVERY Ở ĐÂY 🔥🔥🔥
        try {
            discoveryService.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        userSettings.setDisplayName(localPeer.getName());
        userSettings.setUsername(localPeer.getId());

        friends.addAll(FriendDAO.loadFriends(localPeer.getId()));

        setupCallbacks();
        loadConversations();
    }


    private void initiateCall(boolean isVideo) {
        if (selectedConversation == null || selectedConversation.isGroup()) {
            showAlert("Cannot make call", "Please select a private contact");
            return;
        }

        Peer target = findPeerById(selectedConversation.getId());
        if (target != null) {
            String type = isVideo ? "Video" : "Voice";
            showNotification(type + " Call",
                    "Calling " + target.getName() + "...");
        }
    }

    @FXML
    private void initiateVoiceCall() {
        initiateCall(false);
    }

    @FXML
    private void initiateVideoCall() {
        initiateCall(true);
    }

    private void showNotification(String title, String message) {

        Platform.runLater(() -> {
            Stage toastStage = new Stage();
            toastStage.initOwner(chatMessagesArea.getScene().getWindow());
            toastStage.setResizable(false);
            toastStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);

            VBox root = new VBox(6);
            root.setPadding(new Insets(12));
            root.setStyle("""
                        -fx-background-color: rgba(32,32,32,0.95);
                        -fx-background-radius: 12;
                        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 12, 0, 0, 4);
                    """);

            Label titleLabel = new Label(title);
            titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");

            Label msgLabel = new Label(message);
            msgLabel.setStyle("-fx-text-fill: #dddddd; -fx-font-size: 13;");
            msgLabel.setWrapText(true);

            root.getChildren().addAll(titleLabel, msgLabel);

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            toastStage.setScene(scene);

            // ===== POSITION (BOTTOM RIGHT) =====
            Stage owner = (Stage) toastStage.getOwner();
            toastStage.setX(owner.getX() + owner.getWidth() - 340);
            toastStage.setY(owner.getY() + owner.getHeight() - 140);

            toastStage.show();

            // ===== AUTO CLOSE =====
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(toastStage::close);
                }
            }, 3000);
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    // ================= CALLBACKS =================
    private void setupCallbacks() {

        messageService.setOnPrivateMessage(msg -> Platform.runLater(() -> {

            // ===== GROUP MESSAGE =====
            if (msg.getType() == Message.Type.GROUP_TEXT) {

                String gid = msg.getGroupId();
                if (gid == null) return;

                Conversation c = conversations.computeIfAbsent(gid, id -> {
                    Group g = GroupDAO.loadGroups(localPeer.getId()).get(id);
                    if (g == null) return null;
                    Conversation cc = new Conversation(g);
                    conversationListView.getItems().add(cc);
                    return cc;
                });

                if (c == null) return;

                c.addMessage(msg);

                if (c == selectedConversation) {
                    displayMessage(msg, false);
                } else {
                    c.incrementUnreadCount();
                }

                conversationListView.refresh();
                return;
            }

            // ===== PRIVATE MESSAGE =====
            Conversation conv = conversations.computeIfAbsent(
                    msg.getFrom(),
                    id -> {
                        Peer p = findPeerById(id);
                        if (p == null) return null;
                        Conversation c = new Conversation(p);
                        conversationListView.getItems().add(c);
                        return c;
                    }
            );

            if (conv == null) return;

            conv.addMessage(msg);

            if (conv == selectedConversation) {
                displayMessage(msg, false);
            } else {
                conv.incrementUnreadCount();
            }

            conversationListView.refresh();
        }));

        messageService.setOnSignalMessage(msg ->
                Platform.runLater(() -> handleSignal(msg)));
    }

    // ================= LOAD =================
    private void loadConversations() {

        conversationListView.getItems().clear();
        conversations.clear();

        // ===== FRIENDS (FROM DB) =====
        for (String fid : friends) {

            // 🔥 reuse nếu đã tồn tại
            Conversation c = conversations.computeIfAbsent(fid, id -> {
                Peer p = findPeerById(id); // online hoặc offline

                return new Conversation(p);
            });

            conversationListView.getItems().add(c);
        }

        // ===== GROUPS (FROM DB) =====
        GroupDAO.loadGroups(localPeer.getId()).forEach((groupId, g) -> {

            // 🔥 KHÔNG tạo mới nếu đã tồn tại
            Conversation c = conversations.computeIfAbsent(groupId, id -> {
                return new Conversation(g);
            });

            if (!conversationListView.getItems().contains(c)) {
                conversationListView.getItems().add(c);
            }
        });
    }

    // ================= OPEN =================
    private void openConversation(Conversation c) {
        if (c == null) return;

        selectedConversation = c;
        c.resetUnreadCount();
        conversationListView.refresh();

        chatHeaderName.setText(c.getName());
        chatHeaderStatus.setText(c.isGroup() ? "Group chat" : "Active");

        chatMessagesArea.getChildren().clear();
        c.getMessages().forEach(m ->
                displayMessage(m, m.getFrom().equals(localPeer.getId())));
    }

    // ================= SEND =================
    private void sendMessage() {
        if (selectedConversation == null) return;

        String text = messageInput.getText().trim();
        if (text.isEmpty()) return;

        if (selectedConversation.isGroup()) {
            sendGroup(text);
        } else {
            sendPrivate(text);
        }

        messageInput.clear();
    }

    private void sendPrivate(String text) {

        Peer target = findPeerById(selectedConversation.getId());
        if (target == null) return;

        Message msg = new Message(
                Message.Type.TEXT,
                localPeer.getId(),
                localPeer.getName(),
                text
        );

        if (replyTarget != null) {
            msg.setReplyToMessageId(replyTarget.getId());
            replyTarget = null;
            chatHeaderStatus.setText("Active");
        }

        messageService.sendPrivateMessage(msg, target);

        selectedConversation.addMessage(msg);
        displayMessage(msg, true);
    }

    private void sendGroup(String text) {
        Group g = selectedConversation.getGroup();

        Message msg = Message.groupText(
                localPeer,
                g.getId(),
                g.getName(),
                text
        );

        for (String memberId : g.getMembers()) {
            if (memberId.equals(localPeer.getId())) continue;

            Peer p = findPeerById(memberId);
            if (p != null) {
                messageService.sendPrivateMessage(msg, p);
            }
        }

        selectedConversation.addMessage(msg);
        displayMessage(msg, true);
    }

    // ================= DISPLAY =================
    private void displayMessage(Message msg, boolean mine) {
// ================= RECALL =================
        if (msg.isRecalled()) {

            Label recalled = new Label("Tin nhắn đã được thu hồi");
            recalled.setStyle("-fx-font-style: italic; -fx-text-fill:#888;");

            HBox wrap = new HBox(recalled);
            wrap.setAlignment(Pos.CENTER);
            wrap.setPadding(new Insets(6));

            chatMessagesArea.getChildren().add(wrap);
            return;
        }

        // ===== FILE =====
        if (msg.getType() == Message.Type.FILE_META) {
            displayFileMessage(msg, mine);
            return;
        }

        // ===== IMAGE =====
        if (msg.getType() == Message.Type.IMAGE) {
            displayImageMessage(msg, mine);
            return;
        }

        VBox bubble = new VBox(2);

        String content = safe(msg.getContent());

        // ================= EMOJI (XỬ LÝ TRƯỚC) =================
        if (content.startsWith(":") && content.endsWith(":")) {

            String file = content.substring(1, content.length() - 1);
            InputStream is = getClass()
                    .getResourceAsStream("/com/p2p/view/emoji/" + file);

            if (is != null) {
                ImageView emoji = new ImageView(new Image(is));
                emoji.setFitWidth(32);
                emoji.setFitHeight(32);

                bubble.getChildren().add(emoji);

                Label time = new Label(msg.getTimestamp());
                time.setStyle("-fx-font-size:10; -fx-text-fill:#888;");
                bubble.getChildren().add(time);

                HBox wrap = new HBox(bubble);
                wrap.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
                wrap.setPadding(new Insets(4));

                chatMessagesArea.getChildren().add(wrap);
                return;
            }
        }

        // ================= TEXT BÌNH THƯỜNG =================
        Label text = new Label(
                mine ? content : msg.getFromName() + ": " + content
        );

        text.setWrapText(true);
        text.setStyle("""
        -fx-background-color: %s;
        -fx-text-fill: %s;
        -fx-padding:8 12;
        -fx-background-radius:16;
    """.formatted(
                mine ? "#0084ff" : "#f0f2f5",
                mine ? "white" : "black"
        ));

        Label time = new Label(msg.getTimestamp());
        time.setStyle("-fx-font-size:10; -fx-text-fill:#888;");

        bubble.getChildren().addAll(text, time);

        if (mine) {
            Label status = new Label(
                    msg.getStatus() == Message.Status.READ ? "✓✓ Read" : "✓ Sent"
            );
            status.setStyle("-fx-font-size:10; -fx-text-fill:#aaa;");
            bubble.getChildren().add(status);
        }

        HBox wrap = new HBox(bubble);
        wrap.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        wrap.setPadding(new Insets(4));

        chatMessagesArea.getChildren().add(wrap);

        // ================= CONTEXT MENU (KHÔNG CHO MESSAGE ĐÃ THU HỒI) =================
        boolean recalled = msg.isRecalled();

        if (!recalled) {
            ContextMenu menu = new ContextMenu();

            MenuItem reply = new MenuItem("Trả lời");
            reply.setOnAction(e -> {
                replyTarget = msg;
                chatHeaderStatus.setText("Replying to: " + content);
            });
            menu.getItems().add(reply);

            if (mine) {
                MenuItem recall = new MenuItem("Thu hồi");
                recall.setOnAction(e -> recallMessage(msg));
                menu.getItems().add(recall);
            }

            bubble.setOnContextMenuRequested(e ->
                    menu.show(bubble, e.getScreenX(), e.getScreenY())
            );
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private void recallMessage(Message msg) {

        Message recall = new Message(
                Message.Type.RECALL,
                localPeer.getId(),
                localPeer.getName()
        );

        recall.setReplyToMessageId(msg.getId());
        recall.setGroupId(msg.getGroupId());
        recall.setTarget(msg.getTarget());

        // ===== SEND =====
        if (msg.getGroupId() != null) {
            for (String memberId : selectedConversation.getGroup().getMembers()) {
                if (memberId.equals(localPeer.getId())) continue;
                Peer p = findPeerById(memberId);
                if (p != null && p.getAddress() != null) {
                    messageService.sendSignalMessage(recall, p);
                }
            }
        } else {
            Peer target = findPeerById(selectedConversation.getId());
            if (target != null && target.getAddress() != null) {
                messageService.sendSignalMessage(recall, target);
            }
        }

        // ===== LOCAL =====
        msg.setRecalled(true);
        refreshChat();
    }

    // ================= SIGNAL =================
    private void handleSignal(Message msg) {

        switch (msg.getType()) {

            case FRIEND_REQUEST -> handleFriendRequest(msg);
            case FRIEND_ACCEPT -> handleFriendAccept(msg);
            case UNFRIEND -> handleUnfriend(msg);

            case GROUP_INVITE -> handleGroupInvite(msg);
            case GROUP_TEXT -> handleGroupText(msg);
            case GROUP_DELETE -> handleGroupDelete(msg);

            case READ_RECEIPT -> handleReadReceipt(msg);
            case FILE_META -> handleFileMeta(msg);

            case RECALL -> handleRecall(msg);

        }
    }

    private void handleRecall(Message recall) {

        Conversation c = recall.getGroupId() != null
                ? conversations.get(recall.getGroupId())
                : conversations.get(recall.getFrom());

        if (c == null) return;

        c.getMessages().stream()
                .filter(m -> m.getId().equals(recall.getReplyToMessageId()))
                .findFirst()
                .ifPresent(m -> m.setRecalled(true));

        refreshChat();
    }

    private void handleFileMeta(Message msg) {

        Conversation c;

        // ===== XÁC ĐỊNH CONVERSATION =====
        if (msg.getGroupId() != null) {
            c = conversations.computeIfAbsent(
                    msg.getGroupId(),
                    id -> new Conversation(GroupDAO.loadGroups(localPeer.getId()).get(id))
            );
        } else {
            c = conversations.computeIfAbsent(
                    msg.getFrom(),
                    id -> new Conversation(findPeerById(id))
            );
        }

        // ===== IMAGE AUTO RECEIVE =====
        if (msg.getType() == Message.Type.IMAGE) {
            try {
                File dir = new File("received_images");
                dir.mkdirs();

                File saveTo = new File(
                        dir,
                        msg.getId() + "_" + new File(msg.getFileName()).getName()
                );

                Peer sender = findPeerById(msg.getFrom());
                if (sender == null || sender.getAddress() == null) return;

                FileTransferService.receiveFile(
                        saveTo,
                        sender.getAddress().getHostAddress(),
                        msg.getFilePort(),
                        msg.getFileSize()
                );

                // 🔥 GẮN ĐƯỜNG DẪN LOCAL CHO UI
                msg.setFileName(saveTo.getAbsolutePath());

            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        // ===== ADD MESSAGE (CỰC KỲ QUAN TRỌNG) =====
        c.addMessage(msg);

        // ===== HIỂN THỊ =====
        boolean mine = msg.getFrom().equals(localPeer.getId());
        displayMessage(msg, mine);

        conversationListView.refresh();
    }

    private void displayFileMessage(Message msg, boolean mine) {

        boolean recalled = msg.isRecalled();

        VBox box = new VBox(4);
        box.setStyle(
                "-fx-background-color:" +
                        (mine ? "#0084ff" : "#f0f2f5") +
                        "; -fx-padding:10; -fx-background-radius:12;"
        );

        // ===== FILE NAME =====
        String fileName = recalled
                ? "File đã bị thu hồi"
                : "📄 " + msg.getFileName();

        Label name = new Label(fileName);
        name.setStyle(mine
                ? "-fx-text-fill:white; -fx-font-weight:bold;"
                : "-fx-text-fill:black; -fx-font-weight:bold;"
        );

        // ===== CLICK TO DOWNLOAD =====
        if (!recalled) {
            name.setOnMouseClicked(e -> {

                FileChooser fc = new FileChooser();
                fc.setInitialFileName(msg.getFileName());

                File saveTo = fc.showSaveDialog(chatMessagesArea.getScene().getWindow());
                if (saveTo == null) return;

                Peer sender = findPeerById(msg.getFrom());
                if (sender == null || sender.getAddress() == null) {
                    showAlert("Download failed", "Sender is offline");
                    return;
                }

                FileTransferService.receiveFile(
                        saveTo,
                        sender.getAddress().getHostAddress(),
                        msg.getFilePort(),
                        msg.getFileSize()
                );
            });
        }

        // ===== FILE SIZE =====
        Label size = new Label(
                recalled ? "" : (msg.getFileSize() / 1024) + " KB"
        );
        size.setStyle(mine ? "-fx-text-fill:white;" : "-fx-text-fill:black;");

        // ===== TIME (THÊM PHẦN NÀY) =====
        Label time = new Label(msg.getTimestamp());
        time.setStyle(
                mine
                        ? "-fx-font-size:10; -fx-text-fill:#e0e0e0;"
                        : "-fx-font-size:10; -fx-text-fill:#888;"
        );

        box.getChildren().addAll(name, size, time);

        HBox wrap = new HBox(box);
        wrap.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        wrap.setPadding(new Insets(4));

        // ===== CONTEXT MENU (RECALL) =====
        if (mine && !recalled) {
            ContextMenu menu = new ContextMenu();
            MenuItem recall = new MenuItem("Thu hồi file");
            recall.setOnAction(e -> recallMessage(msg));
            menu.getItems().add(recall);

            box.setOnContextMenuRequested(e ->
                    menu.show(box, e.getScreenX(), e.getScreenY())
            );
        }

        chatMessagesArea.getChildren().add(wrap);
    }

    private void handleUnfriend(Message msg) {
        String id = msg.getFrom();

        friends.remove(id);
        FriendDAO.deleteFriend(localPeer.getId(), id);

        Conversation c = conversations.remove(id);
        conversationListView.getItems().remove(c);
        chatMessagesArea.getChildren().clear();
    }

    private void handleGroupDelete(Message msg) {
        String gid = msg.getGroupId();

        conversations.remove(gid);
        conversationListView.getItems()
                .removeIf(c -> c.getId().equals(gid));

        GroupDAO.deleteGroup(gid);
        chatMessagesArea.getChildren().clear();
    }

    private void handleReadReceipt(Message msg) {
        Conversation c = conversations.get(msg.getFrom());
        if (c == null) return;

        c.getMessages().stream()
                .filter(m -> m.getId().equals(msg.getReplyToMessageId()))
                .findFirst()
                .ifPresent(m -> m.setStatus(Message.Status.READ));

        conversationListView.refresh();
        refreshChat();
    }

    private void refreshChat() {
        if (selectedConversation == null) return;

        chatMessagesArea.getChildren().clear();

        for (Message m : selectedConversation.getMessages()) {
            boolean mine = m.getFrom().equals(localPeer.getId());

            switch (m.getType()) {
                case FILE_META -> displayFileMessage(m, mine);
                case IMAGE -> displayImageMessage(m, mine);
                default -> displayMessage(m, mine);
            }
        }
    }

    private void handleFriendRequest(Message msg) {
        Peer p = findPeerById(msg.getFrom());
        if (p == null) return;

        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                p.getName() + " wants to be friends");
        a.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                friends.add(p.getId());
                FriendDAO.saveFriend(localPeer.getId(), p.getId());

                Conversation c = new Conversation(p);
                conversations.put(p.getId(), c);
                conversationListView.getItems().add(c);

                messageService.sendSignalMessage(
                        Message.friendAccept(localPeer, p), p);
            }
        });
    }

    private void handleFriendAccept(Message msg) {
        Peer p = findPeerById(msg.getFrom());
        if (p == null) return;

        friends.add(p.getId());
        FriendDAO.saveFriend(localPeer.getId(), p.getId());

        Conversation c = new Conversation(p);
        conversations.put(p.getId(), c);
        conversationListView.getItems().add(c);
    }

    private void handleGroupInvite(Message msg) {

        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "Join group " + msg.getGroupName() + "?");

        a.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {

                Group g = new Group(msg.getGroupName(), msg.getFrom());
                g.setId(msg.getGroupId());

                //GroupDAO.saveGroup(g, msg.getFrom());
                GroupDAO.saveMember(g.getId(), localPeer.getId());

                // 🔥 reload members từ DB
                g.getMembers().addAll(GroupDAO.loadMembers(g.getId()));

                Conversation c = new Conversation(g);
                conversations.put(g.getId(), c);
                conversationListView.getItems().add(c);
            }
        });
    }


    private void handleGroupText(Message msg) {

        if (msg.getGroupId() == null) return;

        Conversation c = conversations.computeIfAbsent(msg.getGroupId(), id -> {

            Group g = GroupDAO.loadGroups(localPeer.getId()).get(id);
            if (g == null) return null;

            Conversation cc = new Conversation(g);
            conversationListView.getItems().add(cc);
            return cc;
        });

        if (c == null) return;

        c.addMessage(msg);

        if (c == selectedConversation) {
            displayMessage(msg, false);
        } else {
            c.incrementUnreadCount();
        }

        conversationListView.refresh();
    }

    // ================= INFO / UNFRIEND =================
    @FXML
    private void showConversationInfo() {
        if (selectedConversation == null) return;

        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setHeaderText(selectedConversation.getName());

        ButtonType close = new ButtonType("Close");
        ButtonType action = selectedConversation.isGroup()
                ? new ButtonType("Leave Group")
                : new ButtonType("Unfriend");

        a.getButtonTypes().setAll(action, close);

        a.showAndWait().ifPresent(bt -> {
            if (bt == action) {
                if (selectedConversation.isGroup()) leaveGroup();
                else unfriend();
            }
        });
    }


    private void unfriend() {
        String id = selectedConversation.getId();
        FriendDAO.deleteFriend(localPeer.getId(), id);
        FriendDAO.deleteFriend(id, localPeer.getId());

        conversations.remove(id);
        friends.remove(id);
        conversationListView.getItems().remove(selectedConversation);
        chatMessagesArea.getChildren().clear();
    }

    private void leaveGroup() {
        conversations.remove(selectedConversation.getId());
        conversationListView.getItems().remove(selectedConversation);
        chatMessagesArea.getChildren().clear();
    }

    // ================= SETTINGS =================
    @FXML
    private void openSettings() {
        Stage s = new Stage();
        s.initModality(Modality.APPLICATION_MODAL);
        s.setScene(new Scene(new Label("Settings (TODO)"), 300, 200));
        s.show();
    }


    // ================= UTIL =================
    private Peer findPeerById(String id) {

        // 1️⃣ Peer ONLINE
        for (Peer p : discoveryService.getPeers()) {
            if (p.getId().equals(id)) {
                return p;
            }
        }

        // 2️⃣ Peer OFFLINE → load từ DB
        Peer offline = new Peer();
        offline.setId(id);

        String username = UserDAO.getUsernameByPeerId(id); // 🔥 ĐÚNG
        offline.setName(username);

        return offline;
    }

    private void filterConversations(String q) {
        if (q.isBlank()) {
            conversationListView.getItems().setAll(conversations.values());
        } else {
            conversationListView.getItems().setAll(
                    conversations.values().stream()
                            .filter(c -> c.getName().toLowerCase().contains(q.toLowerCase()))
                            .toList()
            );
        }
    }

    // ================= CELL =================
    private class ConversationCell extends ListCell<Conversation> {

        @Override
        protected void updateItem(Conversation c, boolean empty) {
            super.updateItem(c, empty);

            if (empty || c == null) {
                setText(null);
                setContextMenu(null);
                return;
            }

            setText(c.getName());

            ContextMenu menu = new ContextMenu();

            if (c.isGroup()) {
                // ===== GROUP =====
                MenuItem leaveGroup = new MenuItem("🚪 Leave group");
                leaveGroup.setOnAction(e -> leaveGroupUI(c));

                MenuItem deleteGroup = new MenuItem("🗑️ Delete group");
                deleteGroup.setOnAction(e -> deleteGroupUI(c));

                menu.getItems().addAll(leaveGroup, deleteGroup);

            } else {
                // ===== PRIVATE =====
                MenuItem unfriend = new MenuItem("❌ Unfriend");
                unfriend.setOnAction(e -> unfriendUI(c));

                menu.getItems().add(unfriend);
            }

            setContextMenu(menu);
        }
    }

    private void unfriendUI(Conversation c) {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Unfriend");
        confirm.setHeaderText("Unfriend " + c.getName() + "?");
        confirm.setContentText("You will no longer be able to chat with this person.");

        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {

                Peer target = c.getPeer();

                // Gửi signal UNFRIEND
                Message m = Message.unfriend(localPeer, target);
                messageService.sendSignalMessage(m, target);

                // Local cleanup
                friends.remove(target.getId());
                FriendDAO.deleteFriend(localPeer.getId(), target.getId());

                conversations.remove(target.getId());
                conversationListView.getItems().remove(c);
                chatMessagesArea.getChildren().clear();
            }
        });
    }

    private void leaveGroupUI(Conversation c) {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Leave Group");
        confirm.setHeaderText("Leave group " + c.getName() + "?");
        confirm.setContentText("You will no longer receive messages from this group.");

        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {

                Group g = c.getGroup();

                // Xóa mình khỏi DB
                GroupDAO.removeMember(g.getId(), localPeer.getId());

                // Local cleanup
                conversations.remove(g.getId());
                conversationListView.getItems().remove(c);
                chatMessagesArea.getChildren().clear();
            }
        });
    }

    private void deleteGroupUI(Conversation c) {

        Group g = c.getGroup();

        // Chỉ owner mới được delete
        if (!g.getOwnerId().equals(localPeer.getId())) {
            showAlert("Permission denied", "Only group owner can delete this group.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Group");
        confirm.setHeaderText("Delete group " + g.getName() + "?");
        confirm.setContentText("All members will be removed from this group.");

        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {

                Message m = Message.groupDelete(localPeer, g.getId());

                // Gửi signal cho member online
                for (Peer p : discoveryService.getPeers()) {
                    if (g.getMembers().contains(p.getId())) {
                        messageService.sendSignalMessage(m, p);
                    }
                }

                // Xóa DB
                GroupDAO.deleteGroup(g.getId());

                // Local cleanup
                conversations.remove(g.getId());
                conversationListView.getItems().remove(c);
                chatMessagesArea.getChildren().clear();
            }
        });
    }

}
