package com.covenantcode.crm.controller;

import com.covenantcode.crm.BaseIntegrationTest;
import com.covenantcode.crm.dto.lead.LeadCreateRequest;
import com.covenantcode.crm.entity.Course;
import com.covenantcode.crm.entity.Role;
import com.covenantcode.crm.entity.User;
import com.covenantcode.crm.entity.enums.CourseStatus;
import com.covenantcode.crm.entity.enums.RoleName;
import com.covenantcode.crm.repository.CourseRepository;
import com.covenantcode.crm.repository.RoleRepository;
import com.covenantcode.crm.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class LeadControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Course course;
    private User manager;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        courseRepository.deleteAll();

        course = Course.builder()
                .title("Java Разработчик")
                .description("Полный курс по Java")
                .durationInWeeks(16)
                .price(new BigDecimal("45000.00"))
                .status(CourseStatus.ACTIVE)
                .build();
        course = courseRepository.save(course);

        Role managerRole = roleRepository.findByName(RoleName.MANAGER)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(RoleName.MANAGER);
                    return roleRepository.save(role);
                });

        manager = User.builder()
                .firstName("Алексей")
                .lastName("Смирнов")
                .email("manager@test.ru")
                .password(passwordEncoder.encode("password123"))
                .role(managerRole)
                .enabled(true)
                .build();
        manager = userRepository.save(manager);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createLead_withAllFields_returns201() throws Exception {
        LeadCreateRequest request = LeadCreateRequest.builder()
                .firstName("Васин")
                .lastName("Вася")
                .phone("+79161234568")
                .email("vasin@example.com")
                .source("Реклама")
                .interestedCourseId(course.getId())
                .assignedManagerId(manager.getId())
                .comment("Тестовый комментарий")
                .build();

        mockMvc.perform(post("/api/v1/leads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.interestedCourse.id").value(course.getId()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createLead_withOnlyRequiredFields_returns201() throws Exception {
        LeadCreateRequest request = LeadCreateRequest.builder()
                .firstName("Васин")
                .phone("+79161234568")
                .build();

        mockMvc.perform(post("/api/v1/leads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.interestedCourse").isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createLead_withNonExistentCourse_returns404() throws Exception {
        LeadCreateRequest request = LeadCreateRequest.builder()
                .firstName("Васин")
                .phone("+79161234568")
                .interestedCourseId(9999L)
                .build();

        mockMvc.perform(post("/api/v1/leads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("resource-not-found"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createLead_withEmptyFirstName_returns400() throws Exception {
        LeadCreateRequest request = LeadCreateRequest.builder()
                .firstName("")
                .phone("+79161234568")
                .build();

        mockMvc.perform(post("/api/v1/leads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]").value("firstName: Имя лида обязательно"));
    }
}