package vn.sun.public_service_manager.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.sun.public_service_manager.entity.Service;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    
    Page<Service> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    Page<Service> findByCodeContainingIgnoreCase(String code, Pageable pageable);
    
    Page<Service> findByServiceTypeId(Long serviceTypeId, Pageable pageable);
    
    Page<Service> findByResponsibleDepartmentId(Long departmentId, Pageable pageable);
}
