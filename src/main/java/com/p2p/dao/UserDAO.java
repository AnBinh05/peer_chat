package com.p2p.dao;

import com.p2p.db.Database;
import java.sql.*;

public class UserDAO {

    // ================= REGISTER =================
    public static boolean register(String peerId, String username, String password) {

        String sql = """
            INSERT INTO users(peer_id, username, password)
            VALUES (?, ?, ?)
        """;

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, peerId);
            ps.setString(2, username);
            ps.setString(3, password);

            ps.executeUpdate();
            return true;

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("❌ Username already exists");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ================= LOGIN =================
    public static String login(String username, String password) {

        String sql = """
            SELECT peer_id FROM users
            WHERE username=? AND password=?
        """;

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("peer_id"); // 🔥 TRẢ VỀ peer_id
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ================= GET USERNAME =================
    public static String getUsernameByPeerId(String peerId) {

        String sql = "SELECT username FROM users WHERE peer_id=?";

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, peerId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("username");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return peerId; // fallback
    }
}
