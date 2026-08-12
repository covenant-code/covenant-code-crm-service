package com.covenantcode.crm.service.impl;

import com.covenantcode.crm.dto.payment.PaymentCreateRequest;
import com.covenantcode.crm.dto.payment.PaymentResponse;
import com.covenantcode.crm.entity.Payment;
import com.covenantcode.crm.entity.Student;
import com.covenantcode.crm.entity.StudyGroup;
import com.covenantcode.crm.entity.enums.PaymentStatus;
import com.covenantcode.crm.exception.ResourceNotFoundException;
import com.covenantcode.crm.mapper.PaymentMapper;
import com.covenantcode.crm.repository.PaymentRepository;
import com.covenantcode.crm.repository.StudentRepository;
import com.covenantcode.crm.repository.StudyGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudyGroupRepository studyGroupRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Student student;
    private StudyGroup studyGroup;
    private Payment payment;
    private PaymentResponse paymentResponse;
    private PaymentCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        student = Student.builder()
                .id(1L)
                .firstName("Иван")
                .lastName("Петров")
                .build();

        studyGroup = StudyGroup.builder()
                .id(10L)
                .name("Группа А")
                .build();

        payment = Payment.builder()
                .id(100L)
                .student(student)
                .studyGroup(studyGroup)
                .amount(new BigDecimal("5000.00"))
                .status(PaymentStatus.PENDING)
                .description("Оплата за курс")
                .dueDate(LocalDate.of(2026, 9, 1))
                .paidAt(null)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        paymentResponse = PaymentResponse.builder()
                .id(100L)
                .studentId(1L)
                .studentName("Иван Петров")
                .amount(new BigDecimal("5000.00"))
                .status("PENDING")
                .description("Оплата за курс")
                .dueDate(LocalDate.of(2026, 9, 1))
                .studyGroupId(10L)
                .createdAt(LocalDateTime.now())
                .paidAt(null)
                .build();

        createRequest = PaymentCreateRequest.builder()
                .amount(new BigDecimal("5000.00"))
                .description("Оплата за курс")
                .dueDate(LocalDate.of(2026, 9, 1))
                .studyGroupId(10L)
                .build();
    }

    @Test
    void createPayment_shouldSetPendingStatusAndNullPaidAt() {
        Long studentId = 1L;
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(studyGroupRepository.findById(10L)).thenReturn(Optional.of(studyGroup));
        when(paymentMapper.toEntity(createRequest)).thenReturn(payment);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper.toResponse(payment)).thenReturn(paymentResponse);

        PaymentResponse result = paymentService.createPayment(studentId, createRequest);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getPaidAt()).isNull();

        verify(paymentRepository).save(argThat(savedPayment ->
                savedPayment.getStatus() == PaymentStatus.PENDING &&
                        savedPayment.getPaidAt() == null
        ));
        verify(paymentMapper).toEntity(createRequest);
        verify(paymentMapper).toResponse(payment);
    }

    @Test
    void updatePaymentStatus_toPaid_shouldSetPaidAt() {
        Long paymentId = 100L;
        PaymentStatus newStatus = PaymentStatus.PAID;

        Payment existingPayment = Payment.builder()
                .id(paymentId)
                .student(student)
                .amount(new BigDecimal("5000.00"))
                .status(PaymentStatus.PENDING)
                .dueDate(LocalDate.of(2026, 9, 1))
                .paidAt(null)
                .build();

        Payment savedPayment = Payment.builder()
                .id(paymentId)
                .student(student)
                .amount(new BigDecimal("5000.00"))
                .status(PaymentStatus.PAID)
                .dueDate(LocalDate.of(2026, 9, 1))
                .paidAt(OffsetDateTime.now())
                .build();

        PaymentResponse expectedResponse = PaymentResponse.builder()
                .id(paymentId)
                .studentId(1L)
                .studentName("Иван Петров")
                .amount(new BigDecimal("5000.00"))
                .status("PAID")
                .dueDate(LocalDate.of(2026, 9, 1))
                .paidAt(LocalDateTime.now())
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(existingPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        when(paymentMapper.toResponse(any(Payment.class))).thenReturn(expectedResponse);

        PaymentResponse result = paymentService.updatePaymentStatus(paymentId, newStatus);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("PAID");
        assertThat(result.getPaidAt()).isNotNull();

        verify(paymentMapper).toResponse(any(Payment.class));
    }

    @Test
    void updatePaymentStatus_toCancelled_shouldNotSetPaidAt() {
        Long paymentId = 100L;
        PaymentStatus newStatus = PaymentStatus.CANCELLED;

        Payment existingPayment = Payment.builder()
                .id(paymentId)
                .student(student)
                .amount(new BigDecimal("5000.00"))
                .status(PaymentStatus.PENDING)
                .dueDate(LocalDate.of(2026, 9, 1))
                .paidAt(null)
                .build();

        Payment savedPayment = Payment.builder()
                .id(paymentId)
                .student(student)
                .amount(new BigDecimal("5000.00"))
                .status(PaymentStatus.CANCELLED)
                .dueDate(LocalDate.of(2026, 9, 1))
                .paidAt(null)
                .build();

        PaymentResponse expectedResponse = PaymentResponse.builder()
                .id(paymentId)
                .studentId(1L)
                .studentName("Иван Петров")
                .amount(new BigDecimal("5000.00"))
                .status("CANCELLED")
                .dueDate(LocalDate.of(2026, 9, 1))
                .paidAt(null)
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(existingPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        when(paymentMapper.toResponse(savedPayment)).thenReturn(expectedResponse);

        PaymentResponse result = paymentService.updatePaymentStatus(paymentId, newStatus);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("CANCELLED");
        assertThat(result.getPaidAt()).isNull();

        verify(paymentRepository).save(argThat(p ->
                p.getStatus() == PaymentStatus.CANCELLED && p.getPaidAt() == null
        ));
    }

    @Test
    void getOverduePayments_shouldReturnPendingWithDueDateBeforeToday() {
        Payment overdue1 = Payment.builder()
                .id(1L)
                .student(student)
                .amount(new BigDecimal("1000.00"))
                .status(PaymentStatus.PENDING)
                .dueDate(LocalDate.now().minusDays(1))
                .paidAt(null)
                .build();

        Payment overdue2 = Payment.builder()
                .id(2L)
                .student(student)
                .amount(new BigDecimal("2000.00"))
                .status(PaymentStatus.PENDING)
                .dueDate(LocalDate.now().minusDays(5))
                .paidAt(null)
                .build();

        List<Payment> overduePayments = List.of(overdue1, overdue2);

        PaymentResponse response1 = PaymentResponse.builder()
                .id(1L).studentId(1L).studentName("Иван Петров")
                .amount(new BigDecimal("1000.00")).status("PENDING")
                .dueDate(LocalDate.now().minusDays(1)).paidAt(null).build();

        PaymentResponse response2 = PaymentResponse.builder()
                .id(2L).studentId(1L).studentName("Иван Петров")
                .amount(new BigDecimal("2000.00")).status("PENDING")
                .dueDate(LocalDate.now().minusDays(5)).paidAt(null).build();

        when(paymentRepository.findAllByStatusAndDueDateBefore(eq(PaymentStatus.PENDING), any(LocalDate.class)))
                .thenReturn(overduePayments);
        when(paymentMapper.toResponse(overdue1)).thenReturn(response1);
        when(paymentMapper.toResponse(overdue2)).thenReturn(response2);

        List<PaymentResponse> result = paymentService.getOverduePayments();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(PaymentResponse::getStatus).containsOnly("PENDING");
        assertThat(result).extracting(PaymentResponse::getDueDate)
                .allMatch(date -> date.isBefore(LocalDate.now()));

        verify(paymentRepository).findAllByStatusAndDueDateBefore(
                PaymentStatus.PENDING,
                LocalDate.now()
        );
    }

    @Test
    void createPayment_studentNotFound_shouldThrowResourceNotFoundException() {
        Long studentId = 999L;
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.createPayment(studentId, createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Student с id " + studentId + " не найден");

        verify(studentRepository).findById(studentId);
        verifyNoInteractions(studyGroupRepository);
        verifyNoInteractions(paymentRepository);
    }

    @Test
    void updatePaymentStatus_paymentNotFound_shouldThrowResourceNotFoundException() {
        Long paymentId = 999L;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.updatePaymentStatus(paymentId, PaymentStatus.PAID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Payment с id " + paymentId + " не найден");
    }
}
