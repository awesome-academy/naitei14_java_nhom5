package vn.sun.public_service_manager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.sun.public_service_manager.entity.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDTO {
    
    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer processingTime;
    private BigDecimal fee;
    private Long responsibleDepartmentId;
    private String responsibleDepartmentName;
    private Long serviceTypeId;
    private String serviceTypeCategory;
    private LocalDateTime createdAt;
    
    public static ServiceDTO fromEntity(Service service) {
        ServiceDTO dto = new ServiceDTO();
        dto.setId(service.getId());
        dto.setCode(service.getCode());
        dto.setName(service.getName());
        dto.setDescription(service.getDescription());
        dto.setProcessingTime(service.getProcessingTime());
        dto.setFee(service.getFee());
        dto.setCreatedAt(service.getCreatedAt());
        
        if (service.getResponsibleDepartment() != null) {
            dto.setResponsibleDepartmentId(service.getResponsibleDepartment().getId());
            dto.setResponsibleDepartmentName(service.getResponsibleDepartment().getName());
        }
        
        if (service.getServiceType() != null) {
            dto.setServiceTypeId(service.getServiceType().getId());
            dto.setServiceTypeCategory(service.getServiceType().getCategory());
        }
        
        return dto;
    }
}
