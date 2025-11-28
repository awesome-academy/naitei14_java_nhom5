package vn.sun.public_service_manager.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationResDTO {
    private Long id;
    private String applicationCode;
    @Column(columnDefinition = "TEXT")
    private String note;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submittedAt;

    private ApplicationService service;
    private ApplicationCitizen citizen;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ApplicationService {
        private Long id;
        private String name;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ApplicationCitizen {
        private Long id;
        private String fullName;
        private String nationalId;
    }
}
