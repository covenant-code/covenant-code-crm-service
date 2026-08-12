package com.covenantcode.crm.service;

import com.covenantcode.crm.dto.payment.PaymentCreateRequest;
import com.covenantcode.crm.dto.payment.PaymentResponse;
import com.covenantcode.crm.entity.enums.PaymentStatus;

import java.util.List;

public interface PaymentService {

    PaymentResponse createPayment(Long studentId, PaymentCreateRequest request);
    PaymentResponse getPaymentById(Long paymentId);
    List<PaymentResponse> getPaymentsByStudentId(Long studentId);
    PaymentResponse updatePaymentStatus(Long paymentId, PaymentStatus newStatus);
    List<PaymentResponse> getOverduePayments();
}
