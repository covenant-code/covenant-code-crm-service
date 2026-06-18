package com.covenantcode.crm.repository;

import com.covenantcode.crm.entity.StudyGroup;
import com.covenantcode.crm.entity.enums.GroupStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {
    boolean existsByCourseIdAndStatus(Long courseId, GroupStatus status);
}
