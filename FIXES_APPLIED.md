# 🔧 Các Lỗi Đã Được Sửa

## 1. ✅ Import Random Class
- **Vấn đề**: Class `Random` được sử dụng nhưng import không rõ ràng
- **Giải pháp**: Đã thêm `import java.util.Random;` vào `MainController.java`
- **File**: `src/main/java/com/p2p/controller/MainController.java`

## 2. ✅ Database Setup Script
- **Vấn đề**: Database chưa được tạo, có thể gây lỗi khi chạy ứng dụng
- **Giải pháp**: Đã tạo file `database_setup.sql` để tạo database và các bảng cần thiết
- **File mới**: 
  - `database_setup.sql` - Script SQL để tạo database
  - `DATABASE_SETUP.md` - Hướng dẫn setup database

## 3. ✅ Compilation Status
- **Trạng thái**: ✅ BUILD SUCCESS
- Tất cả 19 file Java đã được biên dịch thành công
- Chỉ có warning về deprecated API (không ảnh hưởng chức năng)

---

## 🚀 Cách Chạy Ứng Dụng

### Bước 1: Setup Database (QUAN TRỌNG!)
```bash
# Chạy script SQL để tạo database
mysql -u root -p < database_setup.sql
```

Hoặc mở MySQL Workbench và chạy file `database_setup.sql`

### Bước 2: Kiểm tra cấu hình Database
Mở file `src/main/java/com/p2p/db/Database.java` và đảm bảo:
- URL: `jdbc:mysql://localhost:3306/peertalk`
- USER: `root` (hoặc user của bạn)
- PASS: `""` (hoặc password của bạn)

### Bước 3: Chạy ứng dụng
```bash
mvn javafx:run
```

---

## ⚠️ Các Lỗi Có Thể Gặp Khi Chạy

### Lỗi 1: "Access denied for user 'root'@'localhost'"
**Nguyên nhân**: Password MySQL không đúng hoặc chưa được cấu hình
**Giải pháp**: 
1. Kiểm tra password trong `Database.java`
2. Hoặc tạo user mới:
```sql
CREATE USER 'peertalk'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON peertalk.* TO 'peertalk'@'localhost';
FLUSH PRIVILEGES;
```

### Lỗi 2: "Unknown database 'peertalk'"
**Nguyên nhân**: Database chưa được tạo
**Giải pháp**: Chạy lại `database_setup.sql`

### Lỗi 3: "Table 'peertalk.users' doesn't exist"
**Nguyên nhân**: Các bảng chưa được tạo
**Giải pháp**: Chạy lại `database_setup.sql`

### Lỗi 4: "Address already in use"
**Nguyên nhân**: Port đã được sử dụng bởi instance khác
**Giải pháp**: Đóng instance cũ hoặc đợi vài giây

### Lỗi 5: JavaFX runtime components missing
**Nguyên nhân**: JavaFX không được cấu hình đúng
**Giải pháp**: 
```bash
# Đảm bảo dùng Maven plugin
mvn javafx:run
```

---

## 📋 Checklist Trước Khi Chạy

- [ ] MySQL Server đang chạy
- [ ] Database `peertalk` đã được tạo
- [ ] Các bảng (users, friends, groups, group_members) đã được tạo
- [ ] Cấu hình database trong `Database.java` đúng
- [ ] Đã build project: `mvn clean compile` thành công
- [ ] JavaFX dependencies đã được tải (Maven tự động)

---

## 🎯 Test Ứng Dụng

1. **Đăng ký tài khoản mới**:
   - Nhập username và password
   - Click "Register"
   - Đăng nhập với thông tin vừa tạo

2. **Test P2P Discovery**:
   - Chạy 2 instance trên 2 terminal khác nhau
   - Đăng nhập với 2 tài khoản khác nhau
   - Đợi vài giây, các peer sẽ tự động phát hiện nhau

3. **Test Chat**:
   - Thêm bạn bè
   - Gửi tin nhắn
   - Kiểm tra tin nhắn được nhận

---

## 📞 Hỗ Trợ

Nếu vẫn gặp lỗi, kiểm tra:
1. Console output để xem lỗi cụ thể
2. File `debug.log` (nếu có)
3. MySQL error log
4. JavaFX error messages


