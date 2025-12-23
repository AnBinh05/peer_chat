# 🔧 Fix Lỗi: JavaFX Runtime Components Missing

## ✅ Đã Sửa

1. **Thêm `javafx-base` dependency** vào `pom.xml`
2. **Thêm Maven Compiler Plugin** với cấu hình đúng
3. **Cập nhật JavaFX Maven Plugin** với execution configuration

## 🚀 Cách Chạy Ứng Dụng

### Cách 1: Sử dụng Maven (KHUYẾN NGHỊ)

```bash
mvn javafx:run
```

Hoặc sử dụng script:
- **Windows**: `run.bat`
- **Linux/Mac**: `chmod +x run.sh && ./run.sh`

### Cách 2: Chạy từ IDE (IntelliJ IDEA)

#### Option A: Sử dụng Maven Run Configuration
1. Mở **Run/Debug Configurations**
2. Click **+** → **Maven**
3. Đặt tên: `PeerTalk`
4. **Working directory**: `$PROJECT_DIR$`
5. **Command line**: `javafx:run`
6. Click **OK** và chạy

#### Option B: Tạo Java Application Configuration
1. Mở **Run/Debug Configurations**
2. Click **+** → **Application**
3. Đặt tên: `PeerTalk`
4. **Main class**: `com.p2p.P2PApplication`
5. **VM options**:
   ```
   --module-path %PATH_TO_FX% --add-modules javafx.controls,javafx.fxml
   ```
   (Thay `%PATH_TO_FX%` bằng đường dẫn đến JavaFX SDK lib folder)
6. **Use classpath of module**: `PeerTalk.main`
7. Click **OK** và chạy

### Cách 3: Chạy JAR File (Sau khi build)

```bash
# Build JAR với JavaFX
mvn clean package

# Chạy JAR (cần JavaFX trên classpath)
java --module-path /path/to/javafx-sdk/lib \
     --add-modules javafx.controls,javafx.fxml \
     -cp target/PeerTalk-1.0-SNAPSHOT.jar:target/lib/* \
     com.p2p.P2PApplication
```

## ⚠️ Nếu Vẫn Gặp Lỗi

### Lỗi 1: "Error: JavaFX runtime components are missing"

**Nguyên nhân**: JavaFX modules không được load đúng cách

**Giải pháp**:

1. **Kiểm tra Java version**:
   ```bash
   java -version
   ```
   Phải là Java 11 hoặc cao hơn

2. **Kiểm tra Maven đã tải JavaFX dependencies**:
   ```bash
   mvn dependency:tree | grep javafx
   ```
   Phải thấy:
   - `javafx-controls`
   - `javafx-fxml`
   - `javafx-base`

3. **Xóa cache và tải lại dependencies**:
   ```bash
   mvn clean
   mvn dependency:resolve
   mvn compile
   ```

4. **Nếu dùng IDE, đảm bảo IDE sử dụng Maven để build**:
   - IntelliJ: File → Settings → Build → Build Tools → Maven
   - Đảm bảo "Use Maven" được chọn

### Lỗi 2: "Module not found: javafx.controls"

**Giải pháp**: Thêm VM options khi chạy:
```
--add-modules javafx.controls,javafx.fxml,javafx.base
```

### Lỗi 3: Chạy từ IDE nhưng không chạy được

**Giải pháp**: 
1. Đảm bảo **Project SDK** là Java 11+
2. Đảm bảo **Language level** là 11+
3. **Invalidate Caches**: File → Invalidate Caches / Restart
4. **Reload Maven Project**: Right-click `pom.xml` → Maven → Reload Project

## 📋 Checklist

Trước khi chạy, đảm bảo:
- [ ] Java 11+ đã được cài đặt
- [ ] Maven 3.6+ đã được cài đặt
- [ ] Đã chạy `mvn clean install` thành công
- [ ] JavaFX dependencies đã được tải (kiểm tra `~/.m2/repository/org/openjfx/`)
- [ ] Database đã được setup (xem `DATABASE_SETUP.md`)

## 🔍 Kiểm Tra Cấu Hình

### Kiểm tra pom.xml có đúng không:
```xml
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>17.0.2</version>
</dependency>
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-fxml</artifactId>
    <version>17.0.2</version>
</dependency>
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-base</artifactId>
    <version>17.0.2</version>
</dependency>
```

### Kiểm tra JavaFX plugin:
```xml
<plugin>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-maven-plugin</artifactId>
    <version>0.0.8</version>
    <configuration>
        <mainClass>com.p2p.P2PApplication</mainClass>
    </configuration>
</plugin>
```

## 💡 Tips

1. **Luôn dùng `mvn javafx:run`** thay vì chạy trực tiếp từ IDE
2. **Nếu dùng IDE**, tạo Maven run configuration thay vì Java Application
3. **Kiểm tra console output** để xem lỗi cụ thể
4. **Xóa `.idea` folder** và import lại project nếu vẫn lỗi

## 📞 Vẫn Không Chạy Được?

1. Kiểm tra console output để xem lỗi cụ thể
2. Chạy với verbose mode:
   ```bash
   mvn javafx:run -X
   ```
3. Kiểm tra file log (nếu có)
4. Đảm bảo không có firewall chặn Java


