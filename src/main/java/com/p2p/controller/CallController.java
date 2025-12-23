package com.p2p.controller;

import com.p2p.model.Message;
import com.p2p.model.Peer;
import com.p2p.service.VoiceCallService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class CallController {

    @FXML
    private Label callerNameLabel;
    @FXML
    private Label callStatusLabel;
    @FXML
    private Button acceptButton;
    @FXML
    private Button rejectButton;
    @FXML
    private Button endCallButton;

    private Stage stage;
    private Peer caller;
    private Peer localPeer;
    private VoiceCallService voiceCallService;
    private boolean isIncomingCall;
    private boolean isVideoCall;

    public void setContext(Stage stage, Peer caller, Peer localPeer, 
                          VoiceCallService voiceCallService, 
                          boolean isIncomingCall, boolean isVideoCall) {
        this.stage = stage;
        this.caller = caller;
        this.localPeer = localPeer;
        this.voiceCallService = voiceCallService;
        this.isIncomingCall = isIncomingCall;
        this.isVideoCall = isVideoCall;

        updateUI();
    }

    private void updateUI() {
        if (isIncomingCall) {
            callerNameLabel.setText("Incoming " + (isVideoCall ? "Video" : "Voice") + " Call");
            callStatusLabel.setText("From: " + (caller != null ? caller.getName() : "Unknown"));
            acceptButton.setVisible(true);
            rejectButton.setVisible(true);
            endCallButton.setVisible(false);
        } else {
            callerNameLabel.setText("Calling...");
            callStatusLabel.setText(caller != null ? caller.getName() : "Unknown");
            acceptButton.setVisible(false);
            rejectButton.setVisible(false);
            endCallButton.setVisible(true);
        }
    }

    @FXML
    private void acceptCall() {
        if (voiceCallService != null && caller != null) {
            voiceCallService.acceptCall(caller);
            showCallInProgress();
        }
    }

    @FXML
    private void rejectCall() {
        if (voiceCallService != null && caller != null) {
            voiceCallService.rejectCall(caller);
            close();
        }
    }

    @FXML
    private void endCall() {
        if (voiceCallService != null) {
            voiceCallService.endCall();
            close();
        }
    }

    public void showCallInProgress() {
        Platform.runLater(() -> {
            callerNameLabel.setText((isVideoCall ? "Video" : "Voice") + " Call in progress");
            callStatusLabel.setText(caller != null ? caller.getName() : "Unknown");
            acceptButton.setVisible(false);
            rejectButton.setVisible(false);
            endCallButton.setVisible(true);
        });
    }

    public void updateCallStatus(String status) {
        Platform.runLater(() -> {
            callStatusLabel.setText(status);
        });
    }

    public void close() {
        Platform.runLater(() -> {
            if (stage != null) {
                stage.close();
            }
        });
    }
}

