# Sơ Đồ Use Case - PeerTalk P2P Chat Application

## 1. Tổng Quan Use Case Diagram

```mermaid
graph TB
    User[👤 User]
    
    subgraph Authentication["🔐 Xác Thực"]
        UC1[Đăng ký]
        UC2[Đăng nhập]
    end
    
    subgraph Discovery["🔍 Khám Phá"]
        UC3[Khám phá peer]
    end
    
    subgraph FriendMgmt["👥 Quản Lý Bạn Bè"]
        UC4[Thêm bạn bè]
        UC5[Hủy kết bạn]
    end
    
    subgraph Messaging["💬 Nhắn Tin"]
        UC6[Gửi tin nhắn]
        UC7[Thu hồi tin nhắn]
        UC8[Trả lời tin nhắn]
    end
    
    subgraph FileSharing["📁 Chia Sẻ File"]
        UC9[Chia sẻ file]
    end
    
    subgraph Group["👨‍👩‍👧‍👦 Nhóm"]
        UC10[Tạo nhóm]
        UC11[Gửi tin nhắn nhóm]
        UC12[Quản lý nhóm]
    end
    
    subgraph Call["📞 Gọi"]
        UC13[Gọi thoại/video]
    end
    
    User --> UC1
    User --> UC2
    User --> UC3
    User --> UC4
    User --> UC5
    User --> UC6
    User --> UC7
    User --> UC8
    User --> UC9
    User --> UC10
    User --> UC11
    User --> UC12
    User --> UC13
```

---

## 2. Chi Tiết Use Case Với Include/Extend

### 2.1. Use Case: Đăng Ký (Register)

```mermaid
graph TB
    User((👤 User))
    
    subgraph System["P2P Chat & Voice Application"]
        direction TB
        UC1((Đăng ký))
        Ext1((Username đã tồn tại))
        Inc1((Validate input))
        Inc2((Tạo peerId))
        Inc3((Lưu vào Database))
    end
    
    User -.-> UC1
    UC1 -.->|<<include>>| Inc1
    UC1 -.->|<<include>>| Inc2
    UC1 -.->|<<include>>| Inc3
    Ext1 -.->|<<extend>>| UC1
    
    style User fill:#FFFFFF,stroke:#000000,stroke-width:2px
    style UC1 fill:#FFFFFF,stroke:#000000,stroke-width:2px
    style Ext1 fill:#FFFFFF,stroke:#000000,stroke-width:2px
    style Inc1 fill:#FFFFFF,stroke:#000000,stroke-width:2px
    style Inc2 fill:#FFFFFF,stroke:#000000,stroke-width:2px
    style Inc3 fill:#FFFFFF,stroke:#000000,stroke-width:2px
    style System fill:#E6F3FF,stroke:#000000,stroke-width:3px
```

**Mô tả:**
- **Actor**: User
- **Use Case chính**: Đăng ký
- **Include**:
  - Validate input (kiểm tra username và password không rỗng)
  - Tạo peerId (UUID ngẫu nhiên)
  - Lưu vào Database (bảng users)
- **Extend**: Username đã tồn tại (hiển thị thông báo lỗi)

---

### 2.2. Use Case: Đăng Nhập (Login)

```mermaid
graph TB
    User[👤 User]
    
    UC2[Đăng nhập]
    Ext2[Sai thông tin]
    Inc4[Validate input]
    Inc5[Kiểm tra Database]
    Inc6[Tạo Peer object]
    Inc7[Khởi động Services]
    Inc8[Load giao diện chính]
    
    User --> UC2
    UC2 -.include.-> Inc4
    UC2 -.include.-> Inc5
    UC2 -.include.-> Inc6
    UC2 -.include.-> Inc7
    UC2 -.include.-> Inc8
    Ext2 -.extend.-> UC2
    
    style UC2 fill:#E1F5FF
    style Ext2 fill:#FFE1E1
    style Inc4 fill:#F0F0F0
    style Inc5 fill:#F0F0F0
    style Inc6 fill:#F0F0F0
    style Inc7 fill:#F0F0F0
    style Inc8 fill:#F0F0F0
```

**Mô tả:**
- **Actor**: User
- **Use Case chính**: Đăng nhập
- **Include**:
  - Validate input
  - Kiểm tra Database (SELECT từ users)
  - Tạo Peer object (với peerId và username)
  - Khởi động Services (PeerDiscoveryService, MessageService, VoiceCallService)
  - Load giao diện chính (MainView)
- **Extend**: Sai thông tin (hiển thị thông báo lỗi)

---

### 2.3. Use Case: Khám Phá Peer (Peer Discovery)

```mermaid
graph TB
    System[🖥️ System]
    
    UC3[Khám phá peer]
    Ext3[Peer offline]
    Inc9[Join Multicast Group]
    Inc10[Gửi HEARTBEAT]
    Inc11[Nhận HEARTBEAT]
    Inc12[Quản lý danh sách peers]
    
    System --> UC3
    UC3 -.include.-> Inc9
    UC3 -.include.-> Inc10
    UC3 -.include.-> Inc11
    UC3 -.include.-> Inc12
    Ext3 -.extend.-> UC3
    
    style UC3 fill:#E1F5FF
    style Ext3 fill:#FFE1E1
    style Inc9 fill:#F0F0F0
    style Inc10 fill:#F0F0F0
    style Inc11 fill:#F0F0F0
    style Inc12 fill:#F0F0F0
```

**Mô tả:**
- **Actor**: System (tự động)
- **Use Case chính**: Khám phá peer
- **Include**:
  - Join Multicast Group (230.0.0.1:4446)
  - Gửi HEARTBEAT (mỗi 2 giây)
  - Nhận HEARTBEAT (từ peers khác)
  - Quản lý danh sách peers (thêm mới, update, xóa offline)
- **Extend**: Peer offline (sau 6 giây không nhận HEARTBEAT)

---

### 2.4. Use Case: Thêm Bạn Bè (Add Friend)

```mermaid
graph TB
    User[👤 User]
    
    UC4[Thêm bạn bè]
    Ext4[Peer không online]
    Ext5[Từ chối lời mời]
    Inc13[Hiển thị danh sách peers]
    Inc14[Gửi FRIEND_REQUEST]
    Inc15[Nhận FRIEND_ACCEPT]
    Inc16[Lưu vào Database]
    
    User --> UC4
    UC4 -.include.-> Inc13
    UC4 -.include.-> Inc14
    UC4 -.include.-> Inc15
    UC4 -.include.-> Inc16
    Ext4 -.extend.-> UC4
    Ext5 -.extend.-> UC4
    
    style UC4 fill:#E1F5FF
    style Ext4 fill:#FFE1E1
    style Ext5 fill:#FFE1E1
    style Inc13 fill:#F0F0F0
    style Inc14 fill:#F0F0F0
    style Inc15 fill:#F0F0F0
    style Inc16 fill:#F0F0F0
```

**Mô tả:**
- **Actor**: User
- **Use Case chính**: Thêm bạn bè
- **Include**:
  - Hiển thị danh sách peers (từ PeerDiscoveryService)
  - Gửi FRIEND_REQUEST (qua UDP signal port)
  - Nhận FRIEND_ACCEPT (từ peer)
  - Lưu vào Database (bảng friends)
- **Extend**: 
  - Peer không online (không thể gửi request)
  - Từ chối lời mời (peer từ chối, không lưu vào DB)

---

### 2.5. Use Case: Gửi Tin Nhắn (Send Message)

```mermaid
graph TB
    User[👤 User]
    
    UC6[Gửi tin nhắn]
    Ext6[Gửi emoji]
    Ext7[Gửi hình ảnh]
    Ext8[Peer offline]
    Inc17[Nhập nội dung]
    Inc18[Tạo Message object]
    Inc19[Gửi qua UDP]
    Inc20[Hiển thị trong UI]
    
    User --> UC6
    UC6 -.include.-> Inc17
    UC6 -.include.-> Inc18
    UC6 -.include.-> Inc19
    UC6 -.include.-> Inc20
    Ext6 -.extend.-> UC6
    Ext7 -.extend.-> UC6
    Ext8 -.extend.-> UC6
    
    style UC6 fill:#E1F5FF
    style Ext6 fill:#FFF4E1
    style Ext7 fill:#FFF4E1
    style Ext8 fill:#FFE1E1
    style Inc17 fill:#F0F0F0
    style Inc18 fill:#F0F0F0
    style Inc19 fill:#F0F0F0
    style Inc20 fill:#F0F0F0
```

**Mô tả:**
- **Actor**: User
- **Use Case chính**: Gửi tin nhắn
- **Include**:
  - Nhập nội dung
  - Tạo Message object (Type: TEXT)
  - Gửi qua UDP (đến peer.textPort)
  - Hiển thị trong UI (màu xanh - sent)
- **Extend**:
  - Gửi emoji (content dạng :emoji_file:)
  - Gửi hình ảnh (Type: IMAGE)
  - Peer offline (không thể gửi)

---

### 2.6. Use Case: Chia Sẻ File (Share File)

```mermaid
graph TB
    User[👤 User]
    
    UC9[Chia sẻ file]
    Ext9[Chia sẻ trong nhóm]
    Ext10[Peer offline]
    Inc21[Chọn file]
    Inc22[Tạo fileId]
    Inc23[Tạo port ngẫu nhiên]
    Inc24[Gửi FILE_META qua UDP]
    Inc25[Mở ServerSocket TCP]
    Inc26[Gửi file qua TCP]
    
    User --> UC9
    UC9 -.include.-> Inc21
    UC9 -.include.-> Inc22
    UC9 -.include.-> Inc23
    UC9 -.include.-> Inc24
    UC9 -.include.-> Inc25
    UC9 -.include.-> Inc26
    Ext9 -.extend.-> UC9
    Ext10 -.extend.-> UC9
    
    style UC9 fill:#E1F5FF
    style Ext9 fill:#FFF4E1
    style Ext10 fill:#FFE1E1
    style Inc21 fill:#F0F0F0
    style Inc22 fill:#F0F0F0
    style Inc23 fill:#F0F0F0
    style Inc24 fill:#F0F0F0
    style Inc25 fill:#F0F0F0
    style Inc26 fill:#F0F0F0
```

**Mô tả:**
- **Actor**: User
- **Use Case chính**: Chia sẻ file
- **Include**:
  - Chọn file (FileChooser)
  - Tạo fileId (UUID)
  - Tạo port ngẫu nhiên (54000-54999)
  - Gửi FILE_META qua UDP (metadata: fileName, size, port)
  - Mở ServerSocket TCP (trên port đã tạo)
  - Gửi file qua TCP (FileTransferService)
- **Extend**:
  - Chia sẻ trong nhóm (gửi FILE_META đến tất cả members)
  - Peer offline (không thể gửi)

---

### 2.7. Use Case: Thu Hồi Tin Nhắn (Recall Message)

```mermaid
graph TB
    User[👤 User]
    
    UC7[Thu hồi tin nhắn]
    Ext11[Tin nhắn đã quá lâu]
    Ext12[Tin nhắn trong nhóm]
    Inc27[Chọn tin nhắn]
    Inc28[Tạo RECALL message]
    Inc29[Gửi qua UDP]
    Inc30[Đánh dấu recalled]
    Inc31[Hiển thị "Đã thu hồi"]
    
    User --> UC7
    UC7 -.include.-> Inc27
    UC7 -.include.-> Inc28
    UC7 -.include.-> Inc29
    UC7 -.include.-> Inc30
    UC7 -.include.-> Inc31
    Ext11 -.extend.-> UC7
    Ext12 -.extend.-> UC7
    
    style UC7 fill:#E1F5FF
    style Ext11 fill:#FFE1E1
    style Ext12 fill:#FFF4E1
    style Inc27 fill:#F0F0F0
    style Inc28 fill:#F0F0F0
    style Inc29 fill:#F0F0F0
    style Inc30 fill:#F0F0F0
    style Inc31 fill:#F0F0F0
```

**Mô tả:**
- **Actor**: User
- **Use Case chính**: Thu hồi tin nhắn
- **Include**:
  - Chọn tin nhắn (right-click → "Thu hồi")
  - Tạo RECALL message (với replyToMessageId)
  - Gửi qua UDP (signal port)
  - Đánh dấu recalled (setRecalled(true))
  - Hiển thị "Đã thu hồi" (màu xám, in nghiêng)
- **Extend**:
  - Tin nhắn đã quá lâu (có thể không cho phép thu hồi)
  - Tin nhắn trong nhóm (gửi RECALL đến tất cả members)

---

### 2.8. Use Case: Tạo Nhóm (Create Group)

```mermaid
graph TB
    User[👤 User]
    
    UC10[Tạo nhóm]
    Ext13[Thành viên từ chối]
    Inc32[Nhập tên nhóm]
    Inc33[Chọn thành viên]
    Inc34[Tạo Group object]
    Inc35[Lưu vào Database]
    Inc36[Gửi GROUP_INVITE]
    Inc37[Tạo Conversation]
    
    User --> UC10
    UC10 -.include.-> Inc32
    UC10 -.include.-> Inc33
    UC10 -.include.-> Inc34
    UC10 -.include.-> Inc35
    UC10 -.include.-> Inc36
    UC10 -.include.-> Inc37
    Ext13 -.extend.-> UC10
    
    style UC10 fill:#E1F5FF
    style Ext13 fill:#FFE1E1
    style Inc32 fill:#F0F0F0
    style Inc33 fill:#F0F0F0
    style Inc34 fill:#F0F0F0
    style Inc35 fill:#F0F0F0
    style Inc36 fill:#F0F0F0
    style Inc37 fill:#F0F0F0
```

**Mô tả:**
- **Actor**: User
- **Use Case chính**: Tạo nhóm
- **Include**:
  - Nhập tên nhóm
  - Chọn thành viên (multi-select từ danh sách bạn bè)
  - Tạo Group object (với UUID id)
  - Lưu vào Database (bảng groups và group_members)
  - Gửi GROUP_INVITE (đến từng thành viên)
  - Tạo Conversation (cho nhóm)
- **Extend**: Thành viên từ chối (không tham gia nhóm)

---

### 2.9. Use Case: Gửi Tin Nhắn Nhóm (Send Group Message)

```mermaid
graph TB
    User[👤 User]
    
    UC11[Gửi tin nhắn nhóm]
    Ext14[Thành viên offline]
    Inc38[Chọn nhóm]
    Inc39[Nhập tin nhắn]
    Inc40[Tạo GROUP_TEXT message]
    Inc41[Gửi đến từng member]
    Inc42[Hiển thị trong nhóm]
    
    User --> UC11
    UC11 -.include.-> Inc38
    UC11 -.include.-> Inc39
    UC11 -.include.-> Inc40
    UC11 -.include.-> Inc41
    UC11 -.include.-> Inc42
    Ext14 -.extend.-> UC11
    
    style UC11 fill:#E1F5FF
    style Ext14 fill:#FFE1E1
    style Inc38 fill:#F0F0F0
    style Inc39 fill:#F0F0F0
    style Inc40 fill:#F0F0F0
    style Inc41 fill:#F0F0F0
    style Inc42 fill:#F0F0F0
```

**Mô tả:**
- **Actor**: User
- **Use Case chính**: Gửi tin nhắn nhóm
- **Include**:
  - Chọn nhóm (từ danh sách conversations)
  - Nhập tin nhắn
  - Tạo GROUP_TEXT message (với groupId)
  - Gửi đến từng member (unicast, trừ người gửi)
  - Hiển thị trong nhóm (màu xanh)
- **Extend**: Thành viên offline (không nhận được tin nhắn)

---

### 2.10. Use Case: Gọi Thoại/Video (Voice/Video Call)

```mermaid
graph TB
    User[👤 User]
    
    UC13[Gọi thoại/video]
    Ext15[Gọi video]
    Ext16[Từ chối cuộc gọi]
    Ext17[Peer offline]
    Ext18[Đang trong cuộc gọi]
    Inc43[Chọn peer]
    Inc44[Gửi CALL_REQUEST]
    Inc45[Nhận CALL_ACCEPT]
    Inc46[Khởi động audio streaming]
    Inc47[Kết nối cuộc gọi]
    
    User --> UC13
    UC13 -.include.-> Inc43
    UC13 -.include.-> Inc44
    UC13 -.include.-> Inc45
    UC13 -.include.-> Inc46
    UC13 -.include.-> Inc47
    Ext15 -.extend.-> UC13
    Ext16 -.extend.-> UC13
    Ext17 -.extend.-> UC13
    Ext18 -.extend.-> UC13
    
    style UC13 fill:#E1F5FF
    style Ext15 fill:#FFF4E1
    style Ext16 fill:#FFE1E1
    style Ext17 fill:#FFE1E1
    style Ext18 fill:#FFE1E1
    style Inc43 fill:#F0F0F0
    style Inc44 fill:#F0F0F0
    style Inc45 fill:#F0F0F0
    style Inc46 fill:#F0F0F0
    style Inc47 fill:#F0F0F0
```

**Mô tả:**
- **Actor**: User
- **Use Case chính**: Gọi thoại/video
- **Include**:
  - Chọn peer (từ danh sách bạn bè)
  - Gửi CALL_REQUEST (qua UDP signal port)
  - Nhận CALL_ACCEPT (từ peer)
  - Khởi động audio streaming (mở microphone và speaker)
  - Kết nối cuộc gọi (gửi/nhận audio qua UDP voice port)
- **Extend**:
  - Gọi video (tương tự nhưng có video stream)
  - Từ chối cuộc gọi (gửi CALL_REJECT)
  - Peer offline (không thể gọi)
  - Đang trong cuộc gọi (không thể gọi mới)

---

### 2.11. Use Case: Trả Lời Tin Nhắn (Reply Message)

```mermaid
graph TB
    User[👤 User]
    
    UC8[Trả lời tin nhắn]
    Inc48[Chọn tin nhắn gốc]
    Inc49[Set replyTarget]
    Inc50[Nhập tin nhắn trả lời]
    Inc51[Gửi với replyToMessageId]
    Inc52[Hiển thị liên kết]
    
    User --> UC8
    UC8 -.include.-> Inc48
    UC8 -.include.-> Inc49
    UC8 -.include.-> Inc50
    UC8 -.include.-> Inc51
    UC8 -.include.-> Inc52
    
    style UC8 fill:#E1F5FF
    style Inc48 fill:#F0F0F0
    style Inc49 fill:#F0F0F0
    style Inc50 fill:#F0F0F0
    style Inc51 fill:#F0F0F0
    style Inc52 fill:#F0F0F0
```

**Mô tả:**
- **Actor**: User
- **Use Case chính**: Trả lời tin nhắn
- **Include**:
  - Chọn tin nhắn gốc (right-click → "Trả lời")
  - Set replyTarget (lưu message gốc)
  - Nhập tin nhắn trả lời
  - Gửi với replyToMessageId (liên kết đến tin nhắn gốc)
  - Hiển thị liên kết (trong UI của peer nhận)

---

## 3. Use Case Diagram Tổng Hợp

```mermaid
graph TB
    User[👤 User]
    
    UC1[Đăng ký]
    UC2[Đăng nhập]
    UC3[Khám phá peer]
    UC4[Thêm bạn bè]
    UC5[Hủy kết bạn]
    UC6[Gửi tin nhắn]
    UC7[Thu hồi tin nhắn]
    UC8[Trả lời tin nhắn]
    UC9[Chia sẻ file]
    UC10[Tạo nhóm]
    UC11[Gửi tin nhắn nhóm]
    UC12[Quản lý nhóm]
    UC13[Gọi thoại/video]
    
    User --> UC1
    User --> UC2
    User --> UC3
    User --> UC4
    User --> UC5
    User --> UC6
    User --> UC7
    User --> UC8
    User --> UC9
    User --> UC10
    User --> UC11
    User --> UC12
    User --> UC13
    
    UC4 -.->|include| UC3
    UC6 -.->|include| UC4
    UC7 -.->|include| UC6
    UC8 -.->|include| UC6
    UC9 -.->|include| UC4
    UC10 -.->|include| UC4
    UC11 -.->|include| UC10
    UC13 -.->|include| UC4
    
    style User fill:#FFE1E1
    style UC1 fill:#E1F5FF
    style UC2 fill:#E1F5FF
    style UC3 fill:#E1F5FF
    style UC4 fill:#E1F5FF
    style UC5 fill:#E1F5FF
    style UC6 fill:#E1F5FF
    style UC7 fill:#E1F5FF
    style UC8 fill:#E1F5FF
    style UC9 fill:#E1F5FF
    style UC10 fill:#E1F5FF
    style UC11 fill:#E1F5FF
    style UC12 fill:#E1F5FF
    style UC13 fill:#E1F5FF
```

---

## 4. Bảng Tóm Tắt Use Cases

| ID | Use Case | Actor | Mô Tả Ngắn |
|----|----------|-------|------------|
| UC1 | Đăng ký | User | Tạo tài khoản mới |
| UC2 | Đăng nhập | User | Đăng nhập vào hệ thống |
| UC3 | Khám phá peer | System | Tự động phát hiện peers trong LAN |
| UC4 | Thêm bạn bè | User | Gửi và chấp nhận lời mời kết bạn |
| UC5 | Hủy kết bạn | User | Xóa bạn bè khỏi danh sách |
| UC6 | Gửi tin nhắn | User | Gửi tin nhắn văn bản/emoji/hình ảnh |
| UC7 | Thu hồi tin nhắn | User | Thu hồi tin nhắn đã gửi |
| UC8 | Trả lời tin nhắn | User | Trả lời một tin nhắn cụ thể |
| UC9 | Chia sẻ file | User | Gửi file đến bạn bè hoặc nhóm |
| UC10 | Tạo nhóm | User | Tạo nhóm chat mới |
| UC11 | Gửi tin nhắn nhóm | User | Gửi tin nhắn đến tất cả thành viên nhóm |
| UC12 | Quản lý nhóm | User | Rời nhóm, xóa nhóm, mời thành viên |
| UC13 | Gọi thoại/video | User | Thực hiện cuộc gọi 1-1 |

---

## 5. Quan Hệ Giữa Các Use Cases

### 5.1. Quan Hệ Include (Bắt Buộc)

Các use case này **luôn luôn** bao gồm các use case khác:

- **Đăng nhập** includes: Validate, Kiểm tra DB, Tạo Peer, Khởi động Services
- **Khám phá peer** includes: Join Multicast, Gửi/Nhận HEARTBEAT
- **Thêm bạn bè** includes: Hiển thị peers, Gửi REQUEST, Nhận ACCEPT
- **Gửi tin nhắn** includes: Nhập nội dung, Tạo Message, Gửi UDP, Hiển thị UI
- **Chia sẻ file** includes: Chọn file, Tạo fileId, Gửi META, Gửi qua TCP

### 5.2. Quan Hệ Extend (Tùy Chọn)

Các use case này **có thể** được mở rộng trong các điều kiện cụ thể:

- **Đăng ký** extends: Username đã tồn tại
- **Đăng nhập** extends: Sai thông tin
- **Thêm bạn bè** extends: Peer offline, Từ chối lời mời
- **Gửi tin nhắn** extends: Gửi emoji, Gửi hình ảnh, Peer offline
- **Chia sẻ file** extends: Chia sẻ trong nhóm, Peer offline
- **Gọi thoại** extends: Gọi video, Từ chối, Peer offline, Đang trong cuộc gọi

---

## 6. Ký Hiệu Sử Dụng

- **→** (Solid line): Association (Actor sử dụng Use Case)
- **-.include.->** (Dashed line với <<include>>): Include relationship
- **-.extend.->** (Dashed line với <<extend>>): Extend relationship
- **👤**: Actor (User)
- **🖥️**: System Actor

---

**Lưu ý**: Tất cả các sơ đồ trên sử dụng Mermaid syntax và có thể hiển thị trên các công cụ hỗ trợ Mermaid như GitHub, GitLab, hoặc các Markdown viewer.

