package vn.sun.public_service_manager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi citizenPublicApi() {
        return GroupedOpenApi.builder()
                .group("1. Citizen Public APIs")
                .pathsToMatch("/api/v1/citizen/auth/**", "/api/v1/services/**")
                .build();
    }

    @Bean
    public GroupedOpenApi citizenProtectedApi() {
        return GroupedOpenApi.builder()
                .group("2. Citizen Protected APIs")
                .pathsToMatch("/api/v1/citizen/**", "/api/citizen/**")
                .pathsToExclude("/api/v1/citizen/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi applicationApi() {
        return GroupedOpenApi.builder()
                .group("3. Application APIs")
                .pathsToMatch("/api/v1/applications/**")
                .build();
    }

    @Bean
    public GroupedOpenApi notificationApi() {
        return GroupedOpenApi.builder()
                .group("4. Notification APIs")
                .pathsToMatch("/api/v1/notifications/**")
                .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("5. Admin APIs")
                .pathsToMatch("/admin/**")
                .build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("Development Server");

        Contact contact = new Contact();
        contact.setName("Public Service Manager Team");
        contact.setEmail("support@publicservice.vn");

        License license = new License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0.html");

        Info info = new Info()
                .title("Hệ thống Quản lý Dịch vụ Công - API Documentation")
                .version("1.0.0")
                .description("## Mô tả tổng quan\n\n" +
                        "API documentation đầy đủ cho Hệ thống quản lý dịch vụ công.\n\n" +
                        "### Các nhóm API chính:\n\n" +
                        "#### 1. **Citizen Public APIs** (Không cần xác thực)\n" +
                        "- Đăng ký tài khoản công dân\n" +
                        "- Đăng nhập hệ thống\n" +
                        "- Xem danh sách dịch vụ công\n" +
                        "- Xem chi tiết dịch vụ\n\n" +
                        "#### 2. **Citizen Protected APIs** (Cần JWT token)\n" +
                        "- Xem thông tin cá nhân\n" +
                        "- Cập nhật thông tin cá nhân\n" +
                        "- Đổi mật khẩu\n\n" +
                        "#### 3. **Application APIs** (Cần JWT token)\n" +
                        "- Nộp hồ sơ mới\n" +
                        "- Xem danh sách hồ sơ của tôi\n" +
                        "- Xem chi tiết hồ sơ\n" +
                        "- Bổ sung tài liệu\n" +
                        "- Xuất danh sách hồ sơ (CSV)\n\n" +
                        "#### 4. **Notification APIs** (Cần JWT token)\n" +
                        "- Xem danh sách thông báo\n" +
                        "- Đếm thông báo chưa đọc\n" +
                        "- Đánh dấu đã đọc\n\n" +
                        "#### 5. **Admin APIs** (Cần quyền Admin/Manager/Staff)\n" +
                        "- Quản lý người dùng\n" +
                        "- Quản lý phòng ban\n" +
                        "- Quản lý dịch vụ\n" +
                        "- Quản lý loại dịch vụ\n" +
                        "- Quản lý hồ sơ\n" +
                        "- Import/Export CSV\n\n" +
                        "### Hướng dẫn sử dụng JWT Authentication:\n\n" +
                        "1. **Đăng ký tài khoản** (nếu chưa có): `POST /api/v1/citizen/auth/register`\n" +
                        "2. **Đăng nhập**: `POST /api/v1/citizen/auth/login`\n" +
                        "3. **Copy token** từ response (field `token`)\n" +
                        "4. **Click nút Authorize** ở trên cùng\n" +
                        "5. **Nhập**: `Bearer <your-token>` (hoặc chỉ token, hệ thống tự thêm 'Bearer')\n" +
                        "6. **Click Authorize** để lưu\n" +
                        "7. Giờ bạn có thể gọi các API cần xác thực\n\n" +
                        "### Lưu ý:\n" +
                        "- Token có thời hạn, khi hết hạn cần đăng nhập lại\n" +
                        "- Các API có ổ khóa 🔒 yêu cầu authentication\n" +
                        "- File upload hỗ trợ: pdf, doc, docx, jpg, png\n" +
                        "- Kích thước tối đa: 10MB/file")
                .contact(contact)
                .license(license);

        // JWT Security Scheme
        io.swagger.v3.oas.models.security.SecurityScheme securityScheme = 
                new io.swagger.v3.oas.models.security.SecurityScheme()
                .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("Nhập JWT token sau khi đăng nhập. Hệ thống sẽ tự động thêm 'Bearer ' prefix nếu cần.");

        io.swagger.v3.oas.models.security.SecurityRequirement securityRequirement = 
                new io.swagger.v3.oas.models.security.SecurityRequirement()
                .addList("Bearer Authentication");

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication", securityScheme))
                .addSecurityItem(securityRequirement);
    }
}
