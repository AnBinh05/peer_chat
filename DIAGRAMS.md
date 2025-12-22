# Sơ Đồ Hệ Thống PeerTalk

## 1. Sơ Đồ Use Case (Use Case Diagram)

```mermaid
graph TB
    User[User]
    
    %% Authentication
    UC1[Đăng ký tài khoản]
    UC2[Đăng nhập]
    
    %% Friend Management
    UC3[Tìm kiếm bạn bè]
    UC4[Gửi lời mời kết bạn]
    UC5[Chấp nhận lời mời kết bạn]
    UC6[Hủy kết bạn]
    
    %% Messaging
    UC7[Gửi tin nhắn văn bản]
    UC8[Gửi tin nhắn emoji]
    UC9[Gửi hình ảnh]
    UC10[Gửi file]
    UC11[Thu hồi tin nhắn]
    UC12[Trả lời tin nhắn]
    
    %% Group Management
    UC13[Tạo nhóm]
    UC14[Mời thành viên vào nhóm]
    UC15[Gửi tin nhắn nhóm]
    UC16[Rời nhóm]
    UC17[Xóa nhóm]
    
    %% Voice/Video Call
    UC18[Gọi thoại]
    UC19[Gọi video]
    
    %% Discovery
    UC20[Khám phá peer trong mạng LAN]
    
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
    User --> UC14
    User --> UC15
    User --> UC16
    User --> UC17
    User --> UC18
    User --> UC19
    User --> UC20
```

## 2. Sơ Đồ Hoạt Động (Activity Diagrams)

### 2.1. Quy Trình Đăng Nhập và Khởi Tạo

```mermaid
flowchart TD
    Start([Bắt đầu]) --> Input[Nhập username và password]
    Input --> Validate{Kiểm tra thông tin}
    Validate -->|Sai| Error[Hiển thị lỗi]
    Error --> Input
    Validate -->|Đúng| CreatePeer[Tạo LocalPeer]
    CreatePeer --> AttachNetwork[Gắn thông tin mạng<br/>IP, Ports]
    AttachNetwork --> StartServices[Khởi động Services<br/>- DiscoveryService<br/>- MessageService<br/>- VoiceCallService]
    StartServices --> LoadUI[Load giao diện chính]
    LoadUI --> LoadData[Load dữ liệu<br/>- Danh sách bạn bè<br/>- Nhóm chat<br/>- Lịch sử tin nhắn]
    LoadData --> End([Hoàn thành])
```

### 2.2. Quy Trình Gửi Tin Nhắn

```mermaid
flowchart TD
    Start([Người dùng nhập tin nhắn]) --> CheckConv{Đã chọn<br/>cuộc trò chuyện?}
    CheckConv -->|Chưa| End1([Kết thúc])
    CheckConv -->|Có| CheckType{Loại tin nhắn?}
    
    CheckType -->|Văn bản| CreateText[Tạo Message TEXT]
    CheckType -->|Emoji| CreateEmoji[Tạo Message EMOJI]
    CheckType -->|Hình ảnh| CreateImage[Tạo Message IMAGE]
    CheckType -->|File| CreateFile[Tạo Message FILE_META]
    
    CreateText --> CheckGroup{Cuộc trò chuyện<br/>nhóm?}
    CreateEmoji --> CheckGroup
    CreateImage --> CheckGroup
    CreateFile --> CheckGroup
    
    CheckGroup -->|Nhóm| SendGroup[Gửi đến tất cả<br/>thành viên nhóm]
    CheckGroup -->|Riêng tư| SendPrivate[Gửi đến peer đích]
    
    SendGroup --> SaveLocal[Lưu tin nhắn<br/>vào Conversation]
    SendPrivate --> SaveLocal
    SaveLocal --> Display[Hiển thị tin nhắn<br/>trên UI]
    Display --> End2([Hoàn thành])
```

### 2.3. Quy Trình Thêm Bạn

```mermaid
flowchart TD
    Start([Mở dialog thêm bạn]) --> Discovery[DiscoveryService<br/>hiển thị danh sách peer online]
    Discovery --> Select[Chọn peer từ danh sách]
    Select --> SendRequest[Gửi FRIEND_REQUEST<br/>qua Signal Port]
    SendRequest --> Wait[Chờ phản hồi]
    Wait --> Receive{Peer nhận được<br/>lời mời?}
    Receive -->|Có| ShowDialog[Hiển thị dialog<br/>xác nhận]
    ShowDialog --> UserChoice{Người dùng<br/>chọn?}
    UserChoice -->|Chấp nhận| Accept[Gửi FRIEND_ACCEPT]
    UserChoice -->|Từ chối| Reject[Gửi FRIEND_REJECT]
    Accept --> SaveFriend[Lưu vào database<br/>FriendDAO]
    SaveFriend --> CreateConv[Tạo Conversation mới]
    CreateConv --> End([Hoàn thành])
    Reject --> End
```

### 2.4. Quy Trình Peer Discovery

```mermaid
flowchart TD
    Start([Khởi động DiscoveryService]) --> JoinMulticast[Join Multicast Group<br/>230.0.0.1:4446]
    JoinMulticast --> StartHeartbeat[Bắt đầu gửi Heartbeat<br/>mỗi 2 giây]
    StartHeartbeat --> StartListener[Bắt đầu lắng nghe<br/>Heartbeat từ peers khác]
    
    StartListener --> ReceiveHeartbeat{Nhận Heartbeat?}
    ReceiveHeartbeat -->|Có| CheckSelf{Là chính mình?}
    CheckSelf -->|Đúng| ReceiveHeartbeat
    CheckSelf -->|Sai| CheckExists{Peer đã tồn tại<br/>trong danh sách?}
    
    CheckExists -->|Có| UpdateLastSeen[Cập nhật lastSeen]
    CheckExists -->|Chưa| AddPeer[Thêm peer mới<br/>vào danh sách]
    
    UpdateLastSeen --> ReceiveHeartbeat
    AddPeer --> ReceiveHeartbeat
    
    StartHeartbeat --> Cleanup[Kiểm tra peers<br/>offline mỗi 6 giây]
    Cleanup --> CheckAlive{Peer còn<br/>hoạt động?}
    CheckAlive -->|Không| RemovePeer[Xóa peer khỏi<br/>danh sách]
    CheckAlive -->|Có| Cleanup
    RemovePeer --> Cleanup
```

## 3. Sơ Đồ Lớp Tổng Quát (Class Diagram)

```mermaid
classDiagram
    class P2PApplication {
        -Stage mainStage
        -Peer localPeer
        -PeerDiscoveryService discoveryService
        -MessageService messageService
        -VoiceCallService voiceCallService
        +setLocalPeer(Peer)
        +getLocalPeer() Peer
        +startMainApp()
        +start(Stage)
    }
    
    class MainController {
        -Peer localPeer
        -Conversation selectedConversation
        -Map~String,Conversation~ conversations
        -Set~String~ friends
        -PeerDiscoveryService discoveryService
        -MessageService messageService
        -VoiceCallService voiceCallService
        +setContext(...)
        +sendMessage()
        +openConversation(Conversation)
        +createGroup(String, List~Peer~)
        +sendFile(File)
        +sendImage(File)
    }
    
    class LoginController {
        -TextField usernameField
        -PasswordField passwordField
        +onLogin()
        +onRegister()
    }
    
    class AddFriendController {
        -Peer localPeer
        -MessageService messageService
        +setContext(...)
        +sendRequest()
    }
    
    class Peer {
        -String id
        -String name
        -InetAddress address
        -int textPort
        -int voicePort
        -int signalPort
        -LocalDateTime lastSeen
        +updateLastSeen()
        +isAlive() boolean
    }
    
    class Message {
        -String id
        -Type type
        -Status status
        -String from
        -String fromName
        -String target
        -String content
        -String groupId
        -String fileName
        -long fileSize
        +friendRequest(Peer, Peer) Message
        +groupText(...) Message
        +fileMeta(...) Message
    }
    
    class Conversation {
        -String id
        -String name
        -boolean isGroup
        -Peer peer
        -Group group
        -List~Message~ messages
        -int unreadCount
        +addMessage(Message)
        +incrementUnreadCount()
    }
    
    class Group {
        -String id
        -String name
        -String ownerId
        -Set~String~ members
        +addMember(String)
        +isMember(String) boolean
    }
    
    class PeerDiscoveryService {
        -Peer localPeer
        -ObservableList~Peer~ peers
        -MulticastSocket socket
        -boolean running
        +start()
        +stop()
        +getPeers() ObservableList~Peer~
    }
    
    class MessageService {
        -Peer localPeer
        -DatagramSocket textSocket
        -DatagramSocket signalSocket
        -Consumer~Message~ onPrivateMessage
        -Consumer~Message~ onSignalMessage
        +start()
        +stop()
        +sendPrivateMessage(Message, Peer)
        +sendSignalMessage(Message, Peer)
    }
    
    class VoiceCallService {
        -Peer localPeer
        -DatagramSocket voiceSocket
        +start()
        +stop()
    }
    
    class FileTransferService {
        +sendFile(File, int port)
        +receiveFile(File, String host, int port, long size)
    }
    
    class UserDAO {
        +register(String, String, String) boolean
        +login(String, String) String
        +getUsernameByPeerId(String) String
    }
    
    class FriendDAO {
        +saveFriend(String, String)
        +deleteFriend(String, String)
        +loadFriends(String) Set~String~
    }
    
    class GroupDAO {
        +saveGroup(Group, String)
        +saveMember(String, String)
        +loadGroups(String) Map~String,Group~
        +deleteGroup(String)
    }
    
    class Database {
        +getConnection() Connection
    }
    
    P2PApplication --> MainController
    P2PApplication --> LoginController
    P2PApplication --> PeerDiscoveryService
    P2PApplication --> MessageService
    P2PApplication --> VoiceCallService
    P2PApplication --> Peer
    
    MainController --> Conversation
    MainController --> Peer
    MainController --> Group
    MainController --> Message
    MainController --> PeerDiscoveryService
    MainController --> MessageService
    MainController --> FileTransferService
    MainController --> FriendDAO
    MainController --> GroupDAO
    
    LoginController --> UserDAO
    LoginController --> Peer
    
    AddFriendController --> Peer
    AddFriendController --> MessageService
    AddFriendController --> PeerDiscoveryService
    
    Conversation --> Peer
    Conversation --> Group
    Conversation --> Message
    
    Group --> Peer : members
    
    MessageService --> Peer
    MessageService --> Message
    
    PeerDiscoveryService --> Peer
    PeerDiscoveryService --> Message
    
    UserDAO --> Database
    FriendDAO --> Database
    GroupDAO --> Database
```

## 4. Biểu Đồ Tuần Tự (Sequence Diagrams)

### 4.1. Đăng Nhập và Khởi Tạo Hệ Thống

```mermaid
sequenceDiagram
    participant User
    participant LoginController
    participant UserDAO
    participant P2PApplication
    participant PeerDiscoveryService
    participant MessageService
    participant VoiceCallService
    participant MainController
    
    User->>LoginController: Nhập username/password
    LoginController->>UserDAO: login(username, password)
    UserDAO-->>LoginController: peerId
    LoginController->>P2PApplication: setLocalPeer(peer)
    LoginController->>P2PApplication: startMainApp()
    
    P2PApplication->>P2PApplication: attachNetworkInfo(peer)
    P2PApplication->>PeerDiscoveryService: new PeerDiscoveryService(peer)
    P2PApplication->>PeerDiscoveryService: start()
    P2PApplication->>MessageService: new MessageService(peer)
    P2PApplication->>MessageService: start()
    P2PApplication->>VoiceCallService: new VoiceCallService(peer)
    P2PApplication->>VoiceCallService: start()
    
    P2PApplication->>MainController: setContext(peer, services)
    MainController->>MainController: loadConversations()
    MainController->>MainController: setupCallbacks()
    MainController-->>User: Hiển thị giao diện chính
```

### 4.2. Gửi Tin Nhắn Riêng Tư

```mermaid
sequenceDiagram
    participant User
    participant MainController
    participant MessageService
    participant Message
    participant Peer
    participant TargetPeer
    
    User->>MainController: Nhập tin nhắn và nhấn Send
    MainController->>MainController: sendMessage()
    MainController->>MainController: sendPrivate(text)
    MainController->>Message: new Message(TEXT, from, content)
    MainController->>MainController: findPeerById(targetId)
    MainController->>MessageService: sendPrivateMessage(msg, target)
    MessageService->>MessageService: Serialize Message to JSON
    MessageService->>TargetPeer: UDP DatagramPacket<br/>(textPort)
    
    TargetPeer->>MessageService: Nhận DatagramPacket
    MessageService->>MessageService: Deserialize JSON to Message
    MessageService->>MainController: onPrivateMessage callback
    MainController->>MainController: conversations.computeIfAbsent()
    MainController->>MainController: addMessage(msg)
    MainController->>MainController: displayMessage(msg, false)
    MainController-->>User: Hiển thị tin nhắn trên UI
```

### 4.3. Thêm Bạn Bè

```mermaid
sequenceDiagram
    participant UserA
    participant MainControllerA
    participant AddFriendControllerA
    participant MessageServiceA
    participant Message
    participant MessageServiceB
    participant MainControllerB
    participant UserB
    participant FriendDAO
    
    UserA->>MainControllerA: Mở dialog thêm bạn
    MainControllerA->>AddFriendControllerA: openAddFriendDialog()
    AddFriendControllerA->>AddFriendControllerA: Hiển thị danh sách peers online
    UserA->>AddFriendControllerA: Chọn peer và gửi request
    AddFriendControllerA->>Message: Message.friendRequest(localPeer, target)
    AddFriendControllerA->>MessageServiceA: sendSignalMessage(msg, target)
    MessageServiceA->>MessageServiceB: UDP Signal Port
    
    MessageServiceB->>MainControllerB: onSignalMessage callback
    MainControllerB->>UserB: Hiển thị dialog xác nhận
    UserB->>MainControllerB: Chấp nhận
    MainControllerB->>FriendDAO: saveFriend(localPeerId, friendId)
    MainControllerB->>Message: Message.friendAccept(localPeer, target)
    MainControllerB->>MessageServiceB: sendSignalMessage(accept, target)
    MessageServiceB->>MessageServiceA: UDP Signal Port
    
    MessageServiceA->>MainControllerA: onSignalMessage callback
    MainControllerA->>FriendDAO: saveFriend(localPeerId, friendId)
    MainControllerA->>MainControllerA: Tạo Conversation mới
    MainControllerA-->>UserA: Hiển thị bạn bè mới trong danh sách
```

### 4.4. Peer Discovery (Khám Phá Peer)

```mermaid
sequenceDiagram
    participant PeerA
    participant PeerDiscoveryServiceA
    participant MulticastGroup
    participant PeerDiscoveryServiceB
    participant PeerB
    
    Note over PeerA,PeerB: Khởi động Discovery Service
    
    PeerA->>PeerDiscoveryServiceA: start()
    PeerDiscoveryServiceA->>MulticastGroup: Join 230.0.0.1:4446
    PeerDiscoveryServiceA->>PeerDiscoveryServiceA: startHeartbeat()
    PeerDiscoveryServiceA->>PeerDiscoveryServiceA: startListening()
    
    loop Mỗi 2 giây
        PeerDiscoveryServiceA->>Message: new Message(HEARTBEAT, ...)
        PeerDiscoveryServiceA->>MulticastGroup: Gửi Heartbeat
        MulticastGroup->>PeerDiscoveryServiceB: Broadcast Heartbeat
        PeerDiscoveryServiceB->>PeerDiscoveryServiceB: handleHeartbeat()
        PeerDiscoveryServiceB->>PeerDiscoveryServiceB: Kiểm tra peer đã tồn tại?
        alt Peer mới
            PeerDiscoveryServiceB->>PeerB: Thêm vào danh sách peers
        else Peer đã tồn tại
            PeerDiscoveryServiceB->>PeerB: updateLastSeen()
        end
    end
    
    loop Mỗi 6 giây
        PeerDiscoveryServiceB->>PeerB: Kiểm tra isAlive()
        alt Peer offline
            PeerDiscoveryServiceB->>PeerB: Xóa khỏi danh sách
        end
    end
```

### 4.5. Gửi Tin Nhắn Nhóm

```mermaid
sequenceDiagram
    participant UserA
    participant MainControllerA
    participant MessageServiceA
    participant Group
    participant MessageServiceB
    participant MainControllerB
    participant UserB
    participant MessageServiceC
    participant MainControllerC
    participant UserC
    
    UserA->>MainControllerA: Gửi tin nhắn trong nhóm
    MainControllerA->>MainControllerA: sendGroup(text)
    MainControllerA->>Message: Message.groupText(localPeer, groupId, content)
    MainControllerA->>Group: getMembers()
    Group-->>MainControllerA: [member1, member2, ...]
    
    loop Cho mỗi thành viên
        MainControllerA->>MainControllerA: findPeerById(memberId)
        MainControllerA->>MessageServiceA: sendPrivateMessage(msg, member)
        MessageServiceA->>MessageServiceB: UDP Text Port (member1)
        MessageServiceA->>MessageServiceC: UDP Text Port (member2)
    end
    
    MessageServiceB->>MainControllerB: onPrivateMessage callback
    MainControllerB->>MainControllerB: Kiểm tra type == GROUP_TEXT
    MainControllerB->>MainControllerB: conversations.computeIfAbsent(groupId)
    MainControllerB->>MainControllerB: addMessage(msg)
    MainControllerB-->>UserB: Hiển thị tin nhắn nhóm
    
    MessageServiceC->>MainControllerC: onPrivateMessage callback
    MainControllerC->>MainControllerC: Kiểm tra type == GROUP_TEXT
    MainControllerC->>MainControllerC: conversations.computeIfAbsent(groupId)
    MainControllerC->>MainControllerC: addMessage(msg)
    MainControllerC-->>UserC: Hiển thị tin nhắn nhóm
```

### 4.6. Gửi File

```mermaid
sequenceDiagram
    participant UserA
    participant MainControllerA
    participant FileTransferService
    participant MessageServiceA
    participant Message
    participant MessageServiceB
    participant MainControllerB
    participant FileTransferServiceB
    participant UserB
    
    UserA->>MainControllerA: Chọn file để gửi
    MainControllerA->>MainControllerA: chooseFile()
    MainControllerA->>MainControllerA: sendFile(file)
    MainControllerA->>Message: Message.fileMeta(..., fileId, fileName, size, port)
    MainControllerA->>MessageServiceA: sendSignalMessage(meta, target)
    MessageServiceA->>MessageServiceB: UDP Signal Port (file metadata)
    
    MainControllerB->>MainControllerB: handleFileMeta(msg)
    MainControllerB->>MainControllerB: addMessage(msg)
    MainControllerB-->>UserB: Hiển thị file message với nút download
    
    MainControllerA->>FileTransferService: sendFile(file, port)
    FileTransferService->>FileTransferService: Tạo ServerSocket
    FileTransferService->>FileTransferService: Chờ kết nối từ receiver
    
    UserB->>MainControllerB: Click download
    MainControllerB->>FileTransferServiceB: receiveFile(saveTo, host, port, size)
    FileTransferServiceB->>FileTransferService: Kết nối Socket
    FileTransferService->>FileTransferServiceB: Gửi file data
    FileTransferServiceB->>FileTransferServiceB: Lưu file vào disk
    FileTransferServiceB-->>UserB: File đã tải xong
```

### 4.7. Thu Hồi Tin Nhắn

```mermaid
sequenceDiagram
    participant UserA
    participant MainControllerA
    participant MessageServiceA
    participant Message
    participant MessageServiceB
    participant MainControllerB
    participant UserB
    
    UserA->>MainControllerA: Click "Thu hồi" trên tin nhắn
    MainControllerA->>MainControllerA: recallMessage(msg)
    MainControllerA->>Message: new Message(RECALL, ...)
    MainControllerA->>Message: setReplyToMessageId(msgId)
    
    alt Tin nhắn nhóm
        MainControllerA->>MainControllerA: Lấy danh sách members
        loop Cho mỗi member
            MainControllerA->>MessageServiceA: sendSignalMessage(recall, member)
            MessageServiceA->>MessageServiceB: UDP Signal Port
        end
    else Tin nhắn riêng
        MainControllerA->>MessageServiceA: sendSignalMessage(recall, target)
        MessageServiceA->>MessageServiceB: UDP Signal Port
    end
    
    MainControllerA->>MainControllerA: msg.setRecalled(true)
    MainControllerA->>MainControllerA: refreshChat()
    MainControllerA-->>UserA: Hiển thị "Tin nhắn đã được thu hồi"
    
    MessageServiceB->>MainControllerB: onSignalMessage callback
    MainControllerB->>MainControllerB: handleRecall(recall)
    MainControllerB->>MainControllerB: Tìm message theo replyToMessageId
    MainControllerB->>MainControllerB: message.setRecalled(true)
    MainControllerB->>MainControllerB: refreshChat()
    MainControllerB-->>UserB: Hiển thị "Tin nhắn đã được thu hồi"
```

---

## Ghi Chú

### Kiến Trúc Hệ Thống

1. **P2P Architecture**: Hệ thống sử dụng kiến trúc peer-to-peer, không có server trung tâm
2. **UDP Communication**: Tất cả giao tiếp sử dụng UDP sockets
   - Text Port: Tin nhắn văn bản
   - Signal Port: Tín hiệu điều khiển (friend request, group invite, etc.)
   - Voice Port: Cuộc gọi thoại/video
3. **Multicast Discovery**: Sử dụng UDP Multicast để khám phá peers trong mạng LAN
4. **Database**: SQLite để lưu trữ thông tin người dùng, bạn bè, và nhóm chat

### Các Port Sử Dụng

- **Multicast Port**: 4446 (Discovery)
- **Text Port**: 52000-52999 (Random)
- **Voice Port**: TextPort + 1
- **Signal Port**: TextPort + 2
- **File Transfer Port**: 54000-54999 (Random)
- **Image Transfer Port**: 55000-55999 (Random)

### Luồng Dữ Liệu

1. **Heartbeat**: Gửi mỗi 2 giây qua Multicast
2. **Peer Timeout**: 6 giây không nhận heartbeat → coi là offline
3. **Message Types**: TEXT, IMAGE, FILE_META, GROUP_TEXT, FRIEND_REQUEST, etc.
4. **Signal Messages**: Điều khiển hệ thống (friend, group, recall, etc.)

