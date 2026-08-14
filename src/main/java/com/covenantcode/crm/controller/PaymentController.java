package com.covenantcode.crm.controller;

import com.covenantcode.crm.dto.payment.PaymentCreateRequest;
import com.covenantcode.crm.dto.payment.PaymentResponse;
import com.covenantcode.crm.dto.payment.PaymentStatusUpdateRequest;
import com.covenantcode.crm.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Validated
@Slf4j
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/students/{studentId}/payments")
    public ResponseEntity<PaymentResponse> createPayment(
            @PathVariable Long studentId,
            @Valid @RequestBody PaymentCreateRequest request) {
        log.info("Creating payment for student {} with request: {}", studentId, request);
        PaymentResponse response = paymentService.createPayment(studentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/students/{studentId}/payments")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStudent(
            @PathVariable Long studentId){
        log.info("Fetching payments for student {}", studentId);
        List<PaymentResponse> payments = paymentService.getPaymentsByStudentId(studentId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/payments/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable Long id) {
        log.info("Fetching payment by id {}", id);
        PaymentResponse response = paymentService.getPaymentById(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/payments/{id}/status")
    public ResponseEntity<PaymentResponse> updatePaymentStatus(
            @PathVariable Long id,
            @Valid @RequestBody PaymentStatusUpdateRequest request) {
        log.info("Updating payment {} status to {}", id, request.getStatus());
        PaymentResponse response = paymentService.updatePaymentStatus(id, request.getStatus());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/payments/overdue")
    public ResponseEntity<List<PaymentResponse>> getOverduePayments() {
        log.info("Fetching overdue payments");
        List<PaymentResponse> overdue = paymentService.getOverduePayments();
        return ResponseEntity.ok(overdue);
    }
}
