package vn.sun.public_service_manager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.sun.public_service_manager.dto.CitizenProfileResponse;
import vn.sun.public_service_manager.dto.CitizenProfileUpdateRequest;
import vn.sun.public_service_manager.service.CitizenService;
import vn.sun.public_service_manager.utils.annotation.ApiMessage;

import java.security.Principal;

@RestController
@RequestMapping("/api/citizen/profile")
@Tag(name = "Citizen Profile", description = "APIs quản lý profile công dân (yêu cầu JWT token)")
@SecurityRequirement(name = "Bearer Authentication")
public class CitizenProfileController {

    @Autowired
    private CitizenService citizenService;

    @GetMapping
    @ApiMessage("Lấy thông tin cá nhân thành công")
    @Operation(
            summary = "Lấy thông tin profile",
            description = "Lấy thông tin cá nhân của công dân đang đăng nhập")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = CitizenProfileResponse.class))),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    public ResponseEntity<CitizenProfileResponse> getProfile(Principal principal) {
        String nationalId = principal.getName();
        CitizenProfileResponse dto = citizenService.getProfile(nationalId);
        return ResponseEntity.ok(dto);
    }

    @PutMapping
    @ApiMessage("Cập nhật thông tin cá nhân thành công")
    @Operation(
            summary = "Cập nhật profile",
            description = "Cập nhật thông tin cá nhân của công dân")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(schema = @Schema(implementation = CitizenProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    public ResponseEntity<?> updateProfile(Principal principal, @RequestBody CitizenProfileUpdateRequest updateDTO) {
        try {
            String nationalId = principal.getName();
            CitizenProfileResponse updated = citizenService.updateProfile(nationalId, updateDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
