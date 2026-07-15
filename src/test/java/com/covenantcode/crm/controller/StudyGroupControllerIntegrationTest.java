package com.covenantcode.crm.controller;

import com.covenantcode.crm.BaseIntegrationTest;
import com.covenantcode.crm.dto.group.GroupStatusUpdateRequest;
import com.covenantcode.crm.dto.group.StudyGroupCreateRequest;
import com.covenantcode.crm.entity.Course;
import com.covenantcode.crm.entity.Role;
import com.covenantcode.crm.entity.Student;
import com.covenantcode.crm.entity.StudyGroup;
import com.covenantcode.crm.entity.User;
import com.covenantcode.crm.entity.enums.GroupStatus;
import com.covenantcode.crm.entity.enums.RoleName;
import com.covenantcode.crm.repository.CourseRepository;
import com.covenantcode.crm.repository.RoleRepository;
import com.covenantcode.crm.repository.StudentRepository;
import com.covenantcode.crm.repository.StudyGroupRepository;
import com.covenantcode.crm.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(RoleName.TEACHER);
                    return roleRepository.save(newRole);
                });
        Role managerRole = roleRepository.findByName(RoleName.MANAGER)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(RoleName.MANAGER);
                    return roleRepository.save(newRole);
                });

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

        StudyGroup group1 = StudyGroup.builder()
                .name("Morning Java")
                .course(testCourse)
                .teacher(teacher)
                .startDate(LocalDate.now())
                .status(GroupStatus.ACTIVE)
                .students(new HashSet<>(Set.of(student1, student2)))
                .build();

        StudyGroup group2 = StudyGroup.builder()
                .name("Evening Java")
                .course(testCourse)
                .teacher(teacher)
                .startDate(LocalDate.now().plusDays(7))
                .status(GroupStatus.ACTIVE)
                .students(new HashSet<>(Set.of(student2)))
                .build();

        StudyGroup group3 = StudyGroup.builder()
                .name("Advanced Java")
                .course(testCourse)
                .teacher(teacher)
                .startDate(LocalDate.now().plusDays(14))
                .status(GroupStatus.COMPLETED)
                .students(new HashSet<>())
                .build();
        studyGroupRepository.saveAll(List.of(group1, group2, group3));

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

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Список без фильтров — должен вернуть все группы (3)")
    void getAllStudyGroups_noFilters_shouldReturnAllGroups() throws Exception {
        mockMvc.perform(get("/api/v1/groups")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Фильтр по courseId — должен вернуть только группы курса Java Core")
    void getAllStudyGroups_filterByCourseId_shouldReturnOnlyMatchingGroups() throws Exception {
        mockMvc.perform(get("/api/v1/groups")
                        .param("courseId", testCourse.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content[*].course.id").value(
                        org.hamcrest.Matchers.everyItem(
                                org.hamcrest.Matchers.is(
                                        Integer.valueOf(testCourse.getId().intValue())
                                )
                        )
                ));

    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Фильтр по status ACTIVE — должен вернуть только активные группы")
    void getAllStudyGroups_filterByStatusActive_shouldReturnOnlyActiveGroups() throws Exception {
        mockMvc.perform(get("/api/v1/groups")
                        .param("status", GroupStatus.ACTIVE.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[*].status").value(everyItem(is(GroupStatus.ACTIVE.name()))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Пагинация: page=0&size=2 — должен вернуть 2 группы")
    void getAllStudyGroups_pagination_shouldReturnPageWith2Groups() throws Exception {
        mockMvc.perform(get("/api/v1/groups")
                        .param("page", "0")
                        .param("size", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    @DisplayName("TEACHER не может видеть список групп — 403")
    void getAllStudyGroups_withTeacherRole_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/groups")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PATCH /{id}/status: DRAFT → ACTIVE → 200")
    void updateStatus_DraftToActive_ShouldReturn200() throws Exception {

        StudyGroup draftGroup = StudyGroup.builder()
                .name("Draft Group")
                .course(testCourse)
                .teacher(teacher)
                .startDate(LocalDate.now())
                .status(GroupStatus.DRAFT)
                .students(new HashSet<>())
                .build();
        draftGroup = studyGroupRepository.save(draftGroup);

        GroupStatusUpdateRequest request = new GroupStatusUpdateRequest(GroupStatus.ACTIVE);
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/api/v1/groups/{id}/status", draftGroup.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(GroupStatus.ACTIVE.name()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PATCH /{id}/status: DRAFT → COMPLETED → 400")
    void updateStatus_DraftToCompleted_ShouldReturn400() throws Exception {
        StudyGroup draftGroup = StudyGroup.builder()
                .name("Draft Group 2")
                .course(testCourse)
                .teacher(teacher)
                .startDate(LocalDate.now())
                .status(GroupStatus.DRAFT)
                .students(new HashSet<>())
                .build();
        draftGroup = studyGroupRepository.save(draftGroup);

        GroupStatusUpdateRequest request = new GroupStatusUpdateRequest(GroupStatus.COMPLETED);
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/api/v1/groups/{id}/status", draftGroup.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("bad-request"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PATCH /{id}/status: COMPLETED → ACTIVE → 400")
    void updateStatus_CompletedToActive_ShouldReturn400() throws Exception {

        StudyGroup completedGroup = StudyGroup.builder()
                .name("Completed Group")
                .course(testCourse)
                .teacher(teacher)
                .startDate(LocalDate.now().minusDays(30))
                .status(GroupStatus.COMPLETED)
                .students(new HashSet<>())
                .build();
        completedGroup = studyGroupRepository.save(completedGroup);

        GroupStatusUpdateRequest request = new GroupStatusUpdateRequest(GroupStatus.ACTIVE);
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/api/v1/groups/{id}/status", completedGroup.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("bad-request"));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    @DisplayName("PATCH /{id}/status: TEACHER → 403")
    void updateStatus_WithTeacherRole_ShouldReturn403() throws Exception {

        StudyGroup group = StudyGroup.builder()
                .name("Test Group")
                .course(testCourse)
                .teacher(teacher)
                .startDate(LocalDate.now())
                .status(GroupStatus.DRAFT)
                .students(new HashSet<>())
                .build();
        group = studyGroupRepository.save(group);

        GroupStatusUpdateRequest request = new GroupStatusUpdateRequest(GroupStatus.ACTIVE);
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/api/v1/groups/{id}/status", group.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PATCH /{id}/status: группа не найдена → 404")
    void updateStatus_GroupNotFound_ShouldReturn404() throws Exception {
        // given
        GroupStatusUpdateRequest request = new GroupStatusUpdateRequest(GroupStatus.ACTIVE);
        String requestJson = objectMapper.writeValueAsString(request);
        Long nonExistentId = 999L;

        // when & then
        mockMvc.perform(patch("/api/v1/groups/{id}/status", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("resource-not-found"));
    }
}