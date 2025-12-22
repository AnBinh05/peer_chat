# 🎯 P2P Chat & Voice Call Application

## 📖 Tổng quan

Ứng dụng P2P hoàn chỉnh kết hợp **Text Chat** và **Voice Call** sử dụng Java, JavaFX và UDP networking. Không sử dụng TCP Server - hoàn toàn P2P.

### ✨ Tính năng chính

1. ✅ **P2P Text Chat**
    - Chat room real-time giữa nhiều peer
    - UDP Multicast broadcast
    - Giao diện bubble chat đẹp mắt

2. ✅ **P2P Voice Call**
    - Gọi thoại 1-1 giống Skype
    - UDP audio streaming
    - Incoming call popup
    - Call/Hangup controls

3. ✅ **Peer Discovery**
    - Tự động phát hiện peer trong LAN
    - Heartbeat mechanism
    - Peer timeout detection

---

## 🏗️ Kiến trúc hệ thống

### Cấu trúc thư mục

```
P2PChatVoiceApp/
├── src/main/java/com/p2p/
│   ├── P2PApplication.java          # Main application
│   ├── model/
│   │   ├── Peer.java                # Model peer
│   │   └── Message.java             # Model message
│   ├── service/
│   │   ├── PeerDiscoveryService.java   # Peer discovery
│   │   ├── MessageService.java         # Text messaging
│   │   └── VoiceCallService.java       # Voice call
│   └── util/
│       └── JsonUtil.java            # JSON helper
└── pom.xml                          # Maven config
```

### Kiến trúc Network

```
┌─────────────────────────────────────────────────────┐
│              UDP MULTICAST GROUP                     │
│            230.0.0.1:4446                           │
├─────────────────────────────────────────────────────┤
│                                                      │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐     │
│  │  Peer A  │◄──►│  Peer B  │◄──►│  Peer C  │     │
│  │          │    │          │    │          │     │
│  │ Text+    │    │ Text+    │    │ Text+    │     │
│  │ Voice    │    │ Voice    │    │ Voice    │     │
│  └──────────┘    └──────────┘    └──────────┘     │
│                                                      │
│  • Heartbeat broadcast (2s)                         │
│  • Text message multicast                           │
│  • Voice call signaling (unicast UDP)               │
│  • Audio stream (unicast UDP)                       │
└─────────────────────────────────────────────────────┘
```

---

## 🔧 Yêu cầu hệ thống

- **Java**: JDK 11 hoặc cao hơn
- **Maven**: 3.6+ (để build project)
- **OS**: Windows, macOS, Linux
- **Network**: Cùng LAN (Wi-Fi hoặc Ethernet)

---

## 🚀 Cài đặt và chạy

### Bước 1: Clone hoặc tạo project

```bash
mkdir P2PChatVoiceApp
cd P2PChatVoiceApp
```

### Bước 2: Tạo cấu trúc thư mục

```bash
mkdir -p src/main/java/com/p2p/{model,service,util,controller}
```

### Bước 3: Copy các file code

- Copy `pom.xml` vào thư mục root
- Copy các file `.java` vào đúng package

### Bước 4: Build project

```bash
mvn clean install
```

### Bước 5: Chạy ứng dụng

```bash
mvn javafx:run
```

Hoặc nếu đã build jar:

```bash
java -jar target/P2PChatVoiceApp-1.0-SNAPSHOT.jar
```

### Bước 6: Chạy nhiều instance (để test)

Mở nhiều terminal và chạy `mvn javafx:run` trên mỗi terminal. Mỗi instance sẽ tự động phát hiện nhau.

---

## 📚 Giải thích chi tiết từng phần

### 1. Peer Discovery (PeerDiscoveryService.java)

**Cơ chế:**
- Sử dụng **UDP Multicast** (230.0.0.1:4446)
- Mỗi peer gửi **HEARTBEAT** broadcast mỗi 2 giây
- Peer khác nhận heartbeat → thêm vào danh sách
- Nếu peer không gửi heartbeat > 10s → bị loại khỏi danh sách

**Code quan trọng:**

```java
// Join multicast group
socket.joinGroup(group);

// Gửi heartbeat
Message heartbeat = new Message(Message.Type.HEARTBEAT, ...);
String json = JsonUtil.toJson(heartbeat);
DatagramPacket packet = new DatagramPacket(..., group, MULTICAST_PORT);
socket.send(packet);

// Nhận heartbeat
socket.receive(packet);
Message message = JsonUtil.fromJson(json);
handleHeartbeat(message, packet.getAddress());
```

**Tại sao dùng Multicast?**
- Broadcast đến tất cả peer trong cùng group
- Hiệu quả hơn gửi riêng lẻ
- Phù hợp với LAN

---

### 2. Text Chat (MessageService.java)

**Cơ chế:**
- Dùng chung multicast group với discovery
- Khi gửi tin nhắn → broadcast qua multicast
- Tất cả peer nhận được → hiển thị trong UI
- Bỏ qua tin nhắn từ chính mình

**Code quan trọng:**

```java
// Gửi tin nhắn
Message textMsg = new Message(Message.Type.TEXT, peerId, name, content);
String json = JsonUtil.toJson(textMsg);
DatagramPacket packet = new DatagramPacket(..., group, MULTICAST_PORT);
socket.send(packet);

// Nhận tin nhắn
socket.receive(packet);
Message message = JsonUtil.fromJson(json);
if (message.getType() == Message.Type.TEXT) {
    onMessageReceived.accept(message);  // Callback đến UI
}
```

**JSON Format:**

```json
{
  "type": "TEXT",
  "from": "peer-uuid",
  "fromName": "Alice",
  "content": "Hello everyone!",
  "timestamp": "2024-12-06T10:30:00"
}
```

---

### 3. Voice Call (VoiceCallService.java)

**Cơ chế:**

#### Signaling (CALL_REQUEST, ACCEPT, REJECT, END)
- Gửi **unicast UDP** trực tiếp đến peer cụ thể
- Không broadcast (chỉ 2 peer liên quan)

```java
// Peer A gọi Peer B
Message callReq = new Message(Message.Type.CALL_REQUEST, ...);
callReq.setTarget(peerB.getId());
sendSignalMessage(callReq, peerB);  // Gửi đến IP:port của B

// Peer B nhận → hiển thị popup "Incoming Call"
// B accept → gửi CALL_ACCEPT về A
Message accept = new Message(Message.Type.CALL_ACCEPT, ...);
sendSignalMessage(accept, peerA);

// Cả A và B bắt đầu audio streaming
```

#### Audio Streaming

**Capture (Mic → Network):**

```java
// Mở microphone
TargetDataLine microphone = AudioSystem.getLine(...);
microphone.open(audioFormat);
microphone.start();

// Thread gửi audio
while (inCall) {
    byte[] buffer = new byte[1024];
    microphone.read(buffer, 0, buffer.length);  // Đọc từ mic
    
    // Gửi UDP đến peer
    DatagramPacket packet = new DatagramPacket(
        buffer, buffer.length,
        peerAddress, peerVoicePort
    );
    audioSocket.send(packet);
}
```

**Playback (Network → Speaker):**

```java
// Mở speaker
SourceDataLine speaker = AudioSystem.getLine(...);
speaker.open(audioFormat);
speaker.start();

// Thread nhận audio
while (inCall) {
    byte[] buffer = new byte[1024];
    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
    audioSocket.receive(packet);  // Nhận UDP
    
    // Phát qua speaker
    speaker.write(packet.getData(), 0, packet.getLength());
}
```

**Audio Format:**
- Sample Rate: **16 kHz** (đủ cho giọng nói)
- Sample Size: **16-bit** (chất lượng tốt)
- Channels: **Mono** (tiết kiệm bandwidth)
- Encoding: **PCM** (không nén)

**Tại sao dùng UDP cho audio?**
- Real-time: Độ trễ thấp
- Packet loss OK: Âm thanh vẫn nghe được dù mất vài packet
- TCP sẽ retransmit → tăng delay → giọng nói bị lag

---

## 🎨 Giao diện UI

### Layout chính

```
┌─────────────────────────────────────────────────────┐
│  P2P Chat & Voice - Alice                           │
├──────────────┬──────────────────────────────────────┤
│              │  💬 Chat Room                         │
│ 📡 Online    ├──────────────────────────────────────┤
│ Peers        │                                       │
│              │  [Bob] 10:30                          │
│ You:         │  Hello everyone!                      │
│ Alice        │                                       │
│ 192.168.1.5  │      [Alice] 10:31                    │
│              │      Hi Bob! 👋                       │
│──────────────│                                       │
│ • Bob        │                                       │
│   192.168.1.6│                                       │
│              │                                       │
│ • Charlie    │                                       │
│   192.168.1.7│                                       │
│              │                                       │
│──────────────│                                       │
│ [📞 Call]    ├──────────────────────────────────────┤
│ [📴 Hangup]  │ [Type a message...]         [Send]   │
│ Ready        │                                       │
└──────────────┴──────────────────────────────────────┘
```

### Các tính năng UI

1. **Peer List**: Hiển thị danh sách peer online
2. **Chat Bubbles**: Tin nhắn dạng bubble (xanh = mình, trắng = người khác)
3. **Call Controls**:
    - Chọn peer → Click "Call"
    - Khi có cuộc gọi đến → Popup "Incoming Call"
    - Trong cuộc gọi → Click "Hangup" để kết thúc
4. **Status**: Hiển thị trạng thái (Ready, Calling, Connected)

---

## 🧪 Cách test

### Test 1: Text Chat

1. Chạy 3 instance trên 3 máy khác nhau (hoặc 3 terminal)
2. Mỗi instance nhập tên khác nhau (Alice, Bob, Charlie)
3. Đợi vài giây → tất cả sẽ thấy nhau trong peer list
4. Gửi tin nhắn từ Alice → Bob và Charlie sẽ nhận được
5. Kiểm tra tin nhắn hiển thị đúng người gửi

### Test 2: Voice Call

1. Chạy 2 instance (Alice và Bob)
2. Alice chọn Bob trong peer list → Click "Call"
3. Bob sẽ thấy popup "Incoming Call from Alice"
4. Bob click "Answer"
5. Nói thử → kiểm tra âm thanh
6. Click "Hangup" để kết thúc

### Test 3: Peer Timeout

1. Chạy 2 instance
2. Đóng 1 instance bất ngờ (không graceful shutdown)
3. Sau 10 giây, instance còn lại sẽ tự động loại peer đã mất

---

## ⚠️ Xử lý lỗi thường gặp

### Lỗi 1: "Address already in use"

**Nguyên nhân**: Port đã được sử dụng

**Giải pháp**:
- Đóng instance cũ
- Hoặc sửa code để dùng port khác

```java
int textPort = 50000 + new Random().nextInt(1000);  // Random port
```

### Lỗi 2: Không thấy peer khác

**Nguyên nhân**:
- Firewall chặn UDP multicast
- Không cùng network

**Giải pháp**:
- Tắt firewall tạm thời
- Kiểm tra cùng subnet (ping thử)
- Kiểm tra router có block multicast không

### Lỗi 3: Không nghe được âm thanh

**Nguyên nhân**:
- Mic/Speaker không hoạt động
- Audio format không support

**Giải pháp**:
- Kiểm tra mic/speaker trong system settings
- Thử sample rate thấp hơn (8kHz)
- Kiểm tra audio permissions

### Lỗi 4: JavaFX runtime components missing

**Giải pháp**:

```bash
# Chạy với module path
mvn javafx:run

# Hoặc
java --module-path /path/to/javafx-sdk/lib \
     --add-modules javafx.controls,javafx.fxml \
     -jar app.jar
```

---

## 📊 Phân tích Performance

### Bandwidth Usage

**Text Chat:**
- 1 tin nhắn ≈ 500 bytes JSON
- 10 tin nhắn/phút ≈ 5 KB/phút
- **Rất nhẹ**

**Voice Call:**
- 16kHz, 16-bit, mono = 32 KB/s
- 1 phút ≈ 1.9 MB
- **Chấp nhận được với LAN**

### Latency

- **Peer Discovery**: 2-4 giây (heartbeat interval)
- **Text Message**: < 100ms (trong LAN)
- **Voice Call**: 50-200ms (acceptable cho LAN)

---

## 🎓 Kiến thức mở rộng

### 1. Tại sao không dùng TCP?

**TCP** (Transmission Control Protocol):
- ✅ Đảm bảo tin cậy (reliable)
- ✅ Đảm bảo thứ tự (ordered)
- ❌ Cần server trung tâm (client-server model)
- ❌ Retransmission gây delay
- ❌ Phức tạp hơn với P2P

**UDP** (User Datagram Protocol):
- ✅ Không cần server (P2P friendly)
- ✅ Low latency
- ✅ Multicast/Broadcast support
- ❌ Không đảm bảo tin cậy
- ❌ Có thể mất packet
- ✅ **OK cho text chat và voice** (mất vài packet không ảnh hưởng nhiều)

### 2. Multicast vs Broadcast

**Multicast** (230.0.0.1):
- Chỉ peer join group mới nhận
- Hiệu quả hơn broadcast
- **Được chọn trong project này**

**Broadcast** (255.255.255.255):
- Tất cả máy trong subnet nhận
- Gây traffic nhiều hơn
- Một số router block

### 3. Audio Codec

**PCM** (được dùng):
- Không nén
- Chất lượng cao
- Bandwidth: 32 KB/s

**Opus** (nâng cao):
- Nén tốt (6-12 KB/s)
- Chất lượng tốt
- Phức tạp hơn

### 4. NAT Traversal

**Trong project**: Chỉ hoạt động trong LAN

**Mở rộng** (cho WAN):
- STUN server (Session Traversal Utilities for NAT)
- TURN server (Traversal Using Relays around NAT)
- ICE (Interactive Connectivity Establishment)

---

## 🔐 Bảo mật (Security)

**⚠️ Lưu ý**: Project này là demo học tập, **KHÔNG** dùng trong production.

**Các vấn đề bảo mật:**

1. **Không mã hóa**: Tin nhắn và audio gửi plain text
2. **Không xác thực**: Ai cũng có thể join
3. **Spoofing**: Có thể giả mạo peer ID

**Cải thiện** (nâng cao):
- Mã hóa end-to-end (AES, RSA)
- Xác thực peer (shared secret, certificates)
- Chống replay attack (nonce, timestamp)

---

## 📝 Bài tập mở rộng

### Cơ bản

1. ✏️ Thêm emoji selector cho chat
2. ✏️ Hiển thị "typing..." indicator
3. ✏️ Lưu chat history vào file
4. ✏️ Thêm notification sound

### Trung bình

1. 🔧 File transfer P2P (send/receive files)
2. 🔧 Group voice call (3+ peers)
3. 🔧 Screen sharing (screenshot broadcast)
4. 🔧 Custom theme (dark mode)

### Nâng cao

1. 🚀 Mã hóa end-to-end
2. 🚀 NAT traversal (STUN/TURN)
3. 🚀 Video call (webcam streaming)
4. 🚀 Opus codec integration
5. 🚀 Mesh network topology

---

## 🙏 Kết luận

Project này cung cấp:
- ✅ Ví dụ hoàn chỉnh về **P2P networking**
- ✅ Kết hợp **UDP Multicast** và **Audio Streaming**
- ✅ **JavaFX GUI** đẹp và dễ dùng
- ✅ **Code rõ ràng**, nhiều comment
- ✅ Phù hợp cho **đồ án môn học Mạng máy tính**

**Học được gì:**
- UDP socket programming
- Multicast/Broadcast
- P2P architecture
- Audio processing (Java Sound API)
- Real-time communication
- JavaFX UI development

**Chúc bạn thành công!** 🎉

---

## 📞 Support

Nếu gặp vấn đề, hãy kiểm tra:
1. Java version (>= 11)
2. Maven installed
3. JavaFX dependencies
4. Firewall settings
5. Network connectivity

Happy coding! 🚀