# API Documentation - Hệ thống Quản lý Dịch vụ Công

## Tổng quan

Hệ thống quản lý dịch vụ công cung cấp các API RESTful cho công dân và quản trị viên.

### Thông tin cơ bản
- **Base URL**: `http://localhost:8080`
- **API Version**: `v1`
- **Authentication**: JWT Bearer Token
- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

## Xác thực (Authentication)

Hệ thống sử dụng JWT (JSON Web Token) để xác thực người dùng.

### Quy trình đăng nhập:

1. **Đăng ký tài khoản** (nếu chưa có):
   ```
   POST /api/v1/citizen/auth/register
   ```

2. **Đăng nhập**:
   ```
   POST /api/v1/citizen/auth/login
   ```
   Response sẽ trả về JWT token.

3. **Sử dụng token**: Thêm header vào mọi request cần xác thực:
   ```
   Authorization: Bearer <your-jwt-token>
   ```

4. **Đăng xuất**:
   ```
   GET /api/v1/citizen/auth/logout
   ```
   Client cần xóa token đã lưu.

## Nhóm API

### 1. Citizen Public APIs (Không cần xác thực)

#### 1.1. Đăng ký tài khoản
```http
POST /api/v1/citizen/auth/register
Content-Type: application/json

{
  "nationalId": "123456789012",
  "fullName": "Nguyễn Văn A",
  "dob": "1990-01-01",
  "gender": "Nam",
  "address": "123 Đường ABC, Quận 1, TP.HCM",
  "phone": "0901234567",
  "email": "nguyenvana@example.com",
  "password": "Password123!"
}
```

**Response 201 Created:**
```json
{
  "citizen": {
    "nationalId": "123456789012",
    "fullName": "Nguyễn Văn A",
    "email": "nguyenvana@example.com"
  }
}
```

#### 1.2. Đăng nhập
```http
POST /api/v1/citizen/auth/login
Content-Type: application/json

{
  "nationalId": "123456789012",
  "password": "Password123!"
}
```

**Response 200 OK:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### 1.3. Xem danh sách dịch vụ
```http
GET /api/v1/services?page=1&size=10&sortBy=id&sortDir=asc&keyword=&serviceTypeId=
```

**Query Parameters:**
- `page`: Số trang (bắt đầu từ 1)
- `size`: Số dịch vụ mỗi trang
- `sortBy`: Sắp xếp theo field (id, name, ...)
- `sortDir`: Chiều sắp xếp (asc/desc)
- `keyword`: Tìm kiếm theo tên hoặc mã
- `serviceTypeId`: Lọc theo loại dịch vụ

#### 1.4. Xem chi tiết dịch vụ
```http
GET /api/v1/services/{id}
```

### 2. Citizen Protected APIs (Cần JWT token)

#### 2.1. Xem thông tin cá nhân
```http
GET /api/v1/citizen/me
Authorization: Bearer <token>
```

#### 2.2. Cập nhật thông tin cá nhân
```http
PUT /api/v1/citizen/update
Authorization: Bearer <token>
Content-Type: application/json

{
  "fullName": "Nguyễn Văn A",
  "dob": "1990-01-01",
  "gender": "Nam",
  "address": "456 Đường XYZ, Quận 2, TP.HCM",
  "phone": "0901234567",
  "email": "nguyenvana.new@example.com"
}
```

#### 2.3. Đổi mật khẩu
```http
PUT /api/v1/citizen/change-password
Authorization: Bearer <token>
Content-Type: application/json

{
  "oldPassword": "OldPassword123!",
  "newPassword": "NewPassword123!",
  "confirmPassword": "NewPassword123!"
}
```

### 3. Application APIs (Cần JWT token)

#### 3.1. Lấy danh sách hồ sơ của tôi
```http
GET /api/v1/applications?page=1&size=10&sortBy=id&sortDir=asc&keyword=
Authorization: Bearer <token>
```

#### 3.2. Xem chi tiết hồ sơ
```http
GET /api/v1/applications/{id}
Authorization: Bearer <token>
```

#### 3.3. Nộp hồ sơ mới (Upload file)
```http
POST /api/v1/applications/upload
Authorization: Bearer <token>
Content-Type: multipart/form-data

serviceId: 1
note: "Hồ sơ cấp CMND mới"
files: [file1.pdf, file2.jpg, ...]
```

**File hỗ trợ:** pdf, doc, docx, jpg, png  
**Kích thước tối đa:** 10MB/file

**Response 200 OK:**
```json
{
  "applicationId": "APP-20231224-001",
  "uploadedAt": "2023-12-24T10:30:00"
}
```

#### 3.4. Bổ sung tài liệu cho hồ sơ
```http
POST /api/v1/applications/upload-more
Authorization: Bearer <token>
Content-Type: multipart/form-data

applicationId: 1
files: [file3.pdf, file4.jpg, ...]
```

#### 3.5. Xuất danh sách hồ sơ ra CSV (Admin/Manager)
```http
GET /api/v1/applications/export-applications
Authorization: Bearer <admin-token>
```

### 4. Notification APIs (Cần JWT token)

#### 4.1. Lấy danh sách thông báo
```http
GET /api/v1/notifications?page=0&size=20&sortBy=createdAt&sortDir=desc
Authorization: Bearer <token>
```

#### 4.2. Đếm thông báo chưa đọc
```http
GET /api/v1/notifications/unread-count
Authorization: Bearer <token>
```

**Response 200 OK:**
```json
5
```

#### 4.3. Đánh dấu thông báo đã đọc
```http
PUT /api/v1/notifications/{id}/mark-as-read
Authorization: Bearer <token>
```

#### 4.4. Đánh dấu tất cả đã đọc
```http
PUT /api/v1/notifications/mark-all-as-read
Authorization: Bearer <token>
```

### 5. Admin APIs (Cần quyền Admin/Manager/Staff)

#### 5.1. Xuất dịch vụ ra CSV
```http
GET /admin/services/export
Authorization: Bearer <admin-token>
```

#### 5.2. Import dịch vụ từ CSV
```http
POST /admin/services/import
Authorization: Bearer <admin-token>
Content-Type: multipart/form-data

file: services.csv
```

#### 5.3. Xuất loại dịch vụ ra CSV
```http
GET /admin/servicetypes/export
Authorization: Bearer <admin-token>
```

#### 5.4. Import loại dịch vụ từ CSV
```http
POST /admin/servicetypes/import
Authorization: Bearer <admin-token>
Content-Type: multipart/form-data

file: service-types.csv
```

## Mã lỗi HTTP

| Code | Ý nghĩa |
|------|---------|
| 200 | OK - Thành công |
| 201 | Created - Tạo mới thành công |
| 400 | Bad Request - Dữ liệu không hợp lệ |
| 401 | Unauthorized - Chưa đăng nhập hoặc token không hợp lệ |
| 403 | Forbidden - Không có quyền truy cập |
| 404 | Not Found - Không tìm thấy tài nguyên |
| 500 | Internal Server Error - Lỗi server |

## Response Format

### Success Response
```json
{
  "status": 200,
  "message": "Thành công",
  "data": { ... }
}
```

### Error Response
```json
{
  "status": 400,
  "message": "Lỗi validation",
  "error": "Field 'email' is required"
}
```

## Ví dụ sử dụng với cURL

### 1. Đăng ký
```bash
curl -X POST http://localhost:8080/api/v1/citizen/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "123456789012",
    "fullName": "Nguyễn Văn A",
    "dob": "1990-01-01",
    "gender": "Nam",
    "address": "123 Đường ABC",
    "phone": "0901234567",
    "email": "test@example.com",
    "password": "Password123!"
  }'
```

### 2. Đăng nhập
```bash
curl -X POST http://localhost:8080/api/v1/citizen/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "123456789012",
    "password": "Password123!"
  }'
```

### 3. Lấy danh sách dịch vụ
```bash
curl -X GET "http://localhost:8080/api/v1/services?page=1&size=10"
```

### 4. Xem thông tin cá nhân (cần token)
```bash
curl -X GET http://localhost:8080/api/v1/citizen/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 5. Upload hồ sơ (cần token)
```bash
curl -X POST http://localhost:8080/api/v1/applications/upload \
  -H "Authorization: Bearer <token>" \
  -F "serviceId=1" \
  -F "note=Hồ sơ cấp CMND mới" \
  -F "files=@document1.pdf" \
  -F "files=@document2.jpg"
```

## Ví dụ sử dụng với JavaScript/Fetch

### 1. Đăng nhập và lưu token
```javascript
async function login(nationalId, password) {
  const response = await fetch('http://localhost:8080/api/v1/citizen/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ nationalId, password })
  });
  
  const data = await response.json();
  
  if (response.ok) {
    // Lưu token vào localStorage
    localStorage.setItem('token', data.token);
    return data.token;
  } else {
    throw new Error(data.message);
  }
}
```

### 2. Gọi API với token
```javascript
async function getMyProfile() {
  const token = localStorage.getItem('token');
  
  const response = await fetch('http://localhost:8080/api/v1/citizen/me', {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  return await response.json();
}
```

### 3. Upload file
```javascript
async function uploadApplication(serviceId, note, files) {
  const token = localStorage.getItem('token');
  const formData = new FormData();
  
  formData.append('serviceId', serviceId);
  formData.append('note', note);
  
  // Thêm nhiều file
  for (let file of files) {
    formData.append('files', file);
  }
  
  const response = await fetch('http://localhost:8080/api/v1/applications/upload', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`
    },
    body: formData
  });
  
  return await response.json();
}
```

## Testing với Swagger UI

1. Truy cập: `http://localhost:8080/swagger-ui/index.html`
2. Chọn nhóm API cần test (ví dụ: "1. Citizen Public APIs")
3. Đối với API cần authentication:
   - Đăng nhập trước để lấy token
   - Click nút **Authorize** (ổ khóa) ở góc trên bên phải
   - Nhập token (có thể với hoặc không có prefix "Bearer ")
   - Click **Authorize**
4. Mở endpoint cần test, click **Try it out**
5. Điền parameters và request body
6. Click **Execute**
7. Xem kết quả ở phần Response

## Lưu ý quan trọng

1. **Token hết hạn**: JWT token có thời hạn sử dụng. Khi hết hạn, cần đăng nhập lại để lấy token mới.

2. **File upload**: 
   - Chỉ chấp nhận: pdf, doc, docx, jpg, png
   - Kích thước tối đa: 10MB/file
   - Có thể upload nhiều file cùng lúc

3. **Phân trang**:
   - Page bắt đầu từ 1 (user-friendly)
   - Một số API bắt đầu từ 0 (internal)

4. **CORS**: Nếu gọi từ domain khác, cần cấu hình CORS trong backend

5. **Rate limiting**: Có thể có giới hạn số request trong một khoảng thời gian

## Liên hệ & Hỗ trợ

- **Email**: support@publicservice.vn
- **Team**: Public Service Manager Team
- **License**: Apache 2.0

## Changelog

### Version 1.0.0 (2023-12-24)
- Initial release với đầy đủ Swagger documentation
- Hỗ trợ JWT authentication
- APIs cho Citizen, Application, Notification
- APIs cho Admin quản lý hệ thống
- Import/Export CSV

