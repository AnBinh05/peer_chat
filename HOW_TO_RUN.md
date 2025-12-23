# 🚀 Cách Chạy Ứng Dụng Đúng Cách

## ⚠️ QUAN TRỌNG: Phải dùng Maven để chạy!

**KHÔNG chạy trực tiếp bằng:**
```bash
java -jar target/PeerTalk-1.0-SNAPSHOT.jar  # ❌ SẼ LỖI JavaFX
java com.p2p.P2PApplication                  # ❌ SẼ LỖI JavaFX
```

**PHẢI chạy bằng:**
```bash
mvn javafx:run  # ✅ ĐÚNG CÁCH
```

## 📋 Các Bước Chạy Ứng Dụng

### Bước 1: Kiểm tra yêu cầu

```bash
# Kiểm tra Java (phải >= 11)
java -version

# Kiểm tra Maven
mvn --version
```

### Bước 2: Setup Database (nếu chưa làm)

```bash
mysql -u root -p < database_setup.sql
```

### Bước 3: Chạy ứng dụng

**Cách 1: Dùng script (KHUYẾN NGHỊ)**
```bash
# Windows
run.bat

# Linux/Mac
chmod +x run.sh
./run.sh
```

**Cách 2: Dùng Maven trực tiếp**
```bash
# Build
mvn clean compile

# Chạy
mvn javafx:run
```

**Cách 3: Build và chạy một lệnh**
```bash
mvn clean compile javafx:run
```

## ❌ Tại Sao Không Chạy Được?

### Lỗi: "JavaFX runtime components are missing"

**Nguyên nhân**: Đang chạy trực tiếp bằng `java` command thay vì dùng Maven plugin

**Giải pháp**: 
1. **PHẢI dùng** `mvn javafx:run`
2. **KHÔNG dùng** `java -jar` hoặc `java com.p2p.P2PApplication`

### Lỗi: "Module not found: javafx.controls"

**Nguyên nhân**: JavaFX modules chưa được load

**Giải pháp**: Dùng `mvn javafx:run` - Maven plugin sẽ tự động load modules

## 🔍 Kiểm Tra

### Kiểm tra JavaFX dependencies đã tải chưa:
```bash
mvn dependency:tree | findstr javafx
```

Phải thấy:
- `javafx-controls`
- `javafx-fxml`
- `javafx-base`

### Kiểm tra build thành công:
```bash
mvn clean compile
```

Phải thấy: `BUILD SUCCESS`

## 💡 Tips

1. **Luôn dùng `mvn javafx:run`** - Đây là cách duy nhất đảm bảo JavaFX modules được load đúng
2. **Nếu dùng IDE**: Tạo Maven run configuration với command `javafx:run`
3. **Không build JAR và chạy trực tiếp** - JAR không chứa JavaFX runtime

## 🎯 Test Nhanh

```bash
# 1. Build
mvn clean compile

# 2. Chạy
mvn javafx:run

# 3. Ứng dụng sẽ mở cửa sổ login
```

## 📞 Vẫn Lỗi?

1. Kiểm tra console output để xem lỗi cụ thể
2. Đảm bảo đang dùng `mvn javafx:run` không phải `java`
3. Kiểm tra file `pom.xml` có JavaFX plugin không
4. Xóa cache: `mvn clean` và build lại


