# UITHub

**Ứng dụng Android quản lý thông tin sinh viên** — tra cứu lịch thi, điểm số, thông báo, v.v.

## Tính năng chính

- 📅 **Lịch thi** — Xem danh sách lịch thi, hiển thị số ngày còn lại, trạng thái "Sắp thi" / "Đã thi"
- 📊 **Điểm số** — Tra cứu điểm từng môn học
- 🔔 **Thông báo** — Cập nhật tin tức từ trường
- 👤 **Hồ sơ** — Thông tin cá nhân và cài đặt
- 🔐 **Đăng nhập** — Xác thực tài khoản sinh viên

## Công nghệ sử dụng

| Thành phần  | Công nghệ                                        |
|-------------|--------------------------------------------------|
| Ngôn ngữ    | Java                                             |
| UI          | XML (ConstraintLayout, RecyclerView, ViewPager2) |
| Networking  | Retrofit + Gson                                  |
| Backend API | REST                                             |
| Build tool  | Gradle (Groovy)                                  |

## Cấu trúc thư mục

```
app/src/main/java/com/example/uithub/
├── adapter/           # Adapter cho RecyclerView
│   ├── ExamScheduleAdapter.java    # Adapter hiển thị lịch thi
│   └── ScheduleItemAdapter.java    # Adapter hiển thị thời khóa biểu
├── api/               # API service (Retrofit interface)
├── models/            # Các model class (POJO)
├── utils/             # Utility classes
├── BaseActivity.java
├── GradeDetailActivity.java
├── LoginActivity.java / LoginFragment.java
├── ScheduleFragment.java     # Fragment lịch thi
├── StudyFragment.java        # Fragment thời khóa biểu
├── ProfileFragment.java
└── AnnouncementParser.java   # Parse thông báo
```

## Hướng dẫn build

### Yêu cầu

- Android Studio (phiên bản mới nhất)
- JDK 17+
- Gradle (tự động tải qua Gradle Wrapper)

### Các bước

1. **Clone source**
   ```bash
   git clone https://github.com/doqin/UITHub.git
   cd UITHub
   ```

2. **Mở bằng Android Studio**
    - File → Open → chọn thư mục `UITHub`
    - Đợi Gradle sync hoàn tất

3. **Build & chạy**
    - Chọn device (máy thật hoặc emulator)
    - Nhấn **Run** (▶) hoặc dùng lệnh:
   ```bash
   ./gradlew assembleDebug
   ```
