package com.p2p.dao;

import com.p2p.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

/**
 * FriendDAO
 * Quản lý bảng friends
 *
 * Schema chuẩn:
 * friends(owner TEXT, friend TEXT)
 */
public class FriendDAO {

    // ================= SAVE FRIEND =================
    public static void saveFriend(String owner, String friend) {
        try (Connection c = Database.getConnection()) {
            PreparedStatement ps = c.prepareStatement(
                    "INSERT IGNORE INTO friends(owner, friend) VALUES (?, ?)"
            );

            ps.setString(1, owner);
            ps.setString(2, friend);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= DELETE FRIEND =================
    public static void deleteFriend(String owner, String friend) {
        try (Connection c = Database.getConnection()) {
            PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM friends WHERE owner=? AND friend=?"
            );
            ps.setString(1, owner);
            ps.setString(2, friend);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= LOAD FRIENDS =================
    public static Set<String> loadFriends(String owner) {

        Set<String> friends = new HashSet<>();

        try (Connection c = Database.getConnection()) {
            PreparedStatement ps = c.prepareStatement(
                    "SELECT friend FROM friends WHERE owner=?"
            );
            ps.setString(1, owner);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                friends.add(rs.getString("friend"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return friends;
    }
}
