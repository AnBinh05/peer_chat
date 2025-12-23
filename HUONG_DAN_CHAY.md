# 📖 HƯỚNG DẪN CHẠY ỨNG DỤNG PEERTALK

## 🎯 Tổng Quan

Đây là ứng dụng P2P Chat sử dụng JavaFX. Để chạy được, bạn cần:
1. ✅ Java 11+ đã cài đặt
2. ✅ Maven 3.6+ đã cài đặt  
3. ✅ MySQL đã cài đặt và đang chạy
4. ✅ Database đã được tạo

---

## 📋 BƯỚC 1: KIỂM TRA YÊU CẦU

### 1.1. Kiểm tra Java

Mở **PowerShell** hoặc **CMD** và chạy:

```bash
java -version
```

**Kết quả mong đợi**: Phải hiển thị Java 11 hoặc cao hơn (ví dụ: `java version "17.0.8"`)

**Nếu chưa có Java**: 
- Tải Java từ: https://adoptium.net/
- Cài đặt và thêm vào PATH

### 1.2. Kiểm tra Maven

```bash
mvn --version
```

**Kết quả mong đợi**: Phải hiển thị Maven 3.6 hoặc cao hơn

**Nếu chưa có Maven**:
- Tải Maven từ: https://maven.apache.org/download.cgi
- Giải nén và thêm vào PATH

### 1.3. Kiểm tra MySQL

```bash
mysql --version
```

**Hoặc kiểm tra MySQL đang chạy**:
- Windows: Mở **Services** (services.msc) → Tìm **MySQL** → Phải ở trạng thái **Running**

**Nếu chưa có MySQL**:
- Tải MySQL từ: https://dev.mysql.com/downloads/installer/
- Cài đặt MySQL Server

---

## 🗄️ BƯỚC 2: SETUP DATABASE

### 2.1. Tạo Database và các bảng

Mở **MySQL Command Line** hoặc **MySQL Workbench** và chạy:

**Cách 1: Dùng Command Line**
```bash
mysql -u root -p < database_setup.sql
```

Khi được hỏi, nhập password MySQL của bạn (nếu có password).

**Cách 2: Dùng MySQL Workbench**
1. Mở MySQL Workbench
2. Kết nối đến MySQL server
3. File → Open SQL Script → Chọn file `database_setup.sql`
4. Click nút **Execute** (⚡) hoặc nhấn `Ctrl+Shift+Enter`

### 2.2. Kiểm tra Database đã tạo

```bash
mysql -u root -p
```

Sau đó chạy:
```sql
USE peertalk;
SHOW TABLES;
```

Phải thấy 4 bảng:
- `users`
- `friends`
- `groups`
- `group_members`

### 2.3. Cấu hình Database trong code (nếu cần)

Mở file: `src/main/java/com/p2p/db/Database.java`

Kiểm tra và sửa nếu cần:
```java
private static final String USER = "root";        // Đổi nếu dùng user khác
private static final String PASS = "";            // Đổi nếu có password
```

---

## 🚀 BƯỚC 3: CHẠY ỨNG DỤNG

### Cách 1: Dùng Script (Dễ nhất) ⭐

**Windows:**
```bash
RUN_ME.bat
```

Hoặc:
```bash
run.bat
```

**Linux/Mac:**
```bash
chmod +x run.sh
./run.sh
```

### Cách 2: Dùng Maven Trực Tiếp

Mở **PowerShell** hoặc **CMD** trong thư mục project:

```bash
# Bước 1: Build project
mvn clean compile

# Bước 2: Chạy ứng dụng
mvn javafx:run
```

### Cách 3: Build và Chạy Một Lệnh

```bash
mvn clean compile javafx:run
```

---

## ✅ KIỂM TRA ỨNG DỤNG ĐÃ CHẠY

Sau khi chạy lệnh, bạn sẽ thấy:

1. **Console output**:
   ```
   ✅ Peer Discovery started
   📡 MessageService started
   🔊 VoiceCallService started
   ```

2. **Cửa sổ ứng dụng** sẽ mở với giao diện **Login**

3. **Test đăng ký/đăng nhập**:
   - Nhập username và password
   - Click **Register** để tạo tài khoản mới
   - Hoặc đăng nhập nếu đã có tài khoản

---

## ⚠️ XỬ LÝ LỖI

### Lỗi 1: "JavaFX runtime components are missing"

**Nguyên nhân**: Đang chạy trực tiếp bằng `java` thay vì Maven

**Giải pháp**: 
- ✅ **PHẢI dùng**: `mvn javafx:run`
- ❌ **KHÔNG dùng**: `java -jar` hoặc `java com.p2p.P2PApplication`

### Lỗi 2: "Access denied for user 'root'@'localhost'"

**Nguyên nhân**: Password MySQL không đúng

**Giải pháp**: 
1. Mở `src/main/java/com/p2p/db/Database.java`
2. Sửa `PASS = "your_password"` (thay `your_password` bằng password MySQL của bạn)

### Lỗi 3: "Unknown database 'peertalk'"

**Nguyên nhân**: Database chưa được tạo

**Giải pháp**: Chạy lại `database_setup.sql` (xem Bước 2)

### Lỗi 4: "Address already in use"

**Nguyên nhân**: Port đã được sử dụng bởi instance khác

**Giải pháp**: 
- Đóng instance cũ
- Hoặc đợi vài giây rồi chạy lại

### Lỗi 5: "Insufficient memory"

**Nguyên nhân**: RAM không đủ

**Giải pháp**: 
- Đóng các ứng dụng khác
- Hoặc tăng memory cho IntelliJ (nếu dùng IDE)
- Hoặc chạy từ command line thay vì IDE

---

## 🎯 TEST ỨNG DỤNG

### Test 1: Đăng ký và Đăng nhập

1. Chạy ứng dụng
2. Nhập username: `test1`
3. Nhập password: `123456`
4. Click **Register**
5. Sau đó đăng nhập với thông tin vừa tạo

### Test 2: Chạy Nhiều Instance (P2P)

1. **Mở 2 terminal** khác nhau
2. **Terminal 1**: Chạy `mvn javafx:run` → Đăng nhập với `test1`
3. **Terminal 2**: Chạy `mvn javafx:run` → Đăng nhập với `test2`
4. Đợi vài giây → Hai peer sẽ tự động phát hiện nhau
5. Thêm bạn bè và gửi tin nhắn

### Test 3: Tính Năng Chat

1. Đăng nhập với 2 tài khoản khác nhau (2 terminal)
2. Click nút **Add Friend** (+)
3. Tìm peer khác và gửi lời mời kết bạn
4. Chấp nhận lời mời
5. Gửi tin nhắn cho nhau

---

## 📝 TÓM TẮT CÁC LỆNH QUAN TRỌNG

```bash
# 1. Setup database
mysql -u root -p < database_setup.sql

# 2. Build project
mvn clean compile

# 3. Chạy ứng dụng
mvn javafx:run

# Hoặc dùng script
RUN_ME.bat
```

---

## 📞 CẦN GIÚP ĐỠ?

1. Kiểm tra console output để xem lỗi cụ thể
2. Xem các file hướng dẫn:
   - `HOW_TO_RUN.md` - Hướng dẫn chạy chi tiết
   - `JAVAFX_FIX.md` - Fix lỗi JavaFX
   - `DATABASE_SETUP.md` - Setup database
   - `INTELLIJ_MEMORY_FIX.md` - Fix lỗi memory

3. Đảm bảo:
   - ✅ Java 11+ đã cài
   - ✅ Maven 3.6+ đã cài
   - ✅ MySQL đang chạy
   - ✅ Database đã được tạo
   - ✅ Dùng `mvn javafx:run` không phải `java`

---

## 🎉 CHÚC BẠN THÀNH CÔNG!

Nếu vẫn gặp lỗi, hãy gửi thông báo lỗi cụ thể để được hỗ trợ.


