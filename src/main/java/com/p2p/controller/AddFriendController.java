package com.p2p.controller;

import com.p2p.model.Message;
import com.p2p.model.Peer;
import com.p2p.service.MessageService;
import com.p2p.service.PeerDiscoveryService;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddFriendController {

    @FXML private TextField searchField;
    @FXML private ListView<Peer> peerListView;
    @FXML private TextField peerIdField;

    private Peer localPeer;
    private MessageService messageService;
    private Stage stage;

    private FilteredList<Peer> filteredPeers;

    // ================= CONTEXT =================
    public void setContext(Peer localPeer,
                           PeerDiscoveryService discoveryService,
                           MessageService messageService,
                           Stage stage) {

        this.localPeer = localPeer;
        this.messageService = messageService;
        this.stage = stage;

        // ✅ BIND TRỰC TIẾP OBSERVABLE LIST (KHÔNG FILTER TRƯỚC)
        filteredPeers = new FilteredList<>(
                discoveryService.getPeers(),
                p -> true
        );

        peerListView.setItems(filteredPeers);

        // ===== CELL: NAME + PEER ID =====
        peerListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Peer p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) {
                    setText(null);
                } else {
                    setText(
                            p.getName() +
                                    "\nID: " + p.getId()
                    );
                }
            }
        });

        // ===== CLICK → AUTO FILL PEER ID =====
        peerListView.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, old, peer) -> {
                    if (peer != null) {
                        peerIdField.setText(peer.getId());
                    } else {
                        peerIdField.clear();
                    }
                });

        // ===== SEARCH =====
        searchField.textProperty().addListener((obs, old, q) -> {
            String keyword = q == null ? "" : q.toLowerCase();

            filteredPeers.setPredicate(p ->
                    p.getName().toLowerCase().contains(keyword)
                            || p.getId().toLowerCase().contains(keyword)
            );
        });

        // ===== DOUBLE CLICK SEND =====
        peerListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                sendRequest();
            }
        });

        // DEBUG (có thể xóa sau)
        System.out.println("LOCAL PEER ID = " + localPeer.getId());
    }

    // ================= SEND FRIEND REQUEST =================
    @FXML
    private void sendRequest() {

        Peer target = peerListView.getSelectionModel().getSelectedItem();

        if (target == null) {
            showWarning("Please select an online peer");
            return;
        }

        Message m = Message.friendRequest(localPeer, target);
        messageService.sendSignalMessage(m, target);

        showInfo("Friend Request Sent",
                "Request sent to " + target.getName());

        stage.close();
    }

    // ================= CLOSE =================
    @FXML
    private void close() {
        stage.close();
    }

    // ================= UI HELPERS =================
    private void showWarning(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText("Warning");
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
