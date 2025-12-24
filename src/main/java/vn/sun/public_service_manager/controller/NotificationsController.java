package vn.sun.public_service_manager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import vn.sun.public_service_manager.dto.NotificationDTO;
import vn.sun.public_service_manager.service.NotificationService;
import vn.sun.public_service_manager.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "APIs quản lý thông báo (yêu cầu JWT token)")
@SecurityRequirement(name = "Bearer Authentication")
public class NotificationsController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    @ApiMessage("Lấy danh sách thông báo thành công")
    @Operation(
            summary = "Lấy danh sách thông báo",
            description = "Lấy danh sách thông báo của công dân đang đăng nhập với phân trang và sắp xếp")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    public ResponseEntity<Page<NotificationDTO>> getNotifications(
            Authentication authentication,
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số thông báo mỗi trang", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sắp xếp theo field", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Chiều sắp xếp: asc hoặc desc", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        String nationalId = authentication.getName();
        
        Sort sort = sortDir.equalsIgnoreCase("asc") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<NotificationDTO> notifications = notificationService
            .getNotificationsByNationalId(nationalId, pageable);
        
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread-count")
    @ApiMessage("Lấy số lượng thông báo chưa đọc thành công")
    @Operation(
            summary = "Đếm số thông báo chưa đọc",
            description = "Lấy số lượng thông báo chưa đọc của công dân")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = Long.class))),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        String nationalId = authentication.getName();
        Long unreadCount = notificationService.getUnreadCount(nationalId);
        
        return ResponseEntity.ok(unreadCount);
    }

    @PutMapping("/{id}/mark-as-read")
    @ApiMessage("Đánh dấu thông báo đã đọc thành công")
    @Operation(
            summary = "Đánh dấu thông báo đã đọc",
            description = "Đánh dấu một thông báo cụ thể là đã đọc")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đánh dấu thành công",
                    content = @Content(schema = @Schema(implementation = NotificationDTO.class))),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy thông báo")
    })
    public ResponseEntity<NotificationDTO> markAsRead(
            @Parameter(description = "ID của thông báo", example = "1")
            @PathVariable Long id,
            Authentication authentication
    ) {
        String nationalId = authentication.getName();
        NotificationDTO notification = notificationService.markAsRead(id, nationalId);
        
        return ResponseEntity.ok(notification);
    }

    @PutMapping("/mark-all-as-read")
    @ApiMessage("Đánh dấu tất cả thông báo đã đọc thành công")
    @Operation(
            summary = "Đánh dấu tất cả đã đọc",
            description = "Đánh dấu tất cả thông báo của công dân là đã đọc")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đánh dấu tất cả thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        String nationalId = authentication.getName();
        notificationService.markAllAsRead(nationalId);
        
        return ResponseEntity.ok().build();
    }
}
