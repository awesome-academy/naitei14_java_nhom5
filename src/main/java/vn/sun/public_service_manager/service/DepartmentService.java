package vn.sun.public_service_manager.service;

import java.io.IOException;
import java.io.Writer;

import org.springframework.web.multipart.MultipartFile;

public interface DepartmentService {
    
    void exportDepartmentsToCsv(Writer writer);
    
    void importDepartmentsFromCsv(MultipartFile file) throws IOException;
}
