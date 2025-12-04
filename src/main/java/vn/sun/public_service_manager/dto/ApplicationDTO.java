package vn.sun.public_service_manager.dto;

import java.time.LocalDateTime;
import lombok.Data;
import vn.sun.public_service_manager.entity.Application;

@Data
public class ApplicationDTO {
    private Long id;
    private String applicationCode;
    private String note;
    private String serviceName;
    private String citizenIdNumber;
    private LocalDateTime submittedAt;
    private String assignedStaffName;

    public static ApplicationDTO fromEntity(Application application) {
        ApplicationDTO dto = new ApplicationDTO();
        dto.setId(application.getId());
        dto.setApplicationCode(application.getApplicationCode());
        dto.setNote(application.getNote());

        if (application.getService() != null) {
            dto.setServiceName(application.getService().getName());
        }

        if (application.getCitizen() != null) {
            dto.setCitizenIdNumber(application.getCitizen().getNationalId());
        }

        if (application.getAssignedStaff() != null) {
            dto.setAssignedStaffName(application.getAssignedStaff().getUsername());
        }

        dto.setSubmittedAt(application.getSubmittedAt());
        return dto;
    }
}