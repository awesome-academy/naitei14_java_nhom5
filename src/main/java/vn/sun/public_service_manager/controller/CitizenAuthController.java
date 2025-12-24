package vn.sun.public_service_manager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import vn.sun.public_service_manager.entity.Citizen;
import vn.sun.public_service_manager.repository.CitizenRepository;
import vn.sun.public_service_manager.service.CitizenService;
import vn.sun.public_service_manager.service.JwtService;
import vn.sun.public_service_manager.utils.annotation.ApiMessage;
import vn.sun.public_service_manager.utils.annotation.LogActivity;
import vn.sun.public_service_manager.dto.CitizenRegistrationDto;
import vn.sun.public_service_manager.dto.JwtAuthResponse;
import vn.sun.public_service_manager.dto.LoginDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/citizen/auth")
@Tag(name = "Citizen Auth", description = "APIs xác thực công dân (không cần đăng nhập)")
public class CitizenAuthController {

        @Autowired
        private CitizenRepository citizenRepository;

        @Autowired
        private CitizenService citizenService;

        @Autowired
        private PasswordEncoder passwordEncoder;
        @Autowired
        private AuthenticationManager authenticationManager; // Dùng để xác thực
        @Autowired
        private JwtService jwtService; // Dùng để tạo token

        // 1. API Đăng ký
        @PostMapping("/register")
        @Operation(
                summary = "Đăng ký tài khoản công dân mới",
                description = "Tạo tài khoản công dân mới với thông tin cá nhân. CMND/CCCD, email và số điện thoại phải là duy nhất.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "201", description = "Đăng ký thành công",
                        content = @Content(mediaType = "application/json",
                                examples = @ExampleObject(value = """
                                {
                                  "citizen": {
                                    "nationalId": "123456789012",
                                    "fullName": "Nguyễn Văn A",
                                    "email": "nguyenvana@example.com"
                                  }
                                }
                                """))),
                @ApiResponse(responseCode = "400", description = "CMND/CCCD hoặc email đã tồn tại",
                        content = @Content(mediaType = "application/json",
                                examples = @ExampleObject(value = """
                                {
                                  "success": false,
                                  "message": "CMND/CCCD đã tồn tại trong hệ thống",
                                  "field": "nationalId"
                                }
                                """)))
        })
        public ResponseEntity<Map<String, Object>> registerCitizen(
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                description = "Thông tin đăng ký công dân",
                                required = true,
                                content = @Content(schema = @Schema(implementation = CitizenRegistrationDto.class),
                                        examples = @ExampleObject(value = """
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
                                        """)))
                        @RequestBody @Valid CitizenRegistrationDto registrationDto) {
                if (citizenRepository.existsByNationalId(registrationDto.getNationalId())) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(Map.of(
                                                        "success", false,
                                                        "message", "CMND/CCCD đã tồn tại trong hệ thống",
                                                        "field", "nationalId"));
                }
                if (citizenRepository.existsByEmail(registrationDto.getEmail())) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(Map.of(
                                                        "success", false,
                                                        "message", "Email đã được sử dụng",
                                                        "field", "email"));
                }

                Citizen citizen = new Citizen();
                // Ánh xạ DTO sang Entity
                citizen.setNationalId(registrationDto.getNationalId());
                citizen.setFullName(registrationDto.getFullName());
                citizen.setDob(registrationDto.getDob());
                citizen.setGender(registrationDto.getGender());
                citizen.setAddress(registrationDto.getAddress());
                citizen.setPhone(registrationDto.getPhone());
                citizen.setEmail(registrationDto.getEmail());
                citizen.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
                // // MÃ HÓA

                Citizen savedCitizen = citizenService.save(citizen);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(Map.of(
                                                // "success", true,
                                                // "message", "Đăng ký thành công! Vui lòng đăng nhập.",
                                                "citizen", Map.of(
                                                                "nationalId", savedCitizen.getNationalId(),
                                                                "fullName", savedCitizen.getFullName(),
                                                                "email", savedCitizen.getEmail())));
        }

        @PostMapping("/login")
        @LogActivity(action = "Citizen Login", targetType = "CITIZEN AUTH", description = "Đăng nhập hệ thống công dân")
        @ApiMessage("Đăng nhập thành công")
        @Operation(
                summary = "Đăng nhập hệ thống",
                description = "Đăng nhập bằng CMND/CCCD và mật khẩu. Trả về JWT token để sử dụng cho các API khác.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Đăng nhập thành công, nhận được JWT token",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = JwtAuthResponse.class),
                                examples = @ExampleObject(value = """
                                {
                                  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                                }
                                """))),
                @ApiResponse(responseCode = "401", description = "Số định danh hoặc mật khẩu không chính xác"),
                @ApiResponse(responseCode = "403", description = "Tài khoản đã bị khóa")
        })
        public ResponseEntity<?> login(
                @io.swagger.v3.oas.annotations.parameters.RequestBody(
                        description = "Thông tin đăng nhập (CMND/CCCD và mật khẩu)",
                        required = true,
                        content = @Content(schema = @Schema(implementation = LoginDto.class),
                                examples = @ExampleObject(value = """
                                {
                                  "nationalId": "123456789012",
                                  "password": "Password123!"
                                }
                                """)))
                @RequestBody @Valid LoginDto loginDto) {
                try {
                        Authentication authentication = authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(loginDto.getNationalId(),
                                                        loginDto.getPassword()));
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        String token = jwtService.generateToken(loginDto.getNationalId());
                        return ResponseEntity.ok(new JwtAuthResponse(token));
                } catch (BadCredentialsException ex) {
                        throw new BadCredentialsException("Số định danh hoặc mật khẩu không chính xác.");
                } catch (DisabledException ex) {
                        throw new DisabledException("Tài khoản đã bị khóa. Vui lòng liên hệ cơ quan chức năng.");
                } catch (AuthenticationException ex) {
                        throw new BadCredentialsException("Lỗi xác thực hệ thống: " + ex.getMessage());
                }
        }

        // 3. API Đăng xuất
        @LogActivity(action = "Citizen Logout", targetType = "CITIZEN AUTH", description = "Đăng xuất hệ thống công dân")
        @GetMapping("/logout")
        @Operation(
                summary = "Đăng xuất khỏi hệ thống",
                description = "Hướng dẫn client xóa JWT token. Server-side không lưu trữ token.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Đăng xuất thành công",
                        content = @Content(mediaType = "text/plain",
                                examples = @ExampleObject(value = "Logout successful. Client must delete the JWT token.")))
        })
        public ResponseEntity<String> logout() {
                // Logout chỉ là hướng dẫn client xóa token.
                return ResponseEntity.ok("Logout successful. Client must delete the JWT token.");
        }
}