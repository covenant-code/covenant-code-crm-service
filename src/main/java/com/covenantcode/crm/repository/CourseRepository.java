package com.covenantcode.crm.repository;

import com.covenantcode.crm.entity.Course;
import com.covenantcode.crm.entity.enums.CourseStatus;
import com.covenantcode.crm.entity.enums.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {

    long countByStatus(CourseStatus status);


    Page<Course> findAllByStatus(CourseStatus status, Pageable pageable);

    Optional<Course> findByTitleIgnoreCase(String title);
}
