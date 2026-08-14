package com.covenantcode.crm.mapper;

import com.covenantcode.crm.dto.payment.PaymentCreateRequest;
import com.covenantcode.crm.dto.payment.PaymentResponse;
import com.covenantcode.crm.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface PaymentMapper {

    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "studentName", expression = "java(payment.getStudent().getFirstName() + \" \" + payment.getStudent().getLastName())")
    @Mapping(target = "studyGroupId", source = "studyGroup.id")
    @Mapping(target = "createdAt", expression = "java(payment.getCreatedAt().toLocalDateTime())")
    @Mapping(target = "paidAt", expression = "java(payment.getPaidAt() != null ? payment.getPaidAt().toLocalDateTime() : null)")
    PaymentResponse toResponse(Payment payment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "studyGroup", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "paidAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Payment toEntity(PaymentCreateRequest request);
}
