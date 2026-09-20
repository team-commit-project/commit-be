package com.receiptmate.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SignupRequest(

        @NotBlank
        String companyName,

        @NotBlank
        String businessNumber,

        @NotBlank
        String businessType,

        @NotBlank
        String phoneNumber,

        @NotNull
        Integer monthlyExpenseBudget,

        @NotNull
        LocalDate receiptStartDate
) {
}