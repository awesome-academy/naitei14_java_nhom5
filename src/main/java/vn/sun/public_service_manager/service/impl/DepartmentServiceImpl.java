package vn.sun.public_service_manager.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import vn.sun.public_service_manager.entity.Department;
import vn.sun.public_service_manager.entity.User;
import vn.sun.public_service_manager.repository.DepartmentRepository;
import vn.sun.public_service_manager.repository.UserRespository;
import vn.sun.public_service_manager.service.DepartmentService;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRespository userRepository;

    @Override
    public void exportDepartmentsToCsv(Writer writer) {
        List<Department> departments = departmentRepository.findAll();

        try {
            // Write UTF-8 BOM
            writer.write('\ufeff');

            // Header
            writer.write("ID,Code,Name,Address,Leader Username\n");

            // Write data
            for (Department dept : departments) {
                writer.write(String.format("%d,%s,%s,%s,%s\n",
                        dept.getId(),
                        escapeCSV(dept.getCode()),
                        escapeCSV(dept.getName()),
                        escapeCSV(dept.getAddress()),
                        dept.getLeader() != null ? escapeCSV(dept.getLeader().getUsername()) : ""));
            }

            writer.flush();

        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi xuất CSV: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void importDepartmentsFromCsv(MultipartFile file) throws IOException {
        List<String> errors = new ArrayList<>();
        int rowNumber = 0;

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
            String expectedHeader = "code,name,address";
            if (!headerLine.toLowerCase().startsWith(expectedHeader.toLowerCase())) {
                throw new RuntimeException("File CSV không đúng định dạng! Cần có: " + expectedHeader);
            }

            // Read data lines
            String line;
            while ((line = reader.readLine()) != null) {
                rowNumber++;

                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    String[] values = parseCSVLine(line);

                    if (values.length < 3) {
                        errors.add("Dòng " + rowNumber + ": Thiếu dữ liệu (code, name, address)");
                        continue;
                    }

                    String code = values[0].trim();
                    String name = values[1].trim();
                    String address = values[2].trim();

                    // Validate required fields
                    if (code.isEmpty() || name.isEmpty()) {
                        errors.add("Dòng " + rowNumber + ": Code và Name không được để trống");
                        continue;
                    }

                    // Check duplicate code
                    if (departmentRepository.existsByCode(code)) {
                        errors.add("Dòng " + rowNumber + ": Mã phòng ban '" + code + "' đã tồn tại");
                        continue;
                    }

                    // Create new Department
                    Department department = new Department();
                    department.setCode(code);
                    department.setName(name);
                    department.setAddress(address);

                    // Leader can be set later through UI
                    departmentRepository.save(department);

                } catch (Exception e) {
                    errors.add("Dòng " + rowNumber + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi đọc file CSV: " + e.getMessage(), e);
        }

        if (!errors.isEmpty()) {
            throw new RuntimeException("Import có lỗi: " + String.join("; ", errors));
        }
    }

    private String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());

        return result.toArray(new String[0]);
    }

    private String escapeCSV(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }

        return value;
    }
}
