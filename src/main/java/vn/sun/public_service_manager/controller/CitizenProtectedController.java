package vn.sun.public_service_manager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import vn.sun.public_service_manager.dto.CitizenProfileResponse;
import vn.sun.public_service_manager.dto.CitizenProfileUpdateRequest;
import vn.sun.public_service_manager.dto.ChangePasswordRequest;
import vn.sun.public_service_manager.service.ApplicationService;
import vn.sun.public_service_manager.service.CitizenService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/citizen")
@RequiredArgsConstructor
@Tag(name = "Citizen Protected", description = "APIs quản lý thông tin cá nhân (yêu cầu JWT token)")
@SecurityRequirement(name = "Bearer Authentication")
public class CitizenProtectedController {

    private final CitizenService citizenService;
    private final ApplicationService applicationService;

    private String getNationalId(UserDetails userDetails) {
        return userDetails.getUsername();
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @GetMapping("/me")
    @Operation(
            summary = "Xem thông tin cá nhân",
            description = "Lấy thông tin chi tiết của công dân đang đăng nhập")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công",
                    content = @Content(schema = @Schema(implementation = CitizenProfileResponse.class))),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy thông tin công dân")
    })
    public ResponseEntity<CitizenProfileResponse> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        String nationalId = getNationalId(userDetails);

        CitizenProfileResponse profile = citizenService.getProfile(nationalId);
        return ResponseEntity.ok(profile);
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @PutMapping("/update")
    @Operation(
            summary = "Cập nhật thông tin cá nhân",
            description = "Cập nhật thông tin cá nhân của công dân. Email không được trùng với người dùng khác.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(schema = @Schema(implementation = CitizenProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc email đã tồn tại"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token không hợp lệ")
    })
    public ResponseEntity<CitizenProfileResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin cập nhật",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CitizenProfileUpdateRequest.class),
                            examples = @ExampleObject(value = """
                            {
                              "fullName": "Nguyễn Văn A",
                              "dob": "1990-01-01",
                              "gender": "Nam",
                              "address": "456 Đường XYZ, Quận 2, TP.HCM",
                              "phone": "0901234567",
                              "email": "nguyenvana.new@example.com"
                            }
                            """)))
            @RequestBody @Valid CitizenProfileUpdateRequest request) {

        String nationalId = getNationalId(userDetails);
        CitizenProfileResponse updatedProfile = citizenService.updateProfile(nationalId, request);
        return ResponseEntity.ok(updatedProfile);
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @PutMapping("/change-password")
    @Operation(
            summary = "Đổi mật khẩu",
            description = "Thay đổi mật khẩu tài khoản. Yêu cầu nhập mật khẩu cũ để xác nhận.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đổi mật khẩu thành công",
                    content = @Content(mediaType = "text/plain",
                            examples = @ExampleObject(value = "Mật khẩu đã được thay đổi thành công."))),
            @ApiResponse(responseCode = "400", description = "Mật khẩu cũ không chính xác"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token không hợp lệ")
    })
    public ResponseEntity<String> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Mật khẩu cũ và mật khẩu mới",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ChangePasswordRequest.class),
                            examples = @ExampleObject(value = """
                            {
                              "oldPassword": "OldPassword123!",
                              "newPassword": "NewPassword123!",
                              "confirmPassword": "NewPassword123!"
                            }
                            """)))
            @Valid @RequestBody ChangePasswordRequest request) {

        String nationalId = getNationalId(userDetails);
        citizenService.changePassword(nationalId, request);

        return ResponseEntity.ok("Mật khẩu đã được thay đổi thành công.");
    }

    // @PreAuthorize("hasRole('CITIZEN')")
    // @GetMapping("/applications")
    // @Operation(summary = "Lấy danh sách hồ sơ của tôi", description = "Lấy tất cả
    // hồ sơ đã nộp")
    // public ResponseEntity<ApiResponseDTO<Object>> listApplications(
    // @AuthenticationPrincipal UserDetails userDetails,
    // @Parameter(description = "Số trang (bắt đầu từ 1)", schema = @Schema(type =
    // "integer", defaultValue = "1", example = "1")) @PageableDefault(size = 10,
    // sort = "submittedAt", direction = Sort.Direction.DESC) Pageable pageable) {

    // String nationalId = getNationalId(userDetails);

    // Page<ApplicationDTO> applicationPage =
    // applicationService.getApplicationsByCitizen(nationalId, pageable);

    // // Chuẩn hóa dữ liệu trả về với metadata phân trang
    // Map<String, Object> data = new LinkedHashMap<>();
    // data.put("content", applicationPage.getContent());
    // data.put("currentPage", applicationPage.getNumber() + 1); // Page index + 1
    // data.put("totalPages", applicationPage.getTotalPages());
    // data.put("totalElements", applicationPage.getTotalElements());
    // data.put("size", applicationPage.getSize());
    // data.put("hasNext", applicationPage.hasNext());
    // data.put("hasPrevious", applicationPage.hasPrevious());

    // ApiResponseDTO<Object> res = new ApiResponseDTO<>();
    // res.setMessage("Lấy danh sách dịch vụ thành công");
    // res.setData(data);
    // res.setStatus(HttpStatus.OK.value());
    // res.setError(null);

    // return ResponseEntity.ok(res);
    // }
}