# Jobday Android

Ứng dụng tuyển dụng Jobday dành cho Android, xây dựng bằng Kotlin và giao diện XML truyền thống. Dự án sử dụng kiến trúc đa module, MVVM kết hợp UDF, Hilt và Navigation Component.

## Tính năng hiện có

- Màn chào và chế độ khám phá không cần đăng nhập.
- Trang chủ với danh mục nhanh, gợi ý việc làm và bottom navigation.
- Đăng ký/đăng nhập Candidate không mật khẩu bằng số điện thoại và OTP.
- Tự động phân biệt tài khoản mới và tài khoản đã có thông qua thông tin người dùng.
- Onboarding nhập họ tên cho Candidate chưa hoàn thiện tài khoản.
- Tab Tài khoản hiển thị họ tên, số điện thoại và loại tài khoản đã đăng nhập.
- Lưu phiên đăng nhập bằng DataStore; access token và refresh token được mã hóa bằng Android Keystore.
- Tự động đính kèm access token và làm mới token bằng OkHttp.

## Công nghệ

- Kotlin
- Android XML + ViewBinding
- Material 3
- Single Activity + Fragment
- Navigation Component
- MVVM + UDF
- Hilt Dependency Injection
- Retrofit + OkHttp
- Kotlin Serialization
- DataStore Preferences
- Android Keystore
- Kotlin Coroutines và Flow

## Cấu trúc module

```text
app                 Entry point, root navigation và cấu hình ứng dụng
core:common         AppResult, AppError và tiện ích dùng chung
core:model          Domain model
core:network        Retrofit API, DTO, interceptor và authenticator
core:data           Repository và mapper giữa network/domain
core:datastore      SessionStore và mã hóa token
feature:auth        Phone, OTP và Candidate onboarding
feature:home        Home, bottom navigation và thông tin tài khoản
```

## Yêu cầu môi trường

- Android Studio phiên bản hỗ trợ AGP 8.12+
- JDK 17
- Android SDK 36
- Min SDK 24
- Backend Jobday chạy ở cổng `8081`

## Cấu hình backend local

Tạo hoặc cập nhật file `local.properties` ở thư mục gốc. File này không được commit lên Git.

### Android Emulator

```properties
sdk.dir=D\:\\Android\\Sdk
API_BASE_URL=http://10.0.2.2:8081/api/v1/
```

### Máy Android thật

Điện thoại và máy tính phải dùng cùng mạng Wi-Fi. Thay địa chỉ bên dưới bằng IPv4 của máy tính:

```properties
sdk.dir=D\:\\Android\\Sdk
API_BASE_URL=http://192.168.1.xxx:8081/api/v1/
```

Có thể xem IPv4 trên Windows bằng:

```powershell
ipconfig
```

Backend phải lắng nghe trên `0.0.0.0:8081` và Windows Firewall phải cho phép kết nối đến cổng này.

## Chạy dự án

1. Khởi động PostgreSQL, Redis và MinIO của backend.
2. Chạy Jobday Backend tại cổng `8081`.
3. Mở thư mục dự án bằng Android Studio.
4. Sync Gradle.
5. Chọn emulator hoặc thiết bị thật rồi chạy cấu hình `app`.

Ở môi trường local, backend hiện sử dụng OTP cố định:

```text
123456
```

Không sử dụng OTP cố định ở staging hoặc production.

## Build và kiểm tra

Trên Windows:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat testDebugUnitTest
```

APK debug được tạo tại:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Luồng Candidate hiện tại

```text
Welcome
  → Khám phá ngay
  → Home Guest
  → Tài khoản
  → Đăng nhập
  → Nhập số điện thoại
  → Xác minh OTP
  → Nhập họ tên nếu chưa hoàn thiện
  → Home đã đăng nhập
```

Các action tìm kiếm, danh mục nhanh, card việc làm và ba tab Hồ sơ, Việc làm, Thông báo đang được để dành cho các bước phát triển tiếp theo.
