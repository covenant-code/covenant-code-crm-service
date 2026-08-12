package com.covenantcode.crm.telegram.lead;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadBotSession {

    private Long chatId;

    @Builder.Default
    private BotStep step = BotStep.IDLE;

    private String firstName;

    private String lastName;

    private String phone;

    private Long courseId;

    private String courseName;
}
