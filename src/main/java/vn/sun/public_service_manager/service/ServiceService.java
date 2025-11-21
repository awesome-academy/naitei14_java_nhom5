package vn.sun.public_service_manager.service;

import vn.sun.public_service_manager.dto.ServiceDTO;
import vn.sun.public_service_manager.dto.ServicePageResponse;

public interface ServiceService {
    
    ServicePageResponse getAllServices(int page, int size, String sortBy, String sortDir);
    
    ServicePageResponse searchByName(String name, int page, int size);
    
    ServiceDTO getServiceById(Long id);
}
