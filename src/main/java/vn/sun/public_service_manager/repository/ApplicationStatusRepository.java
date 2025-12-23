package vn.sun.public_service_manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.sun.public_service_manager.entity.ApplicationStatus;
import vn.sun.public_service_manager.utils.constant.StatusEnum;

@Repository
public interface ApplicationStatusRepository extends JpaRepository<ApplicationStatus, Long> {

    @Query("SELECT COUNT(DISTINCT ast.application.id) FROM ApplicationStatus ast " +
           "WHERE ast.status = :status " +
           "AND ast.id IN (SELECT MAX(ast2.id) FROM ApplicationStatus ast2 GROUP BY ast2.application.id)")
    long countByLatestStatus(@Param("status") StatusEnum status);

}
