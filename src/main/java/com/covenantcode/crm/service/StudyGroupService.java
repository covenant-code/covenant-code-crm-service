package com.covenantcode.crm.service;

import com.covenantcode.crm.dto.group.StudyGroupCreateRequest;
import com.covenantcode.crm.dto.group.StudyGroupResponse;

public interface StudyGroupService {

    StudyGroupResponse create(StudyGroupCreateRequest request);
}
