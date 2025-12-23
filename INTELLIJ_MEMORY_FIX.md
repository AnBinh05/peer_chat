# 🔧 Fix Lỗi: Insufficient Memory - IntelliJ IDEA Build

## ❌ Lỗi Gặp Phải

```
There is insufficient memory for the Java Runtime Environment to continue.
Native memory allocation (mmap) failed to map 264241152 bytes.
The paging file is too small for this operation to complete.
```

## ✅ Giải Pháp

### Giải pháp 1: Tăng Memory cho IntelliJ Build Process (KHUYẾN NGHỊ)

1. **Mở IntelliJ IDEA Settings**:
   - File → Settings (hoặc `Ctrl+Alt+S`)
   - Hoặc: Help → Edit Custom VM Options

2. **Tăng Build Process Heap Size**:
   - Settings → Build, Execution, Deployment → Compiler
   - Tìm **"Build process heap size (Mbytes)"**
   - Tăng từ mặc định (700MB) lên **2048** hoặc **3072**
   - Click **Apply** và **OK**

3. **Tăng IntelliJ IDEA Memory** (nếu cần):
   - Help → Edit Custom VM Options
   - Tìm dòng `-Xmx` (thường là `-Xmx2048m`)
   - Tăng lên `-Xmx4096m` hoặc `-Xmx6144m` (nếu RAM đủ)
   - Restart IntelliJ IDEA

### Giải pháp 2: Chạy từ Command Line (Đơn giản nhất)

Thay vì build từ IDE, chạy từ terminal:

```bash
# Windows PowerShell hoặc CMD
mvn clean compile
mvn javafx:run
```

Hoặc dùng script:
```bash
# Windows
run.bat
```

### Giải pháp 3: Tối ưu Maven Build

Tạo file `.mvn/jvm.config` trong project root:

**Windows** (PowerShell):
```powershell
New-Item -ItemType Directory -Force -Path .mvn
Set-Content -Path .mvn\jvm.config -Value "-Xmx2048m -Xms512m"
```

**Hoặc tạo thủ công**:
1. Tạo folder `.mvn` trong project root
2. Tạo file `jvm.config` trong folder `.mvn`
3. Thêm nội dung: `-Xmx2048m -Xms512m`

### Giải pháp 4: Tắt Parallel Compilation

1. Settings → Build, Execution, Deployment → Compiler
2. Bỏ chọn **"Compile independent modules in parallel"**
3. Bỏ chọn **"Build process heap size"** nếu có
4. Click **Apply** và **OK**

### Giải pháp 5: Tăng Windows Virtual Memory (Paging File)

1. Mở **Control Panel** → **System** → **Advanced system settings**
2. Tab **Advanced** → **Performance** → **Settings**
3. Tab **Advanced** → **Virtual memory** → **Change**
4. Bỏ chọn **"Automatically manage paging file size"**
5. Chọn ổ C: → **Custom size**
6. **Initial size**: 4096 MB
7. **Maximum size**: 8192 MB (hoặc cao hơn nếu có dung lượng)
8. Click **Set** → **OK** → Restart Windows

## 🚀 Cách Chạy Ứng Dụng (Không Dùng IDE Build)

### Option 1: Dùng Maven Command Line

```bash
# Build project
mvn clean compile

# Chạy ứng dụng
mvn javafx:run
```

### Option 2: Dùng Script

```bash
# Windows
run.bat

# Linux/Mac
chmod +x run.sh
./run.sh
```

### Option 3: Build JAR và chạy

```bash
# Build JAR
mvn clean package

# Chạy JAR (cần cấu hình JavaFX modules)
java --module-path %PATH_TO_FX% --add-modules javafx.controls,javafx.fxml -jar target/PeerTalk-1.0-SNAPSHOT.jar
```

## 📋 Checklist

- [ ] Đã tăng Build Process Heap Size trong IntelliJ
- [ ] Đã restart IntelliJ sau khi thay đổi
- [ ] Hoặc đã chuyển sang chạy từ command line
- [ ] Đã kiểm tra RAM còn trống (ít nhất 4GB)

## 💡 Tips

1. **Nếu RAM máy < 8GB**: Nên chạy từ command line thay vì IDE
2. **Đóng các ứng dụng khác** để giải phóng RAM
3. **Tắt các plugin không cần thiết** trong IntelliJ
4. **Sử dụng Maven từ command line** thay vì IDE build system

## 🔍 Kiểm Tra Memory

### Windows:
```powershell
# Kiểm tra RAM
systeminfo | findstr "Total Physical Memory"
```

### Kiểm tra trong IntelliJ:
- Help → About → Xem memory usage
- Task Manager → Xem IntelliJ memory usage

## ⚠️ Lưu Ý

- Lỗi này **KHÔNG phải lỗi code**, mà là vấn đề về cấu hình memory
- Build từ command line thường ổn định hơn IDE build
- Nếu vẫn lỗi sau khi tăng memory, nên chạy từ terminal


