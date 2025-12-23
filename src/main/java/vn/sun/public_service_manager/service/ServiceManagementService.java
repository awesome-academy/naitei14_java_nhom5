package vn.sun.public_service_manager.service;

import vn.sun.public_service_manager.entity.Service;
import vn.sun.public_service_manager.entity.ServiceType;
import vn.sun.public_service_manager.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Optional;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceManagementService {

    private final ServiceRepository serviceRepository;
    private final ServiceTypeService serviceTypeService;
    private final vn.sun.public_service_manager.repository.ApplicationRepository applicationRepository;

    @Transactional
    public Service createOrUpdateService(Service service) {
        return serviceRepository.save(service);
    }

    public Optional<Service> getServiceById(Long id) {
        return serviceRepository.findById(id);
    }

    @Transactional
    public void deleteService(Long id) {
        // Soft delete: set active = false for service and all related applications
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + id));
        
        // Set service inactive
        service.setActive(false);
        serviceRepository.save(service);
        
        // Set all applications of this service inactive
        List<vn.sun.public_service_manager.entity.Application> applications = 
            applicationRepository.findByServiceId(id);
        
        for (vn.sun.public_service_manager.entity.Application app : applications) {
            app.setActive(false);
        }
        
        if (!applications.isEmpty()) {
            applicationRepository.saveAll(applications);
        }
    }
    
    @Transactional
    public void restoreService(Long id) {
        // Restore: set active = true for service and all related applications
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + id));
        
        // Set service active
        service.setActive(true);
        serviceRepository.save(service);
        
        // Set all applications of this service active
        List<vn.sun.public_service_manager.entity.Application> applications = 
            applicationRepository.findByServiceId(id);
        
        for (vn.sun.public_service_manager.entity.Application app : applications) {
            app.setActive(true);
        }
        
        if (!applications.isEmpty()) {
            applicationRepository.saveAll(applications);
        }
    }
    public Page<Service> getServices(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String keyword,
            Long serviceTypeId)
    {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        boolean hasKeyword = StringUtils.hasText(keyword);
        boolean hasTypeFilter = serviceTypeId != null && serviceTypeId > 0;

        if (hasKeyword && hasTypeFilter) {
            ServiceType type = serviceTypeService.getServiceTypeById(serviceTypeId)
                    .orElseThrow(() -> new RuntimeException("ServiceType not found"));

            return serviceRepository.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCaseAndServiceType(
                    keyword, keyword, type, pageable);

        } else if (hasKeyword) {
            return serviceRepository.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
                    keyword, keyword, pageable);

        } else if (hasTypeFilter) {
            ServiceType type = serviceTypeService.getServiceTypeById(serviceTypeId)
                    .orElseThrow(() -> new RuntimeException("ServiceType not found"));
            return serviceRepository.findByServiceType(type, pageable);

        } else {
            return serviceRepository.findAll(pageable);
        }
    }

    /**
     * Export all services to CSV
     */
    public void exportServicesToCsv(Writer writer) {
        List<Service> services = serviceRepository.findAll();

        try {
            // Write UTF-8 BOM for Excel recognition
            writer.write('\ufeff');

            // Header
            writer.write("Code,Name,ServiceType,Fee,ProcessingTime,Description\n");

            // Write data
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (Service service : services) {
                writer.write(String.format("%s,%s,%s,%s,%s,%s\n",
                        escapeCSV(service.getCode()),
                        escapeCSV(service.getName()),
                        escapeCSV(service.getServiceType() != null ? service.getServiceType().getCategory() : ""),
                        service.getFee() != null ? service.getFee().toString() : "0",
                        service.getProcessingTime() != null ? service.getProcessingTime().toString() : "0",
                        escapeCSV(service.getDescription())));
            }

            writer.flush();

        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi xuất CSV: " + e.getMessage(), e);
        }
    }

    /**
     * Import services from CSV file
     */
    @Transactional
    public Map<String, Object> importServicesFromCsv(MultipartFile file) throws IOException {
        List<String> errors = new ArrayList<>();
        List<String> success = new ArrayList<>();
        int rowNumber = 0;
        int totalRows = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            // Skip BOM if present
            reader.mark(1);
            if (reader.read() != 0xFEFF) {
                reader.reset();
            }

            // Read first line as header
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new RuntimeException("File CSV rỗng!");
            }

            // Validate header
            String expectedHeader = "code,name,servicetypeid,fee,processingtime,description";
            if (!headerLine.toLowerCase().replaceAll("\\s+", "").startsWith(expectedHeader)) {
                throw new RuntimeException("File CSV không đúng định dạng! Cần có: code,name,serviceTypeId,fee,processingTime,description");
            }

            // Read data lines
            String line;
            while ((line = reader.readLine()) != null) {
                rowNumber++;
                totalRows++;

                if (line.trim().isEmpty()) {
                    continue; // Skip empty lines
                }

                try {
                    // Parse CSV line (handle quoted values)
                    String[] values = parseCSVLine(line);

                    if (values.length < 3) {
                        errors.add("Dòng " + rowNumber + ": Thiếu dữ liệu bắt buộc (code, name, serviceTypeId)");
                        continue;
                    }

                    String code = values[0].trim();
                    String name = values[1].trim();
                    String serviceTypeIdStr = values[2].trim();
                    String feeStr = values.length > 3 ? values[3].trim() : "0";
                    String processingTimeStr = values.length > 4 ? values[4].trim() : "0";
                    String description = values.length > 5 ? values[5].trim() : "";

                    // Validate required fields
                    if (code.isEmpty()) {
                        errors.add("Dòng " + rowNumber + ": Code không được để trống");
                        continue;
                    }

                    if (name.isEmpty()) {
                        errors.add("Dòng " + rowNumber + ": Name không được để trống");
                        continue;
                    }

                    if (serviceTypeIdStr.isEmpty()) {
                        errors.add("Dòng " + rowNumber + ": ServiceTypeId không được để trống");
                        continue;
                    }

                    // Check duplicate code
                    if (serviceRepository.existsByCode(code)) {
                        errors.add("Dòng " + rowNumber + ": Code '" + code + "' đã tồn tại");
                        continue;
                    }

                    // Validate and get ServiceType
                    Long serviceTypeId;
                    try {
                        serviceTypeId = Long.parseLong(serviceTypeIdStr);
                    } catch (NumberFormatException e) {
                        errors.add("Dòng " + rowNumber + ": ServiceTypeId không hợp lệ");
                        continue;
                    }

                    ServiceType serviceType = serviceTypeService.getServiceTypeById(serviceTypeId)
                            .orElse(null);
                    if (serviceType == null) {
                        errors.add("Dòng " + rowNumber + ": ServiceType ID " + serviceTypeId + " không tồn tại");
                        continue;
                    }

                    // Parse fee
                    BigDecimal fee;
                    try {
                        fee = new BigDecimal(feeStr);
                        if (fee.compareTo(BigDecimal.ZERO) < 0) {
                            errors.add("Dòng " + rowNumber + ": Fee không được âm");
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        errors.add("Dòng " + rowNumber + ": Fee không hợp lệ");
                        continue;
                    }

                    // Parse processing time
                    Integer processingTime;
                    try {
                        processingTime = Integer.parseInt(processingTimeStr);
                        if (processingTime < 0) {
                            errors.add("Dòng " + rowNumber + ": ProcessingTime không được âm");
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        errors.add("Dòng " + rowNumber + ": ProcessingTime không hợp lệ");
                        continue;
                    }

                    // Create new Service
                    Service service = new Service();
                    service.setCode(code);
                    service.setName(name);
                    service.setServiceType(serviceType);
                    service.setFee(fee);
                    service.setProcessingTime(processingTime);
                    service.setDescription(description);

                    // Save service
                    serviceRepository.save(service);
                    success.add("Dòng " + rowNumber + ": Import service '" + name + "' thành công");

                } catch (Exception e) {
                    errors.add("Dòng " + rowNumber + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi đọc file CSV: " + e.getMessage(), e);
        }

        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("total", totalRows);
        response.put("success", success.size());
        response.put("failed", errors.size());
        response.put("errors", errors);
        response.put("successMessages", success);

        return response;
    }

    /**
     * Helper method to parse CSV line with quoted values
     */
    private String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                inQuotes = !inQuotes;
            } else if (ch == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(ch);
            }
        }
        result.add(current.toString());

        return result.toArray(new String[0]);
    }

    /**
     * Helper method to escape CSV values
     */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        // If value contains comma, newline, or quotes, wrap in quotes and escape quotes
        if (value.contains(",") || value.contains("\n") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}