package vn.sun.public_service_manager.service;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

public interface DepartmentService {
    
    void exportDepartmentsToCsv(Writer writer);
    
    Map<String, Object> importDepartmentsFromCsv(MultipartFile file) throws IOException;
}
