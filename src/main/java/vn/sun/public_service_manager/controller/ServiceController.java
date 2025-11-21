package vn.sun.public_service_manager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.sun.public_service_manager.dto.ServiceDTO;
import vn.sun.public_service_manager.dto.ServicePageResponse;
import vn.sun.public_service_manager.service.ServiceService;
import vn.sun.public_service_manager.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/services")
public class ServiceController {
    
    @Autowired
    private ServiceService serviceService;
    
    @GetMapping
    @ApiMessage("Lấy danh sách dịch vụ thành công")
    public ResponseEntity<ServicePageResponse> getAllServices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        ServicePageResponse response = serviceService.getAllServices(page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/search")
    @ApiMessage("Tìm kiếm dịch vụ thành công")
    public ResponseEntity<ServicePageResponse> searchServices(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        ServicePageResponse response = serviceService.searchByName(name, page, size);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @ApiMessage("Lấy chi tiết dịch vụ thành công")
    public ResponseEntity<?> getServiceById(@PathVariable Long id) {
        try {
            ServiceDTO serviceDTO = serviceService.getServiceById(id);
            return ResponseEntity.ok(serviceDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
        }
    }
}
