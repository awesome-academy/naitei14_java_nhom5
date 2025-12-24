package vn.sun.public_service_manager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.sun.public_service_manager.service.ServiceTypeService;
import vn.sun.public_service_manager.utils.annotation.ApiMessage;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("admin/servicetypes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
@Tag(name = "Admin - Service Types", description = "APIs quản lý loại dịch vụ (Admin only)")
@SecurityRequirement(name = "Bearer Authentication")
public class ServiceTypeApiController {

    private final ServiceTypeService serviceTypeService;

    @GetMapping("/export")
    @ApiMessage("Xuất danh sách loại dịch vụ ra file CSV thành công")
    @Operation(
            summary = "Xuất loại dịch vụ ra CSV",
            description = "Xuất toàn bộ danh sách loại dịch vụ ra file CSV")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xuất file thành công",
                    content = @Content(mediaType = "text/csv")),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền Admin")
    })
    public void exportServiceTypesToCsv(HttpServletResponse response) {
        try {
            response.setContentType("text/csv; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"service_types_" + System.currentTimeMillis() + ".csv\"");

            Writer writer = new OutputStreamWriter(
                    response.getOutputStream(),
                    StandardCharsets.UTF_8);

            serviceTypeService.exportServiceTypesToCsv(writer);

            writer.flush();

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xuất file CSV", e);
        }
    }

    @PostMapping("/import")
    @ApiMessage("Nhập danh sách loại dịch vụ từ file CSV thành công")
    @Operation(
            summary = "Import loại dịch vụ từ CSV",
            description = "Import danh sách loại dịch vụ từ file CSV. File phải có định dạng chuẩn.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Import thành công"),
            @ApiResponse(responseCode = "400", description = "File không hợp lệ hoặc sai định dạng"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền Admin")
    })
    public ResponseEntity<Map<String, Object>> importServiceTypesFromCsv(
            @Parameter(description = "File CSV chứa danh sách loại dịch vụ", required = true)
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "File không được để trống!"));
        }

        if (!file.getOriginalFilename().endsWith(".csv")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Chỉ chấp nhận file CSV!"));
        }

        try {
            Map<String, Object> result = serviceTypeService.importServiceTypesFromCsv(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
