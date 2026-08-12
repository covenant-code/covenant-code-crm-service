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
import com.covenantcode.crm.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final StudentRepository studentRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional
    public PaymentResponse createPayment(Long studentId, PaymentCreateRequest request) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student с id " + studentId + " не найден"));
        StudyGroup group = null;
        if (request.getStudyGroupId() != null) {
            group = studyGroupRepository.findById(request.getStudyGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "StudyGroup с id " + request.getStudyGroupId() + " не найдена"));
        }

        Payment payment = paymentMapper.toEntity(request);
        payment.setStudent(student);
        payment.setStudyGroup(group);
        payment.setStatus(PaymentStatus.PENDING);

        Payment saved = paymentRepository.save(payment);
        return paymentMapper.toResponse(saved);
    }

    @Override
    public PaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment с id " + paymentId + " не найден"));
        return paymentMapper.toResponse(payment);
    }

    @Override
    public List<PaymentResponse> getPaymentsByStudentId(Long studentId) {
        return paymentRepository.findAllByStudentId(studentId).stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PaymentResponse updatePaymentStatus(Long paymentId, PaymentStatus newStatus) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment с id " + paymentId + " не найден"));

        if (newStatus == PaymentStatus.PAID) {
            payment.setPaidAt(OffsetDateTime.now());
        }
        payment.setStatus(newStatus);

        Payment updated = paymentRepository.save(payment);
        return paymentMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getOverduePayments() {
        List<Payment> overdue = paymentRepository.findAllByStatusAndDueDateBefore(
                PaymentStatus.PENDING, LocalDate.now());
        return overdue.stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
    }
}
