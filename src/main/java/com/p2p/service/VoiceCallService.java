package com.p2p.service;

import com.p2p.model.Message;
import com.p2p.model.Peer;
import com.p2p.util.JsonUtil;
import javafx.application.Platform;

import javax.sound.sampled.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.function.Consumer;

public class VoiceCallService {

    private static final int AUDIO_BUFFER_SIZE = 1024;
    private static final float SAMPLE_RATE = 16000.0f; // 16 kHz
    private static final int SAMPLE_SIZE = 16; // 16-bit
    private static final int CHANNELS = 1; // Mono
    private static final boolean BIG_ENDIAN = false;

    private final Peer localPeer;
    private final AudioFormat audioFormat;

    private DatagramSocket audioSocket;
    private Thread audioSenderThread;
    private Thread audioReceiverThread;

    private volatile boolean inCall = false;
    private volatile boolean isVideoCall = false;
    private Peer callPeer;
    private InetAddress callPeerAddress;
    private int callPeerVoicePort;

    private TargetDataLine microphone;
    private SourceDataLine speaker;

    private Consumer<Message> onIncomingCall;
    private Consumer<Message> onCallAccepted;
    private Consumer<Message> onCallRejected;
    private Consumer<Message> onCallEnded;

    public VoiceCallService(Peer peer) {
        this.localPeer = peer;
        this.audioFormat = new AudioFormat(
                SAMPLE_RATE,
                SAMPLE_SIZE,
                CHANNELS,
                true,
                BIG_ENDIAN
        );
    }

    public void start() {
        try {
            audioSocket = new DatagramSocket(localPeer.getVoicePort());
            System.out.println("🔊 VoiceCallService started on port " + localPeer.getVoicePort());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Handle call signals from MessageService
    public void handleCallSignal(Message msg, InetAddress senderAddress) {
        if (!isCallMessage(msg)) return;
        processCallSignal(msg, senderAddress);
    }

    private boolean isCallMessage(Message msg) {
        return msg.getType() == Message.Type.CALL_REQUEST ||
               msg.getType() == Message.Type.CALL_ACCEPT ||
               msg.getType() == Message.Type.CALL_REJECT ||
               msg.getType() == Message.Type.CALL_END;
    }

    private void processCallSignal(Message msg, InetAddress senderAddress) {
        switch (msg.getType()) {
            case CALL_REQUEST:
                if (!inCall) {
                    Platform.runLater(() -> {
                        if (onIncomingCall != null) {
                            onIncomingCall.accept(msg);
                        }
                    });
                }
                break;

            case CALL_ACCEPT:
                // Only handle if we initiated the call (we're the caller)
                if (inCall && callPeer != null && callPeer.getId().equals(msg.getFrom())) {
                    Platform.runLater(() -> {
                        if (onCallAccepted != null) {
                            onCallAccepted.accept(msg);
                        }
                    });
                    // Start audio streaming if not already started
                    if (microphone == null || !microphone.isOpen()) {
                        startAudioStreaming();
                    }
                }
                break;

            case CALL_REJECT:
                if (inCall && callPeer != null && callPeer.getId().equals(msg.getFrom())) {
                    Platform.runLater(() -> {
                        if (onCallRejected != null) {
                            onCallRejected.accept(msg);
                        }
                    });
                    endCall();
                }
                break;

            case CALL_END:
                if (inCall && callPeer != null && callPeer.getId().equals(msg.getFrom())) {
                    Platform.runLater(() -> {
                        if (onCallEnded != null) {
                            onCallEnded.accept(msg);
                        }
                    });
                    endCall();
                }
                break;
        }
    }

    // ================= INITIATE CALL =================
    public void initiateCall(Peer target, boolean isVideo) {
        if (inCall) {
            System.out.println("⚠️ Already in a call");
            return;
        }

        if (target == null || target.getAddress() == null) {
            System.out.println("❌ Target peer is offline");
            return;
        }

        this.callPeer = target;
        this.callPeerAddress = target.getAddress();
        this.callPeerVoicePort = target.getVoicePort();
        this.isVideoCall = isVideo;
        this.inCall = true;

        Message callRequest = Message.callRequest(localPeer, target, isVideo);
        sendCallSignal(callRequest, target);
    }

    // ================= ACCEPT CALL =================
    public void acceptCall(Peer caller) {
        if (inCall) {
            System.out.println("⚠️ Already in a call");
            return;
        }

        this.callPeer = caller;
        this.callPeerAddress = caller.getAddress();
        this.callPeerVoicePort = caller.getVoicePort();
        this.inCall = true;

        Message accept = Message.callAccept(localPeer, caller);
        sendCallSignal(accept, caller);

        startAudioStreaming();
    }

    // ================= REJECT CALL =================
    public void rejectCall(Peer caller) {
        Message reject = Message.callReject(localPeer, caller);
        sendCallSignal(reject, caller);
    }

    // ================= END CALL =================
    public void endCall() {
        if (!inCall || callPeer == null) return;

        Message end = Message.callEnd(localPeer, callPeer);
        sendCallSignal(end, callPeer);

        stopAudioStreaming();
        inCall = false;
        callPeer = null;
        callPeerAddress = null;
    }

    // ================= SEND CALL SIGNAL =================
    private void sendCallSignal(Message msg, Peer target) {
        try {
            byte[] data = JsonUtil.toJson(msg).getBytes();
            DatagramPacket packet = new DatagramPacket(
                    data, data.length,
                    target.getAddress(), target.getSignalPort()
            );
            new DatagramSocket().send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= AUDIO STREAMING =================
    private void startAudioStreaming() {
        if (audioSocket == null) return;

        try {
            // Initialize microphone
            DataLine.Info micInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
            if (!AudioSystem.isLineSupported(micInfo)) {
                System.err.println("❌ Microphone not supported");
                return;
            }

            microphone = (TargetDataLine) AudioSystem.getLine(micInfo);
            microphone.open(audioFormat);
            microphone.start();

            // Initialize speaker
            DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
            if (!AudioSystem.isLineSupported(speakerInfo)) {
                System.err.println("❌ Speaker not supported");
                return;
            }

            speaker = (SourceDataLine) AudioSystem.getLine(speakerInfo);
            speaker.open(audioFormat);
            speaker.start();

            // Start audio sender thread
            audioSenderThread = new Thread(() -> {
                byte[] buffer = new byte[AUDIO_BUFFER_SIZE];
                while (inCall && microphone.isOpen()) {
                    try {
                        int bytesRead = microphone.read(buffer, 0, buffer.length);
                        if (bytesRead > 0 && callPeerAddress != null) {
                            DatagramPacket packet = new DatagramPacket(
                                    buffer, bytesRead,
                                    callPeerAddress, callPeerVoicePort
                            );
                            audioSocket.send(packet);
                        }
                    } catch (Exception e) {
                        if (inCall) {
                            System.err.println("❌ Audio send error: " + e.getMessage());
                        }
                    }
                }
            }, "AudioSender");

            // Start audio receiver thread
            audioReceiverThread = new Thread(() -> {
                byte[] buffer = new byte[AUDIO_BUFFER_SIZE];
                while (inCall && speaker.isOpen()) {
                    try {
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        audioSocket.receive(packet);

                        // Verify it's from the call peer
                        if (packet.getAddress().equals(callPeerAddress)) {
                            speaker.write(packet.getData(), 0, packet.getLength());
                        }
                    } catch (Exception e) {
                        if (inCall) {
                            System.err.println("❌ Audio receive error: " + e.getMessage());
                        }
                    }
                }
            }, "AudioReceiver");

            audioSenderThread.setDaemon(true);
            audioReceiverThread.setDaemon(true);
            audioSenderThread.start();
            audioReceiverThread.start();

            System.out.println("🎤 Audio streaming started");

        } catch (Exception e) {
            System.err.println("❌ Failed to start audio streaming: " + e.getMessage());
            e.printStackTrace();
            endCall();
        }
    }

    private void stopAudioStreaming() {
        try {
            if (microphone != null && microphone.isOpen()) {
                microphone.stop();
                microphone.close();
                microphone = null;
            }

            if (speaker != null && speaker.isOpen()) {
                speaker.stop();
                speaker.close();
                speaker = null;
            }

            if (audioSenderThread != null) {
                audioSenderThread.interrupt();
            }

            if (audioReceiverThread != null) {
                audioReceiverThread.interrupt();
            }

            System.out.println("🔇 Audio streaming stopped");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= CALLBACKS =================
    public void setOnIncomingCall(Consumer<Message> callback) {
        this.onIncomingCall = callback;
    }

    public void setOnCallAccepted(Consumer<Message> callback) {
        this.onCallAccepted = callback;
    }

    public void setOnCallRejected(Consumer<Message> callback) {
        this.onCallRejected = callback;
    }

    public void setOnCallEnded(Consumer<Message> callback) {
        this.onCallEnded = callback;
    }

    // ================= GETTERS =================
    public boolean isInCall() {
        return inCall;
    }

    public boolean isVideoCall() {
        return isVideoCall;
    }

    public Peer getCallPeer() {
        return callPeer;
    }

    // ================= STOP =================
    public void stop() {
        endCall();
        if (audioSocket != null) {
            audioSocket.close();
        }
        System.out.println("🛑 VoiceCallService stopped");
    }
}
