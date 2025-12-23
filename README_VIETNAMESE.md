# 🚀 HƯỚNG DẪN NHANH - PEERTALK

## ⚡ CHẠY NHANH (3 BƯỚC)

### Bước 1: Setup Database
```bash
mysql -u root -p < database_setup.sql
```

### Bước 2: Chạy Ứng Dụng
```bash
# Windows
RUN_ME.bat

# Hoặc dùng Maven
mvn javafx:run
```

### Bước 3: Đăng Ký và Sử Dụng
- Mở ứng dụng → Đăng ký tài khoản mới
- Hoặc đăng nhập nếu đã có tài khoản

---

## 📋 YÊU CẦU HỆ THỐNG

- ✅ Java 11+ (khuyến nghị Java 17)
- ✅ Maven 3.6+
- ✅ MySQL Server
- ✅ Windows/Linux/Mac

---

## 🔧 KIỂM TRA NHANH

```bash
# Kiểm tra Java
java -version

# Kiểm tra Maven
mvn --version

# Kiểm tra MySQL
mysql --version
```

---

## ❌ LỖI THƯỜNG GẶP

### "JavaFX runtime components are missing"
→ **Giải pháp**: Dùng `mvn javafx:run` không dùng `java -jar`

### "Access denied for user 'root'"
→ **Giải pháp**: Sửa password trong `Database.java`

### "Unknown database 'peertalk'"
→ **Giải pháp**: Chạy `database_setup.sql`

---

## 📖 HƯỚNG DẪN CHI TIẾT

Xem file: **`HUONG_DAN_CHAY.md`**

---

## 🎯 TEST NHANH

1. Chạy 2 terminal
2. Mỗi terminal chạy: `mvn javafx:run`
3. Đăng nhập với 2 tài khoản khác nhau
4. Thêm bạn bè và chat!

---

**Chúc bạn thành công! 🎉**


