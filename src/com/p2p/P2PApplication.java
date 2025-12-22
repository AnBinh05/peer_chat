package com.p2p;

import com.p2p.controller.MainController;
import com.p2p.model.Peer;
import com.p2p.service.MessageService;
import com.p2p.service.PeerDiscoveryService;
import com.p2p.service.VoiceCallService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.InetAddress;
import java.util.Random;

public class P2PApplication extends Application {

    private static Stage mainStage;

    // 🔥 CHỈ GIỮ localPeer
    private static Peer localPeer;

    private static PeerDiscoveryService discoveryService;
    private static MessageService messageService;
    private static VoiceCallService voiceCallService;

    // =============================
    // SET LOCAL PEER (TỪ LOGIN)
    // =============================
    public static void setLocalPeer(Peer peer) {
        localPeer = peer;
    }

    public static Peer getLocalPeer() {
        return localPeer;
    }

    // =============================
    // LOGIN STAGE
    // =============================
    @Override
    public void start(Stage stage) throws Exception {
        mainStage = stage;

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/p2p/view/login.fxml")
        );

        Scene loginScene = new Scene(loader.load());

        stage.setTitle("PeerTalk - Login");
        stage.setScene(loginScene);
        stage.setResizable(false);
        stage.show();
    }

    // =============================
    // MAIN APP
    // =============================
    public static void startMainApp() {
        Platform.runLater(() -> {
            try {
                // 🔥 1️⃣ GẮN NETWORK INFO CHO PEER CÓ SẴN
                attachNetworkInfo(localPeer);

                // 🔥 2️⃣ START SERVICES
                startServices();

                // 🔥 3️⃣ LOAD MAIN VIEW
                FXMLLoader loader = new FXMLLoader(
                        P2PApplication.class.getResource("/com/p2p/view/MainView.fxml")
                );

                Scene mainScene = new Scene(loader.load());

                // 🔥 4️⃣ INJECT CONTEXT
                MainController controller = loader.getController();
                controller.setContext(
                        localPeer,
                        discoveryService,
                        messageService,
                        voiceCallService
                );

                // 🔥 5️⃣ SHOW STAGE
                mainStage.setScene(mainScene);
                mainStage.setTitle("PeerTalk - " + localPeer.getName());
                mainStage.setResizable(true);
                mainStage.show();

                // 🔥 6️⃣ SHUTDOWN HOOK
                mainStage.setOnCloseRequest(e -> {
                    shutdown();
                    Platform.exit();
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // =============================
    // ATTACH NETWORK INFO (KHÔNG TẠO PEER)
    // =============================
    private static void attachNetworkInfo(Peer peer) throws Exception {

        InetAddress ip = InetAddress.getLocalHost();

        int basePort = 52000 + new Random().nextInt(1000);

        peer.setAddress(ip);
        peer.setTextPort(basePort);
        peer.setVoicePort(basePort + 1);
        peer.setSignalPort(basePort + 2);

        System.out.println("🚀 Peer ports:");
        System.out.println("   text   = " + peer.getTextPort());
        System.out.println("   voice  = " + peer.getVoicePort());
        System.out.println("   signal = " + peer.getSignalPort());
    }

    // =============================
    // START SERVICES
    // =============================
    private static void startServices() throws Exception {

        discoveryService = new PeerDiscoveryService(localPeer);
        discoveryService.start();

        messageService = new MessageService(localPeer);
        messageService.start();

        voiceCallService = new VoiceCallService(localPeer);
        voiceCallService.start();
    }

    // =============================
    // SHUTDOWN
    // =============================
    private static void shutdown() {
        try {
            if (voiceCallService != null) voiceCallService.stop();
            if (messageService != null) messageService.stop();
            if (discoveryService != null) discoveryService.stop();
            System.out.println("🛑 PeerTalk shutdown complete");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
