package com.covenantcode.crm.service.impl;

import com.covenantcode.crm.dto.lead.LeadCreateRequest;
import com.covenantcode.crm.dto.lead.LeadResponse;
import com.covenantcode.crm.entity.Course;
import com.covenantcode.crm.entity.Lead;
import com.covenantcode.crm.entity.User;
import com.covenantcode.crm.entity.enums.LeadStatus;
import com.covenantcode.crm.exception.ResourceNotFoundException;
import com.covenantcode.crm.mapper.LeadMapper;
import com.covenantcode.crm.repository.CourseRepository;
import com.covenantcode.crm.repository.LeadRepository;
import com.covenantcode.crm.repository.UserRepository;
import com.covenantcode.crm.service.LeadService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class LeadServiceImpl implements LeadService {
    private final LeadMapper leadMapper;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final LeadRepository leadRepository;

    @Override
    @Transactional
    public LeadResponse create(LeadCreateRequest request) {
        Lead lead = leadMapper.toEntity(request);

        lead.setStatus(LeadStatus.NEW);

        if (request.getInterestedCourseId() != null) {
            Course course = courseRepository.findById(request.getInterestedCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Course",
                            request.getInterestedCourseId()
                    ));
            lead.setInterestedCourse(course);
        }

        if (request.getAssignedManagerId() != null) {
            User user = userRepository.findById(request.getAssignedManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User",
                            request.getAssignedManagerId()));
            lead.setAssignedManager(user);
        }

        OffsetDateTime now = OffsetDateTime.now();
        lead.setCreatedAt(now);
        lead.setUpdatedAt(now);

        Lead savedLead = leadRepository.save(lead);

        return leadMapper.toResponse(savedLead);
    }
}
