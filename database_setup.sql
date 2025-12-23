-- ============================================
-- PEERTALK DATABASE SETUP SCRIPT
-- ============================================
-- Chạy script này trong MySQL để tạo database và các bảng cần thiết
-- 
-- Cách chạy:
-- 1. Mở MySQL Command Line hoặc MySQL Workbench
-- 2. Chạy: mysql -u root -p < database_setup.sql
--    hoặc copy/paste nội dung này vào MySQL Workbench
-- ============================================

-- Tạo database
CREATE DATABASE IF NOT EXISTS peertalk 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE peertalk;

-- ============================================
-- Bảng USERS
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    peer_id VARCHAR(255) PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Bảng FRIENDS
-- ============================================
CREATE TABLE IF NOT EXISTS friends (
    owner VARCHAR(255) NOT NULL,
    friend VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (owner, friend),
    FOREIGN KEY (owner) REFERENCES users(peer_id) ON DELETE CASCADE,
    FOREIGN KEY (friend) REFERENCES users(peer_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Bảng GROUPS
-- ============================================
CREATE TABLE IF NOT EXISTS groups (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    owner VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner) REFERENCES users(peer_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Bảng GROUP_MEMBERS
-- ============================================
CREATE TABLE IF NOT EXISTS group_members (
    group_id VARCHAR(255) NOT NULL,
    member VARCHAR(255) NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (group_id, member),
    FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    FOREIGN KEY (member) REFERENCES users(peer_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TẠO INDEX ĐỂ TỐI ƯU HIỆU SUẤT
-- ============================================
CREATE INDEX IF NOT EXISTS idx_friends_owner ON friends(owner);
CREATE INDEX IF NOT EXISTS idx_friends_friend ON friends(friend);
CREATE INDEX IF NOT EXISTS idx_group_members_group ON group_members(group_id);
CREATE INDEX IF NOT EXISTS idx_group_members_member ON group_members(member);

-- ============================================
-- HOÀN TẤT
-- ============================================
SELECT 'Database setup completed successfully!' AS Status;


