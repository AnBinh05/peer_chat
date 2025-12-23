# 🚨 Giải Pháp Nhanh: Lỗi Insufficient Memory

## ⚡ Giải Pháp Tức Thì (Khuyến Nghị)

**Bỏ qua build từ IntelliJ, chạy trực tiếp từ Terminal:**

```bash
# Mở PowerShell hoặc CMD trong thư mục project
cd D:\PeerTalk-main

# Build và chạy
mvn clean compile
mvn javafx:run
```

Hoặc dùng script:
```bash
run.bat
```

## 🔧 Fix Lỗi Memory trong IntelliJ (Nếu Muốn Dùng IDE)

### Bước 1: Tăng Build Process Memory

1. **File** → **Settings** (hoặc `Ctrl+Alt+S`)
2. **Build, Execution, Deployment** → **Compiler**
3. Tìm **"Build process heap size (Mbytes)"**
4. Đổi từ **700** thành **2048** hoặc **3072**
5. Click **Apply** → **OK**
6. **Restart IntelliJ IDEA**

### Bước 2: Tăng IntelliJ IDEA Memory (Tùy chọn)

1. **Help** → **Edit Custom VM Options**
2. Tìm dòng `-Xmx2048m` (hoặc tương tự)
3. Đổi thành `-Xmx4096m` hoặc `-Xmx6144m`
4. **Save** và **Restart IntelliJ**

## ✅ Đã Tạo File Cấu Hình

Đã tạo file `.mvn/jvm.config` với cấu hình:
```
-Xmx2048m
-Xms512m
```

File này sẽ tự động được Maven sử dụng khi build từ command line.

## 🎯 Cách Chạy Ứng Dụng

### Option 1: Command Line (Đơn giản nhất)

```bash
mvn javafx:run
```

### Option 2: Dùng Script

```bash
# Windows
run.bat
```

### Option 3: Build JAR

```bash
mvn clean package
# Sau đó chạy JAR với JavaFX modules
```

## ⚠️ Lưu Ý

- **Lỗi này KHÔNG phải lỗi code**, mà là vấn đề về memory configuration
- **Chạy từ terminal thường ổn định hơn** IDE build
- **Nếu RAM < 8GB**, nên chạy từ command line

## 📋 Checklist

- [ ] Đã tăng Build Process Heap Size trong IntelliJ (nếu muốn dùng IDE)
- [ ] Hoặc đã chuyển sang chạy từ command line
- [ ] Đã kiểm tra database đã setup chưa

## 📞 Cần Giúp Đỡ?

Xem file `INTELLIJ_MEMORY_FIX.md` để biết chi tiết hơn.


