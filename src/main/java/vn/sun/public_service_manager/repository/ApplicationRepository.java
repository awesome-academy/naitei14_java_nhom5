package vn.sun.public_service_manager.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.sun.public_service_manager.entity.Application;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Page<Application> findAll(Pageable pageable);

    Page<Application> findByCitizenId(Long citizenId, Pageable pageable);

}
