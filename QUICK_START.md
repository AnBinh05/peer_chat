# 🚀 Quick Start Guide

## Bước 1: Setup Database (QUAN TRỌNG!)

```bash
# Chạy script SQL
mysql -u root -p < database_setup.sql
```

Hoặc mở MySQL Workbench và chạy file `database_setup.sql`

## Bước 2: Chạy Ứng Dụng

### Windows:
```bash
run.bat
```

### Linux/Mac:
```bash
chmod +x run.sh
./run.sh
```

### Hoặc dùng Maven trực tiếp:
```bash
mvn javafx:run
```

## ⚠️ Nếu Gặp Lỗi "JavaFX runtime components are missing"

### Giải pháp nhanh:

1. **Xóa cache và build lại**:
   ```bash
   mvn clean
   mvn install
   mvn javafx:run
   ```

2. **Kiểm tra Java version**:
   ```bash
   java -version
   ```
   Phải là Java 11 hoặc cao hơn

3. **Nếu dùng IDE (IntelliJ)**:
   - File → Invalidate Caches / Restart
   - Right-click `pom.xml` → Maven → Reload Project
   - Tạo Maven run configuration với command: `javafx:run`

## 📋 Checklist

- [ ] MySQL đang chạy
- [ ] Database `peertalk` đã được tạo
- [ ] Java 11+ đã cài đặt
- [ ] Maven 3.6+ đã cài đặt
- [ ] Đã chạy `mvn clean install` thành công

## 🎯 Test Nhanh

1. Chạy ứng dụng: `mvn javafx:run`
2. Đăng ký tài khoản mới
3. Đăng nhập
4. Ứng dụng sẽ tự động phát hiện các peer khác trong mạng LAN

## 📞 Cần Giúp Đỡ?

Xem các file hướng dẫn chi tiết:
- `JAVAFX_FIX.md` - Fix lỗi JavaFX
- `DATABASE_SETUP.md` - Setup database
- `FIXES_APPLIED.md` - Các lỗi đã sửa


