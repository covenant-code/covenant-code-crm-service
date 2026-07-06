package com.covenantcode.crm.controller;

import com.covenantcode.crm.BaseIntegrationTest;
import com.covenantcode.crm.dto.group.StudyGroupCreateRequest;
import com.covenantcode.crm.entity.Course;
import com.covenantcode.crm.entity.User;
import com.covenantcode.crm.entity.Student;
import com.covenantcode.crm.entity.Role;
import com.covenantcode.crm.entity.enums.GroupStatus;
import com.covenantcode.crm.entity.enums.RoleName;
import com.covenantcode.crm.repository.CourseRepository;
import com.covenantcode.crm.repository.RoleRepository;
import com.covenantcode.crm.repository.StudentRepository;
import com.covenantcode.crm.repository.StudyGroupRepository;
import com.covenantcode.crm.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class StudyGroupControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    private Course testCourse;
    private User teacher;
    private Student student1;
    private Student student2;
    private User manager;

    @BeforeEach
    void setUp() {

        studyGroupRepository.deleteAllInBatch();
        studentRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        courseRepository.deleteAllInBatch();

        Role teacherRole = roleRepository.findByName(RoleName.TEACHER)
                .orElseThrow(() -> new RuntimeException("Teacher role not found"));
        Role managerRole = roleRepository.findByName(RoleName.MANAGER)
                .orElseThrow(() -> new RuntimeException("Manager role not found"));

        testCourse = courseRepository.save(Course.builder()
                .title("Java for Test")
                .description("Integration test course")
                .durationInWeeks(8)
                .price(java.math.BigDecimal.valueOf(1000))
                .status(com.covenantcode.crm.entity.enums.CourseStatus.ACTIVE)
                .build());

        teacher = userRepository.save(User.builder()
                .firstName("Teacher")
                .lastName("Test")
                .email("teacher@test.com")
                .password("encoded_password")
                .role(teacherRole)
                .enabled(true)
                .build());

        manager = userRepository.save(User.builder()
                .firstName("Manager")
                .lastName("Test")
                .email("manager@test.com")
                .password("encoded_password")
                .role(managerRole)
                .enabled(true)
                .build());

        student1 = studentRepository.save(Student.builder()
                .firstName("Student1")
                .lastName("Test")
                .email("student1@test.com")
                .phone("123456789")
                .build());

        student2 = studentRepository.save(Student.builder()
                .firstName("Student2")
                .lastName("Test")
                .email("student2@test.com")
                .phone("987654321")
                .build());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createGroup_shouldReturn201AndDraftStatus() throws Exception {
        StudyGroupCreateRequest request = StudyGroupCreateRequest.builder()
                .name("Integration Group")
                .courseId(testCourse.getId())
                .teacherId(teacher.getId())
                .startDate(LocalDate.now().plusDays(7))
                .studentIds(Set.of(student1.getId(), student2.getId()))
                .build();

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Integration Group"))
                .andExpect(jsonPath("$.status").value(GroupStatus.DRAFT.name()))
                .andExpect(jsonPath("$.course.id").value(testCourse.getId()))
                .andExpect(jsonPath("$.teacher.id").value(teacher.getId()))
                .andExpect(jsonPath("$.students").isArray())
                .andExpect(jsonPath("$.students.length()").value(2))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createGroup_courseNotFound_shouldReturn404() throws Exception {
        StudyGroupCreateRequest request = StudyGroupCreateRequest.builder()
                .name("Invalid Course Group")
                .courseId(999L)
                .teacherId(teacher.getId())
                .startDate(LocalDate.now().plusDays(7))
                .build();

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("resource-not-found"))
                .andExpect(jsonPath("$.detail").value(containsString("Course")))
                .andExpect(jsonPath("$.detail").value(containsString("999")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createGroup_teacherIsManager_shouldReturn400() throws Exception {
        StudyGroupCreateRequest request = StudyGroupCreateRequest.builder()
                .name("Group with Manager as Teacher")
                .courseId(testCourse.getId())
                .teacherId(manager.getId())
                .startDate(LocalDate.now().plusDays(7))
                .build();

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("bad-request"))
                .andExpect(jsonPath("$.detail").value(containsString("не является учителем")))
                .andExpect(jsonPath("$.detail").value(containsString(manager.getId().toString())));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void createGroup_withTeacherRole_shouldReturn403() throws Exception {
        StudyGroupCreateRequest request = StudyGroupCreateRequest.builder()
                .name("Group by Teacher")
                .courseId(testCourse.getId())
                .teacherId(teacher.getId())
                .startDate(LocalDate.now().plusDays(7))
                .build();

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());
    }
}