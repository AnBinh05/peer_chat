package com.p2p.dao;

import com.p2p.db.Database;
import com.p2p.model.Group;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * GroupDAO
 * Quản lý bảng groups và group_members
 */
public class GroupDAO {

    // ================= SAVE GROUP =================
    public static void saveGroup(Group g, String ownerId) {
        try (Connection c = Database.getConnection()) {

            PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO groups(id, name, owner) VALUES (?,?,?)"
            );
            ps.setString(1, g.getId());
            ps.setString(2, g.getName());
            ps.setString(3, ownerId);
            ps.executeUpdate();

            // Owner cũng là member
            saveMember(g.getId(), ownerId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= SAVE MEMBER =================
    public static void saveMember(String groupId, String memberId) {
        try (Connection c = Database.getConnection()) {
            PreparedStatement ps = c.prepareStatement(
                    "INSERT IGNORE INTO group_members(group_id, member) VALUES (?,?)"
            );

            ps.setString(1, groupId);
            ps.setString(2, memberId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= LOAD GROUP =================
    public static Map<String, Group> loadGroups(String ownerId) {

        Map<String, Group> groups = new HashMap<>();

        try (Connection c = Database.getConnection()) {

            PreparedStatement ps = c.prepareStatement(
                    "SELECT g.id, g.name, g.owner " +
                            "FROM groups g " +
                            "JOIN group_members gm ON g.id = gm.group_id " +
                            "WHERE gm.member = ?"
            );
            ps.setString(1, ownerId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Group g = new Group(rs.getString("name"), ownerId);
                g.setId(rs.getString("id"));

                // Load members
                g.getMembers().addAll(loadMembers(g.getId()));

                groups.put(g.getId(), g);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return groups;
    }

    // ================= LOAD MEMBERS =================
    public static Set<String> loadMembers(String groupId) {

        Set<String> members = new HashSet<>();

        try (Connection c = Database.getConnection()) {
            PreparedStatement ps = c.prepareStatement(
                    "SELECT member FROM group_members WHERE group_id=?"
            );
            ps.setString(1, groupId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                members.add(rs.getString("member"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return members;
    }// ================= DELETE GROUP =================
    public static void deleteGroup(String groupId) {
        try (Connection c = Database.getConnection()) {

            // 1️⃣ Xóa tất cả member của group
            PreparedStatement ps1 = c.prepareStatement(
                    "DELETE FROM group_members WHERE group_id=?"
            );
            ps1.setString(1, groupId);
            ps1.executeUpdate();

            // 2️⃣ Xóa group
            PreparedStatement ps2 = c.prepareStatement(
                    "DELETE FROM groups WHERE id=?"
            );
            ps2.setString(1, groupId);
            ps2.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // ================= REMOVE MEMBER =================
    public static void removeMember(String groupId, String memberId) {
        try (Connection c = Database.getConnection()) {

            PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM group_members WHERE group_id=? AND member=?"
            );
            ps.setString(1, groupId);
            ps.setString(2, memberId);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
