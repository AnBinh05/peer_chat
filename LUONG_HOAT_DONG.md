# LUỒNG HOẠT ĐỘNG

## 1. Đăng ký (Register)

**User nhập thông tin → Click "Register" → UserDAO.register() → INSERT vào Database → Thông báo "Account created!"**

Chi tiết:
- User nhập username và password
- Click nút "Register"
- LoginController gọi `UserDAO.register(peerId, username, password)`
- Tạo UUID peerId tự động
- Lưu vào bảng `users` trong Database
- Hiển thị thông báo thành công

---

## 2. Đăng nhập (Login)

**User nhập thông tin → Click "Login" → UserDAO.login() → SELECT từ Database → Tạo Peer object → Khởi động Services → Hiển thị MainView**

Chi tiết:
- User nhập username và password
- Click nút "Login"
- LoginController gọi `UserDAO.login(username, password)`
- Kiểm tra trong Database, trả về `peerId` nếu hợp lệ
- Tạo `Peer` object với `peerId` và `username`
- Gọi `P2PApplication.setLocalPeer(peer)`
- Gọi `P2PApplication.startMainApp()`
- Khởi động các Services:
  - PeerDiscoveryService (khám phá peers)
  - MessageService (gửi/nhận tin nhắn)
  - VoiceCallService (gọi thoại/video)
- Load MainView.fxml và hiển thị giao diện chat

---

## 3. Khám phá Peer (Peer Discovery)

**Peer A khởi động → Join Multicast Group (230.0.0.1:4446) → Gửi HEARTBEAT mỗi 2 giây → Peer B nhận HEARTBEAT → Thêm vào danh sách peers → Hiển thị trong UI**

Chi tiết:
- Mỗi peer khi khởi động sẽ join Multicast Group `230.0.0.1:4446`
- Tự động gửi HEARTBEAT message mỗi 2 giây qua UDP Multicast
- HEARTBEAT chứa: `peerId`, `name`, `textPort`, `voicePort`, `signalPort`
- Peers khác nhận HEARTBEAT → Kiểm tra peerId
  - Nếu peer mới → Tạo Peer object và thêm vào danh sách
  - Nếu peer đã tồn tại → Update `lastSeen` timestamp
- Hiển thị danh sách peers online trong UI
- Sau 6 giây không nhận HEARTBEAT → Peer bị đánh dấu offline và xóa khỏi danh sách

---

## 4. Thêm bạn bè (Add Friend)

**User A click "Add Friend" → Hiển thị danh sách peers online → Chọn peer B → Click "Send Request" → Gửi FRIEND_REQUEST qua UDP → Peer B nhận → Hiển thị dialog → Chấp nhận/Từ chối → Lưu vào Database**

Chi tiết:
- User A click nút "Add Friend"
- Hiển thị dialog với danh sách peers online (từ PeerDiscoveryService)
- User A chọn peer B và click "Send Request"
- MainController tạo `FRIEND_REQUEST` message
- Gửi qua `MessageService.sendSignalMessage()` → UDP đến peer B
- Peer B nhận message → `handleFriendRequest()` → Hiển thị dialog "Peer A wants to be friends"
- Nếu chấp nhận:
  - Lưu vào bảng `friends` trong Database (cả 2 chiều)
  - Gửi `FRIEND_ACCEPT` message về peer A
  - Thêm vào danh sách bạn bè và hiển thị trong UI
- Nếu từ chối: Không làm gì

---

## 5. Gửi tin nhắn văn bản (Send Text Message)

**User A nhập tin nhắn → Click "Send" → Tạo TEXT message → Hiển thị trong chat (màu xanh) → Gửi qua UDP đến peer B → Peer B nhận → Thêm vào conversation → Hiển thị trong chat (màu trắng)**

Chi tiết:
- User A chọn conversation với peer B
- Nhập tin nhắn vào TextArea và click "Send"
- MainController tạo `Message` object với type `TEXT`
- Hiển thị tin nhắn ngay lập tức trong UI (màu xanh - tin nhắn của mình)
- Gọi `MessageService.sendPrivateMessage(msg, peerB)`
- Gửi qua UDP đến `peerB.getTextPort()`
- Peer B nhận message → `onPrivateMessage` callback
- Tìm hoặc tạo Conversation cho peer A
- Thêm message vào conversation → `addMessage(msg)`
- Nếu conversation đang mở: Hiển thị tin nhắn (màu trắng - tin nhắn nhận)
- Nếu conversation chưa mở: Tăng `unreadCount` và hiển thị badge số tin nhắn chưa đọc

---

## 6. Gửi emoji (Send Emoji)

**User click emoji icon → Hiển thị emoji picker → Chọn emoji → Tạo message với content ":emoji_file:" → Gửi như tin nhắn text → Peer nhận → Parse emoji → Hiển thị emoji image**

Chi tiết:
- User click icon emoji
- MainController load tất cả file PNG trong folder `/com/p2p/view/emoji`
- Hiển thị emoji picker với grid layout
- User chọn emoji
- Tạo message với content dạng `:emoji_file_name:` (ví dụ: `:smile.png:`)
- Gửi như tin nhắn text thông thường
- Peer nhận message → Parse content
- Nếu content bắt đầu và kết thúc bằng `:`, load file emoji tương ứng
- Hiển thị emoji image trong chat

---

## 7. Gửi file (Send File)

**User click attach icon → Chọn file → Tạo FILE_META message → Gửi metadata qua UDP → Tạo port TCP ngẫu nhiên → Mở ServerSocket → Gửi file qua TCP → Peer nhận metadata → Kết nối TCP → Nhận file → Lưu vào thư mục downloads**

Chi tiết:
- User click icon attach
- Mở FileChooser với filter: `*.txt, *.pdf, *.doc, *.docx, *.xls, *.xlsx, *.zip`
- User chọn file
- Tạo `fileId` (UUID) và `filePort` (54000-54999 ngẫu nhiên)
- Tạo `FILE_META` message chứa: `fileId`, `fileName`, `fileSize`, `filePort`
- Gửi FILE_META qua UDP signal port đến peer
- Đồng thời, `FileTransferService.sendFile()` mở ServerSocket trên `filePort`
- Peer nhận FILE_META → Hiển thị file message trong chat với tên file và size
- User click vào file → `FileTransferService.receiveFile()` kết nối TCP đến sender
- Nhận file data qua TCP stream
- Lưu file vào thư mục `downloads/` hoặc do user chọn vị trí lưu

---

## 8. Gửi hình ảnh (Send Image)

**User click image icon → Chọn hình ảnh → Tạo IMAGE message → Gửi metadata qua UDP → Gửi image qua TCP → Peer nhận → Tự động lưu vào received_images/ → Hiển thị preview trong chat**

Chi tiết:
- User click icon image
- Mở FileChooser với filter: `*.png, *.jpg, *.jpeg, *.gif`
- User chọn hình ảnh
- Tạo `IMAGE` message với `fileId` và `filePort` (55000-55999)
- Hiển thị preview hình ảnh ngay trong chat (màu xanh)
- Gửi IMAGE metadata qua UDP
- Gửi image data qua TCP tương tự gửi file
- Peer nhận IMAGE metadata → Tự động gọi `receiveFile()` (không cần user click)
- Lưu vào thư mục `received_images/`
- Hiển thị hình ảnh trong chat (màu trắng)

---

## 9. Thu hồi tin nhắn (Recall Message)

**User click chuột phải → Chọn "Thu hồi" → Tạo RECALL message → Gửi đến peer → Peer nhận → Tìm message gốc → Đánh dấu recalled → Hiển thị "Tin nhắn đã được thu hồi"**

Chi tiết:
- User click chuột phải vào tin nhắn của mình
- Chọn "Thu hồi" từ context menu
- MainController tạo `RECALL` message với `replyToMessageId` = ID của tin nhắn cần thu hồi
- Gửi RECALL message qua UDP signal port
- Peer nhận RECALL → `handleRecall()`
- Tìm message gốc trong conversation dựa vào `replyToMessageId`
- Đánh dấu `message.setRecalled(true)`
- Refresh chat UI → Hiển thị "Tin nhắn đã được thu hồi" (màu xám, in nghiêng)

---

## 10. Trả lời tin nhắn (Reply Message)

**User click chuột phải → Chọn "Trả lời" → Set replyTarget → Hiển thị "Replying to: ..." → Nhập tin nhắn → Gửi → Tin nhắn mới chứa replyToMessageId → Peer nhận → Hiển thị liên kết đến tin nhắn gốc**

Chi tiết:
- User click chuột phải vào tin nhắn (bất kỳ)
- Chọn "Trả lời" từ context menu
- MainController set `replyTarget = message`
- Hiển thị status "Replying to: [nội dung tin nhắn]"
- User nhập tin nhắn mới và gửi
- Tin nhắn mới có `replyToMessageId = replyTarget.getId()`
- Gửi như tin nhắn thông thường
- Peer nhận → Parse `replyToMessageId` và hiển thị liên kết đến tin nhắn gốc (nếu cần)

---

## 11. Tạo nhóm (Create Group)

**User click "Create Group" → Nhập tên nhóm → Chọn thành viên → Click "Create" → Lưu vào Database → Gửi GROUP_INVITE đến từng thành viên → Thành viên chấp nhận → Lưu vào group_members → Hiển thị nhóm trong danh sách**

Chi tiết:
- User click nút "Create Group"
- Hiển thị dialog với:
  - TextField: Tên nhóm
  - ListView: Danh sách bạn bè (multi-select)
- User nhập tên và chọn thành viên, click "Create"
- Tạo `Group` object với UUID id
- `GroupDAO.saveGroup()` → Lưu vào bảng `groups`
- Thêm localPeer vào members và lưu vào `group_members`
- Với mỗi thành viên được chọn:
  - Tạo `GROUP_INVITE` message
  - Gửi qua UDP đến thành viên
  - Thành viên nhận → Hiển thị dialog "Join group [tên nhóm]?"
  - Nếu chấp nhận: `GroupDAO.saveMember()` → Lưu vào `group_members`
- Tạo Conversation cho nhóm và hiển thị trong danh sách

---

## 12. Gửi tin nhắn nhóm (Send Group Message)

**User chọn nhóm → Nhập tin nhắn → Click "Send" → Tạo GROUP_TEXT message → Gửi đến từng thành viên (trừ người gửi) → Thành viên nhận → Thêm vào conversation nhóm → Hiển thị**

Chi tiết:
- User chọn conversation nhóm
- Nhập tin nhắn và click "Send"
- MainController tạo `GROUP_TEXT` message với `groupId` và `groupName`
- Hiển thị tin nhắn trong chat (màu xanh)
- Lấy danh sách members từ Group object
- Với mỗi member (trừ localPeer):
  - Gửi GROUP_TEXT message qua UDP đến member
- Member nhận → `onPrivateMessage` callback
- Kiểm tra `msg.getGroupId()` → Tìm hoặc tạo Conversation cho nhóm
- Thêm message vào conversation
- Hiển thị trong UI nếu conversation đang mở, hoặc tăng unreadCount

---

## 13. Rời nhóm (Leave Group)

**User click chuột phải vào nhóm → Chọn "Leave group" → Xác nhận → Xóa khỏi group_members → Xóa conversation → Xóa khỏi UI**

Chi tiết:
- User click chuột phải vào nhóm trong danh sách
- Chọn "Leave group" từ context menu
- Hiển thị dialog xác nhận "Leave group [tên]?"
- Nếu xác nhận:
  - `GroupDAO.removeMember(groupId, localPeer.getId())` → Xóa khỏi Database
  - Xóa conversation khỏi `conversations` map
  - Xóa khỏi `conversationListView`
  - Clear chat messages area

---

## 14. Xóa nhóm (Delete Group)

**Owner click chuột phải → Chọn "Delete group" → Xác nhận → Gửi GROUP_DELETE đến tất cả members → Xóa khỏi Database → Members nhận → Xóa conversation**

Chi tiết:
- Chỉ owner của nhóm mới có quyền xóa
- Owner click chuột phải → Chọn "Delete group"
- Hiển thị dialog xác nhận
- Nếu xác nhận:
  - Tạo `GROUP_DELETE` message
  - Gửi đến tất cả members online
  - `GroupDAO.deleteGroup(groupId)` → Xóa khỏi Database (cả `groups` và `group_members`)
  - Xóa conversation khỏi UI
- Members nhận GROUP_DELETE:
  - `handleGroupDelete()` → Xóa conversation và cập nhật UI

---

## 15. Gọi thoại (Voice Call)

**User chọn peer → Click "Call" → Gửi CALL_REQUEST → Peer nhận → Hiển thị dialog "Incoming Call" → Chấp nhận → Gửi CALL_ACCEPT → Khởi động audio streaming → Kết nối cuộc gọi**

Chi tiết:
- User chọn peer từ danh sách bạn bè (không phải nhóm)
- Click nút "Call" (hoặc "Video Call")
- `VoiceCallService.initiateCall(peer, isVideo=false)`
- Kiểm tra: đang trong cuộc gọi? peer online?
- Tạo `CALL_REQUEST` message → Gửi qua UDP signal port
- Peer nhận → `handleCallSignal()` → `onIncomingCall` callback
- Hiển thị call dialog "Incoming Call from [tên]"
- Nếu chấp nhận:
  - `acceptCall()` → Gửi `CALL_ACCEPT`
  - `startAudioStreaming()` → Mở microphone và speaker
  - Gửi audio data qua UDP voice port mỗi buffer
- Caller nhận CALL_ACCEPT → `startAudioStreaming()`
- Cuộc gọi kết nối, audio streaming hai chiều

---

## 16. Kết thúc cuộc gọi (End Call)

**User click "Hangup" → Gửi CALL_END → Dừng audio streaming → Đóng microphone/speaker → Đóng call dialog → Peer nhận CALL_END → Làm tương tự**

Chi tiết:
- Bất kỳ bên nào click "Hangup"
- `VoiceCallService.endCall()`
- Gửi `CALL_END` message đến peer
- `stopAudioStreaming()`:
  - Dừng và đóng microphone (TargetDataLine)
  - Dừng và đóng speaker (SourceDataLine)
  - Interrupt audio sender/receiver threads
- Set `inCall = false`
- Đóng call dialog
- Peer nhận CALL_END → Thực hiện tương tự

---

## 17. Từ chối cuộc gọi (Reject Call)

**Peer nhận CALL_REQUEST → Hiển thị dialog → Click "Reject" → Gửi CALL_REJECT → Đóng dialog → Caller nhận → Hiển thị "Call Rejected" → Đóng dialog**

Chi tiết:
- Peer nhận CALL_REQUEST → Hiển thị call dialog
- User click "Reject"
- `VoiceCallService.rejectCall(caller)`
- Gửi `CALL_REJECT` message
- Đóng call dialog
- Caller nhận CALL_REJECT:
  - `onCallRejected` callback
  - Hiển thị thông báo "Call Rejected"
  - Đóng call dialog, reset `inCall = false`

---

## 18. Tìm kiếm conversation (Search Conversation)

**User nhập từ khóa vào search box → Filter conversations → Hiển thị kết quả khớp → Clear search → Hiển thị lại tất cả**

Chi tiết:
- User nhập text vào TextField search
- `filterConversations(keyword)` được gọi
- Filter `conversations` map:
  - Lọc theo `conversation.getName().toLowerCase().contains(keyword)`
- Cập nhật `conversationListView` với kết quả filter
- Xóa search text → Hiển thị lại tất cả conversations

---

## 19. Đánh dấu đã đọc (Read Receipt)

**User mở conversation → Reset unreadCount → Gửi READ_RECEIPT cho các tin nhắn chưa đọc → Peer nhận → Cập nhật status message thành READ → Hiển thị "✓✓ Read"**

Chi tiết:
- User click vào conversation trong danh sách
- `openConversation()` được gọi
- `conversation.resetUnreadCount()`
- Với mỗi tin nhắn chưa đọc (status = SENT):
  - Tạo `READ_RECEIPT` message với `replyToMessageId = messageId`
  - Gửi đến người gửi
- Người gửi nhận READ_RECEIPT:
  - Tìm message trong conversation
  - `message.setStatus(Status.READ)`
  - Refresh UI → Hiển thị "✓✓ Read" thay vì "✓ Sent"

---

## 20. Khởi động ứng dụng (Application Startup)

**Chạy P2PApplication.main() → Load login.fxml → Hiển thị Login Screen → User đăng nhập → Tạo Peer → Khởi động Services → Load MainView → Hiển thị giao diện chính**

Chi tiết:
- `P2PApplication.main()` được gọi
- JavaFX `launch()` → `start(Stage)`
- Load `login.fxml` → Hiển thị Login Screen
- User thực hiện đăng nhập (xem flow 2)
- Sau khi đăng nhập thành công:
  - `startMainApp()` được gọi
  - `attachNetworkInfo(peer)` → Gán IP và ports (52000-52999)
  - `startServices()`:
    1. PeerDiscoveryService.start() → Join multicast, bắt đầu heartbeat
    2. MessageService.start() → Mở textSocket và signalSocket
    3. VoiceCallService.start() → Mở voiceSocket
  - Load `MainView.fxml`
  - `MainController.setContext()` → Setup callbacks, load conversations
  - Hiển thị giao diện chính với danh sách conversations

---

## 21. Shutdown ứng dụng (Application Shutdown)

**User đóng cửa sổ → onCloseRequest → Shutdown Services → Đóng sockets → Đóng threads → Platform.exit()**

Chi tiết:
- User click nút X để đóng cửa sổ
- `mainStage.setOnCloseRequest()` được trigger
- `shutdown()` được gọi:
  - `VoiceCallService.stop()` → Dừng cuộc gọi nếu có, đóng voiceSocket
  - `MessageService.stop()` → Đóng textSocket và signalSocket
  - `PeerDiscoveryService.stop()` → Leave multicast group, đóng socket, shutdown scheduler
- `Platform.exit()` → Thoát ứng dụng

---

## 📊 Tổng Kết Các Port và Protocol

| Chức năng | Port | Protocol | Mô tả |
|-----------|------|----------|-------|
| Discovery | 4446 | UDP Multicast | Heartbeat, khám phá peers |
| Text Message | 52000-52999 | UDP | Gửi/nhận tin nhắn văn bản |
| Signal | 53000-53999 | UDP | Signal messages (friend, group, call, etc.) |
| Voice | 54000-54999 | UDP | Audio streaming cho cuộc gọi |
| File Transfer | 55000-55999 | TCP | Gửi/nhận file |
| Image Transfer | 56000-56999 | TCP | Gửi/nhận hình ảnh |

---

## 🔄 Luồng Tổng Quan

```
Khởi động → Đăng nhập → Khám phá Peers → Thêm bạn bè → Chat/Gọi
                                              ↓
                                          Tạo nhóm → Chat nhóm
```

