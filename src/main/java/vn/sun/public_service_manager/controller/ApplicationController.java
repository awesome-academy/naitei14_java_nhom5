package vn.sun.public_service_manager.controller;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import vn.sun.public_service_manager.dto.response.ApplicationPageResponse;
import vn.sun.public_service_manager.dto.response.ApplicationResApiDTO;
import vn.sun.public_service_manager.dto.response.FileResDTO;
import vn.sun.public_service_manager.dto.response.MailResDTO;
import vn.sun.public_service_manager.entity.Application;
import vn.sun.public_service_manager.entity.Citizen;
import vn.sun.public_service_manager.exception.FileException;
import vn.sun.public_service_manager.service.ApplicationService;
import vn.sun.public_service_manager.service.CitizenService;
import vn.sun.public_service_manager.service.EmailService;
import vn.sun.public_service_manager.utils.FileUtil;
import vn.sun.public_service_manager.utils.SecurityUtil;
import vn.sun.public_service_manager.utils.annotation.ApiMessage;
import vn.sun.public_service_manager.utils.annotation.LogActivity;

@RestController
@RequestMapping("/api/v1/applications")
@Tag(name = "Applications", description = "APIs quản lý hồ sơ dịch vụ công (yêu cầu JWT token)")
@SecurityRequirement(name = "Bearer Authentication")
public class ApplicationController {

    private final FileUtil fileUtil;
    private final ApplicationService applicationService;
    private final EmailService emailService;
    private final CitizenService citizenService;

    public ApplicationController(FileUtil fileUtil, ApplicationService applicationService, EmailService emailService,
            CitizenService citizenService) {
        this.fileUtil = fileUtil;
        this.applicationService = applicationService;
        this.emailService = emailService;
        this.citizenService = citizenService;
    }

    @GetMapping
    @ApiMessage("Get all applications successfully")
    @Operation(
            summary = "Lấy danh sách hồ sơ của tôi",
            description = "Lấy tất cả hồ sơ đã nộp của công dân đang đăng nhập với filter, search, phân trang và sắp xếp")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = ApplicationPageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    public ResponseEntity<ApplicationPageResponse> getAllApplications(
            @Parameter(description = "Số trang (bắt đầu từ 1)", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Số dịch vụ mỗi trang", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sắp xếp theo field", example = "id") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Chiều sắp xếp: asc hoặc desc", example = "asc") @RequestParam(defaultValue = "asc") String sortDir,
            @Parameter(description = "Tìm kiếm theo tên dịch vụ hoặc mã", example = "") @RequestParam(required = false) String keyword) {
        String nationalId = SecurityUtil.getCurrentUserName();
        Citizen citizen = citizenService.getByNationalId(nationalId);
        if (page < 1)
            page = 1;
        ApplicationPageResponse response = applicationService.getAllApplicationsForCitizen(
                citizen.getId(), page, size, sortBy, sortDir, keyword);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @ApiMessage("Get application by ID successfully")
    @Operation(
            summary = "Xem chi tiết hồ sơ",
            description = "Lấy thông tin chi tiết của một hồ sơ cụ thể")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = ApplicationResApiDTO.class))),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập hồ sơ này"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy hồ sơ")
    })
    public ResponseEntity<ApplicationResApiDTO> getApplicationById(
            @Parameter(description = "ID của hồ sơ", example = "1")
            @PathVariable("id") Long id) {
        String nationalId = SecurityUtil.getCurrentUserName();
        Citizen citizen = citizenService.getByNationalId(nationalId);
        return ResponseEntity.ok(applicationService.getApplicationDetail(id, citizen.getId()));
    }

    @LogActivity(action = "Upload hồ sơ", targetType = "APPLICATION", description = "Upload hồ sơ mới với file đính kèm")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiMessage("Upload application with files successfully")
    @Operation(
            summary = "Nộp hồ sơ mới",
            description = "Upload hồ sơ dịch vụ công mới kèm theo file đính kèm.\n\n" +
                    "**File hỗ trợ:** pdf, doc, docx, jpg, png\n\n" +
                    "**Kích thước tối đa:** 10MB/file\n\n" +
                    "**Lưu ý:** Sau khi upload thành công, hệ thống sẽ gửi email xác nhận")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upload thành công",
                    content = @Content(schema = @Schema(implementation = FileResDTO.class))),
            @ApiResponse(responseCode = "400", description = "File không hợp lệ hoặc quá kích thước"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy dịch vụ")
    })
    public ResponseEntity<FileResDTO> createApplication(
            @Parameter(description = "ID dịch vụ cần nộp hồ sơ", required = true, example = "1")
            @RequestParam("serviceId") Long serviceId,
            @Parameter(description = "Ghi chú về hồ sơ", required = true, example = "Hồ sơ cấp CMND mới")
            @RequestParam("note") String note,
            @Parameter(
                    description = "Danh sách file đính kèm (pdf, doc, docx, jpg, png). Có thể upload nhiều file cùng lúc.",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart(value = "files", required = false) MultipartFile[] files)
            throws FileException {

        if (files == null || files.length == 0) {
            throw new FileException("No files uploaded.");
        }

        List<String> allowedExtensions = List.of("pdf", "doc", "docx", "jpg", "png");
        fileUtil.validateFileExtensions(files, allowedExtensions);

        // create user folder if not exists
        String username = SecurityUtil.getCurrentUserName();
        Citizen citizen = citizenService.getByNationalId(username);
        fileUtil.createDirectoryIfNotExists(username);

        // save files to user folder
        fileUtil.saveFiles(files, username);

        // save application data
        Application application = applicationService.createApplication(serviceId, note, files);

        MailResDTO mailResDTO = new MailResDTO();
        mailResDTO.setApplicationCode(application.getApplicationCode());
        mailResDTO.setServiceName(application.getService().getName());
        mailResDTO.setSubmittedAt(application.getSubmittedAt());
        // send mail
        emailService.sendApplicationConfirmationEmail(
                citizen.getEmail(),
                citizen.getFullName(), "Hồ sơ đã đăng ký...",
                mailResDTO,
                "email_template");

        FileResDTO response = new FileResDTO();
        response.setApplicationId(application.getApplicationCode());
        response.setUploadedAt(application.getSubmittedAt());
        return ResponseEntity.ok(response);
    }

    @LogActivity(action = "Bổ sung tài liệu", targetType = "APPLICATION", description = "Upload thêm tài liệu cho hồ sơ đã tồn tại")
    @PostMapping(value = "/upload-more", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiMessage("Upload more files to existing application successfully")
    @Operation(
            summary = "Bổ sung tài liệu cho hồ sơ",
            description = "Upload thêm file tài liệu cho hồ sơ đã tồn tại.\n\n" +
                    "**File hỗ trợ:** pdf, doc, docx, jpg, png\n\n" +
                    "**Kích thước tối đa:** 10MB/file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upload thành công",
                    content = @Content(schema = @Schema(implementation = FileResDTO.class))),
            @ApiResponse(responseCode = "400", description = "File không hợp lệ hoặc quá kích thước"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy hồ sơ")
    })
    public ResponseEntity<FileResDTO> uploadMoreFiles(
            @Parameter(description = "ID hồ sơ cần bổ sung tài liệu", required = true, example = "1")
            @RequestParam("applicationId") Long applicationId,

            @Parameter(
                    description = "Danh sách file đính kèm bổ sung (pdf, doc, docx, jpg, png)",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart("files") MultipartFile[] files)
            throws FileException {

        if (files == null || files.length == 0) {
            throw new FileException("No files uploaded.");
        }

        List<String> allowedExtensions = List.of("pdf", "doc", "docx", "jpg", "png");
        fileUtil.validateFileExtensions(files, allowedExtensions);

        String username = SecurityUtil.getCurrentUserName();
        fileUtil.createDirectoryIfNotExists(username);
        fileUtil.saveFiles(files, username);

        applicationService.uploadMoreDocuments(applicationId, files);

        FileResDTO response = new FileResDTO();
        response.setApplicationId("Application ID: " + applicationId);
        response.setUploadedAt(java.time.LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
    @GetMapping("/export-applications")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @ApiMessage("Xuất danh sách hồ sơ ra file CSV thành công")
    @Operation(
            summary = "Xuất danh sách hồ sơ ra CSV",
            description = "Xuất toàn bộ danh sách hồ sơ ra file CSV. **Chỉ Admin và Manager được phép**")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xuất file thành công",
                    content = @Content(mediaType = "text/csv")),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public void exportApplicationsToCsv(HttpServletResponse response) {
        try {
            response.setContentType("text/csv; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"application_list_" + System.currentTimeMillis() + ".csv\"");

            // Ghi BOM UTF-8
            response.getOutputStream().write(0xEF);
            response.getOutputStream().write(0xBB);
            response.getOutputStream().write(0xBF);

            Writer writer = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8);
            applicationService.exportApplicationsToCsv(writer);
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xuất file CSV Hồ sơ", e);
        }
    }

}
