package com.covenantcode.crm.service;

import com.covenantcode.crm.dto.group.GroupStatusUpdateRequest;
import com.covenantcode.crm.dto.group.StudyGroupCreateRequest;
import com.covenantcode.crm.dto.group.StudyGroupResponse;
import com.covenantcode.crm.dto.group.StudyGroupUpdateRequest;
import com.covenantcode.crm.entity.enums.GroupStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudyGroupService {

    StudyGroupResponse create(StudyGroupCreateRequest request);

    Page<StudyGroupResponse> getAll(Long courseId, Long teacherId, GroupStatus status, Pageable pageable);

    StudyGroupResponse updateStatus(Long id, GroupStatusUpdateRequest request);

    StudyGroupResponse update(Long id, StudyGroupUpdateRequest request);
}
