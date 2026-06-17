package com.covenantcode.crm.service.impl;

import com.covenantcode.crm.dto.lead.CourseShortResponse;
import com.covenantcode.crm.dto.lead.LeadCreateRequest;
import com.covenantcode.crm.dto.lead.LeadResponse;
import com.covenantcode.crm.dto.lead.UserShortResponse;
import com.covenantcode.crm.entity.Course;
import com.covenantcode.crm.entity.Lead;
import com.covenantcode.crm.entity.User;
import com.covenantcode.crm.entity.enums.LeadStatus;
import com.covenantcode.crm.exception.ResourceNotFoundException;
import com.covenantcode.crm.mapper.LeadMapper;
import com.covenantcode.crm.repository.CourseRepository;
import com.covenantcode.crm.repository.LeadRepository;
import com.covenantcode.crm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class LeadServiceImplTest {

    @Mock
    private LeadMapper leadMapper;

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private LeadServiceImpl leadService;

    private LeadCreateRequest request;
    private Lead lead;
    private Lead savedLead;
    private LeadResponse response;
    private User manager;
    private Course course;
    private OffsetDateTime now;

    @BeforeEach
    void setUp() {
        now = OffsetDateTime.now();
        request = LeadCreateRequest.builder()
                .firstName("Васин")
                .lastName("Вася")
                .phone("+79161234568")
                .email("vasin@example.com")
                .source("Реклама")
                .interestedCourseId(1L)
                .assignedManagerId(2L)
                .comment("Тестовый коммент")
                .build();

        course = Course.builder()
                .id(1L)
                .title("Java Developer")
                .build();

        manager = User.builder()
                .id(2L)
                .firstName("Анатолий")
                .lastName("Пупкин")
                .build();

        lead = Lead.builder()
                .firstName("Васин")
                .lastName("Вася")
                .phone("+79161234568")
                .email("vasin@example.com")
                .source("ВКонтакте")
                .status(LeadStatus.NEW)
                .interestedCourse(course)
                .assignedManager(manager)
                .comment("Тестовый комментарий")
                .createdAt(now)
                .updatedAt(now)
                .build();

        savedLead = Lead.builder()
                .id(1L)
                .firstName("Васин")
                .lastName("Вася")
                .phone("+79161234568")
                .email("vasin@example.com")
                .source("ВКонтакте")
                .status(LeadStatus.NEW)
                .interestedCourse(course)
                .assignedManager(manager)
                .comment("Тестовый комментарий")
                .createdAt(now)
                .updatedAt(now)
                .build();

        response = LeadResponse.builder()
                .id(1L)
                .firstName("Васин")
                .lastName("Вася")
                .phone("+79161234568")
                .email("vasin@example.com")
                .source("ВКонтакте")
                .status("NEW")
                .interestedCourse(CourseShortResponse.builder()
                        .id(1L)
                        .title("Java Разработчик")
                        .build())
                .assignedManager(UserShortResponse.builder()
                        .id(2L)
                        .firstName("Алексей")
                        .lastName("Смирнов")
                        .build())
                .comment("Тестовый комментарий")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Test
    void create_WithAllFields_ShouldReturnLeadResponse(){
        when(leadMapper.toEntity(request)).thenReturn(lead);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userRepository.findById(2L)).thenReturn(Optional.of(manager));
        when(leadRepository.save(any(Lead.class))).thenReturn(savedLead);
        when(leadMapper.toResponse(savedLead)).thenReturn(response);

        LeadResponse result = leadService.create(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo("NEW");
        assertThat(result.getInterestedCourse()).isNotNull();
        assertThat(result.getInterestedCourse().getId()).isEqualTo(1L);
    }

    @Test
    void create_WithOnlyRequiredFields_ShouldReturnLeadResponse() {
        request = LeadCreateRequest.builder()
                .firstName("Васин")
                .phone("+79161234568")
                .build();

        lead = Lead.builder()
                .firstName("Васин")
                .phone("+79161234568")
                .status(LeadStatus.NEW)
                .createdAt(now)
                .updatedAt(now)
                .build();

        savedLead = Lead.builder()
                .id(2L)
                .firstName("Васин")
                .phone("+79161234568")
                .status(LeadStatus.NEW)
                .createdAt(now)
                .updatedAt(now)
                .build();

        response = LeadResponse.builder()
                .id(2L)
                .firstName("Васин")
                .phone("+79161234568")
                .status("NEW")
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(leadMapper.toEntity(request)).thenReturn(lead);
        when(leadRepository.save(any(Lead.class))).thenReturn(savedLead);
        when(leadMapper.toResponse(savedLead)).thenReturn(response);

        LeadResponse result = leadService.create(request);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("NEW");
        assertThat(result.getInterestedCourse()).isNull();
        assertThat(result.getAssignedManager()).isNull();
    }

    @Test
    void create_WhenCourseNotFound_ShouldThrowResourceNotFoundException() {
        request = LeadCreateRequest.builder()
                .firstName("Васин")
                .phone("+79161234568")
                .interestedCourseId(99L)
                .build();

        when(leadMapper.toEntity(any())).thenReturn(lead);
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> leadService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Course not found with id: 99");

        verify(leadRepository, never()).save(any());
    }
}
