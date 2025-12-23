# 🗄️ Hướng Dẫn Setup Database

## Yêu cầu
- MySQL Server đã được cài đặt và đang chạy
- MySQL user: `root` (hoặc user khác có quyền tạo database)
- MySQL password: (để trống hoặc password của bạn)

## Các bước setup

### Bước 1: Kiểm tra MySQL đang chạy
```bash
# Windows
net start MySQL80

# Hoặc kiểm tra trong Services
```

### Bước 2: Tạo database và các bảng

**Cách 1: Sử dụng MySQL Command Line**
```bash
mysql -u root -p < database_setup.sql
```

**Cách 2: Sử dụng MySQL Workbench**
1. Mở MySQL Workbench
2. Kết nối đến MySQL server
3. File → Open SQL Script → chọn `database_setup.sql`
4. Click "Execute" (⚡)

**Cách 3: Chạy từng lệnh trong MySQL Command Line**
```sql
mysql -u root -p
```
Sau đó copy/paste nội dung từ `database_setup.sql`

### Bước 3: Kiểm tra database đã được tạo
```sql
USE peertalk;
SHOW TABLES;
```

Bạn sẽ thấy 4 bảng:
- `users`
- `friends`
- `groups`
- `group_members`

### Bước 4: Cấu hình trong code (nếu cần)

File `src/main/java/com/p2p/db/Database.java`:
```java
private static final String URL = 
    "jdbc:mysql://localhost:3306/peertalk?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC";
private static final String USER = "root";
private static final String PASS = "";  // Đổi nếu có password
```

## Kiểm tra kết nối

Sau khi setup xong, chạy ứng dụng:
```bash
mvn javafx:run
```

Nếu có lỗi kết nối database, kiểm tra:
1. MySQL đang chạy
2. Database `peertalk` đã được tạo
3. Username/password trong `Database.java` đúng
4. Port MySQL (mặc định 3306)

## Troubleshooting

### Lỗi: "Access denied for user 'root'@'localhost'"
- Kiểm tra password MySQL
- Hoặc tạo user mới:
```sql
CREATE USER 'peertalk'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON peertalk.* TO 'peertalk'@'localhost';
FLUSH PRIVILEGES;
```

### Lỗi: "Unknown database 'peertalk'"
- Chạy lại script `database_setup.sql`

### Lỗi: "Table 'peertalk.users' doesn't exist"
- Chạy lại script `database_setup.sql`
- Hoặc tạo thủ công từng bảng


