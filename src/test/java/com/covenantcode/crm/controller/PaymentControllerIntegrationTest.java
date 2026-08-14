package com.covenantcode.crm.controller;

import com.covenantcode.crm.BaseIntegrationTest;
import com.covenantcode.crm.dto.payment.PaymentCreateRequest;
import com.covenantcode.crm.entity.*;
import com.covenantcode.crm.entity.enums.CourseStatus;
import com.covenantcode.crm.entity.enums.GroupStatus;
import com.covenantcode.crm.entity.enums.PaymentStatus;
import com.covenantcode.crm.entity.enums.RoleName;
import com.covenantcode.crm.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@AutoConfigureMockMvc
@WithMockUser(roles = "ADMIN")
public class PaymentControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Student student;
    private StudyGroup studyGroup;

    @BeforeEach
    void setUp() {
        Course course = Course.builder()
                .title("Тестовый курс")
                .description("Для интеграционных тестов")
                .durationInWeeks(10)
                .price(new BigDecimal("5000.00"))
                .status(CourseStatus.ACTIVE)
                .build();
        course = courseRepository.save(course);

        Role teacherRole = roleRepository.findByName(RoleName.TEACHER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.TEACHER).build()));

        User teacher = User.builder()
                .firstName("Учитель")
                .lastName("Тестовый")
                .email("teacher.integration@test.com")
                .password("$2a$10$dummy")
                .role(teacherRole)
                .enabled(true)
                .build();
        teacher = userRepository.save(teacher);

        student = Student.builder()
                .firstName("Иван")
                .lastName("Петров")
                .email("ivan@test.com")
                .build();
        student = studentRepository.save(student);

        studyGroup = StudyGroup.builder()
                .name("Группа А")
                .course(course)
                .teacher(teacher)
                .startDate(LocalDate.now().plusDays(1))
                .status(GroupStatus.ACTIVE)
                .build();
        studyGroup = studyGroupRepository.save(studyGroup);
    }

    @Test
    void createPayment_shouldReturn201AndSetPendingStatus() throws Exception {
        PaymentCreateRequest request = PaymentCreateRequest.builder()
                .amount(new BigDecimal("5000.00"))
                .description("Оплата за курс")
                .dueDate(LocalDate.now().plusDays(30))
                .studyGroupId(studyGroup.getId())
                .build();

        mockMvc.perform(post("/api/v1/students/{studentId}/payments", student.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.studentId").value(student.getId()))
                .andExpect(jsonPath("$.studentName").value("Иван Петров"))
                .andExpect(jsonPath("$.amount").value(5000.00))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.paidAt").value(nullValue()))
                .andExpect(jsonPath("$.description").value("Оплата за курс"))
                .andExpect(jsonPath("$.dueDate").value(LocalDate.now().plusDays(30).toString()))
                .andExpect(jsonPath("$.studyGroupId").value(studyGroup.getId()));

        Payment saved = paymentRepository.findAllByStudentId(student.getId()).get(0);
        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(saved.getPaidAt()).isNull();
        assertThat(saved.getAmount()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(saved.getStudent().getId()).isEqualTo(student.getId());
        assertThat(saved.getStudyGroup().getId()).isEqualTo(studyGroup.getId());
    }

    @Test
    void updatePaymentStatus_toPaid_shouldSetPaidAt() throws Exception {
        Payment payment = Payment.builder()
                .student(student)
                .studyGroup(studyGroup)
                .amount(new BigDecimal("3000.00"))
                .status(PaymentStatus.PENDING)
                .description("Тестовый платёж")
                .dueDate(LocalDate.now().plusDays(10))
                .paidAt(null)
                .build();
        payment = paymentRepository.save(payment);

        String updateRequest = "{\"status\":\"PAID\"}";

        mockMvc.perform(patch("/api/v1/payments/{id}/status", payment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(payment.getId()))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.paidAt").exists());

        Payment updated = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(updated.getPaidAt()).isNotNull();
        assertThat(updated.getPaidAt()).isAfter(OffsetDateTime.now().minusSeconds(5));
    }

    @Test
    void createPayment_studentNotFound_shouldReturn404() throws Exception {
        Long nonExistentStudentId = 999L;
        PaymentCreateRequest request = PaymentCreateRequest.builder()
                .amount(new BigDecimal("1000.00"))
                .description("Оплата")
                .dueDate(LocalDate.now().plusDays(10))
                .build();

        mockMvc.perform(post("/api/v1/students/{studentId}/payments", nonExistentStudentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Student с id " + nonExistentStudentId + " не найден"))
                .andExpect(jsonPath("$.type").value("resource-not-found"));
    }

    @Test
    void getOverduePayments_shouldReturnOnlyPendingWithPastDueDate() throws Exception {
        Payment overduePending = Payment.builder()
                .student(student)
                .studyGroup(studyGroup)
                .amount(new BigDecimal("1000.00"))
                .status(PaymentStatus.PENDING)
                .description("Просроченный")
                .dueDate(LocalDate.now().minusDays(1))
                .paidAt(null)
                .build();
        paymentRepository.save(overduePending);

        Payment futurePending = Payment.builder()
                .student(student)
                .studyGroup(studyGroup)
                .amount(new BigDecimal("2000.00"))
                .status(PaymentStatus.PENDING)
                .description("Будущий")
                .dueDate(LocalDate.now().plusDays(5))
                .paidAt(null)
                .build();
        paymentRepository.save(futurePending);

        Payment paidOverdue = Payment.builder()
                .student(student)
                .studyGroup(studyGroup)
                .amount(new BigDecimal("1500.00"))
                .status(PaymentStatus.PAID)
                .description("Оплачен просроченный")
                .dueDate(LocalDate.now().minusDays(2))
                .paidAt(OffsetDateTime.now())
                .build();
        paymentRepository.save(paidOverdue);

        mockMvc.perform(get("/api/v1/payments/overdue")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(overduePending.getId()))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].dueDate").value(LocalDate.now().minusDays(1).toString()))
                .andExpect(jsonPath("$[0].paidAt").value(nullValue()));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void createPayment_teacher_shouldReturn403() throws Exception {
        PaymentCreateRequest request = PaymentCreateRequest.builder()
                .amount(new BigDecimal("1000.00"))
                .description("Оплата")
                .dueDate(LocalDate.now().plusDays(10))
                .build();

        mockMvc.perform(post("/api/v1/students/{studentId}/payments", student.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void createPayment_student_shouldReturn403() throws Exception {
        PaymentCreateRequest request = PaymentCreateRequest.builder()
                .amount(new BigDecimal("1000.00"))
                .description("Оплата")
                .dueDate(LocalDate.now().plusDays(10))
                .build();

        mockMvc.perform(post("/api/v1/students/{studentId}/payments", student.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}