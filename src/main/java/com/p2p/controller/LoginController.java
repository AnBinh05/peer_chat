package com.p2p.controller;

import com.p2p.P2PApplication;
import com.p2p.dao.UserDAO;
import com.p2p.model.Peer;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.UUID;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    // ==========================
    //        LOGIN
    // ==========================
    @FXML
    private void onLogin() {

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please fill all fields!");
            return;
        }

        // 🔥 LOGIN → LẤY peerId
        String peerId = UserDAO.login(username, password);

        if (peerId == null) {
            errorLabel.setText("Wrong username or password!");
            return;
        }

        // 🔥 TẠO LOCAL PEER ĐÚNG CHUẨN
        Peer localPeer = new Peer();
        localPeer.setId(peerId);
        localPeer.setName(username);

        // 🔥 SET VÀO APP
        P2PApplication.setLocalPeer(localPeer);
        P2PApplication.startMainApp();
    }

    // ==========================
    //       REGISTER
    // ==========================
    @FXML
    private void onRegister() {

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please fill all fields!");
            return;
        }

        // 🔥 TẠO peerId CỐ ĐỊNH
        String peerId = UUID.randomUUID().toString();

        boolean ok = UserDAO.register(peerId, username, password);

        if (ok) {
            errorLabel.setText("Account created! You can log in now.");
        } else {
            errorLabel.setText("Username already exists!");
        }
    }
}
