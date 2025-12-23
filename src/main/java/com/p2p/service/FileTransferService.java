package com.p2p.service;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class FileTransferService {

    private static final int BUFFER_SIZE = 8192; // 8KB

    // ================= SEND =================
    public static void sendFile(File file, int port) {

        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(port)) {

                System.out.println("📤 Waiting receiver on port " + port + "...");
                try (
                        Socket socket = server.accept();
                        FileInputStream fis = new FileInputStream(file);
                        BufferedInputStream bis = new BufferedInputStream(fis);
                        OutputStream out = socket.getOutputStream()
                ) {
                    byte[] buffer = new byte[BUFFER_SIZE];
                    long sent = 0;
                    int read;

                    while ((read = bis.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                        sent += read;
                    }

                    out.flush();
                    System.out.println("✅ File sent: " + file.getName() +
                            " (" + sent / 1024 + " KB)");
                }

            } catch (Exception e) {
                System.err.println("❌ Send file failed");
                e.printStackTrace();
            }
        }, "FileSender-" + file.getName()).start();
    }

    // ================= RECEIVE =================
    public static void receiveFile(
            File saveTo,
            String host,
            int port,
            long fileSize
    ) {

        new Thread(() -> {
            try (
                    Socket socket = new Socket(host, port);
                    InputStream in = socket.getInputStream();
                    BufferedInputStream bis = new BufferedInputStream(in);
                    FileOutputStream fos = new FileOutputStream(saveTo);
                    BufferedOutputStream bos = new BufferedOutputStream(fos)
            ) {
                byte[] buffer = new byte[BUFFER_SIZE];
                long received = 0;
                int read;

                while (received < fileSize &&
                        (read = bis.read(buffer)) != -1) {

                    bos.write(buffer, 0, read);
                    received += read;
                }

                bos.flush();
                System.out.println("✅ File received: " + saveTo.getName() +
                        " (" + received / 1024 + " KB)");

            } catch (Exception e) {
                System.err.println("❌ Receive file failed");
                e.printStackTrace();
            }
        }, "FileReceiver-" + saveTo.getName()).start();
    }
}
