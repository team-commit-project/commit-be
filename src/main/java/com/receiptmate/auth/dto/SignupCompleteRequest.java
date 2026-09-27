package com.receiptmate.auth.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SignupCompleteRequest(

        @NotBlank
        @Size(max = 100)
        String companyName,

        @NotBlank
        @Pattern(
                regexp = "^\\d{3}-\\d{2}-\\d{5}$"
        )
        String businessNumber,

        @NotBlank
        @Size(max = 100)
        String businessType,

        @NotBlank
        @Pattern(
                regexp = "^\\d{11}$"
        )
        String phoneNumber,

        @NotNull
        @Min(0)
        Integer monthlyExpenseBudget,

        @NotNull
        LocalDate receiptStartDate
) {
}