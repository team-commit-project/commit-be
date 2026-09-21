package com.receiptmate.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupCompleteRequest {

    @NotBlank
    @Size(min = 1, max = 100)
    private String companyName;

    @NotBlank
    @Pattern(regexp = "[0-9]{3}-[0-9]{2}-[0-9]{5}")
    private String businessNumber;

    @NotBlank
    @Size(min = 1, max = 100)
    private String businessType;

    @NotBlank
    @Pattern(regexp = "^010[0-9]{8}$")
    private String phoneNumber;

    @NotNull
    @PositiveOrZero
    private Integer monthlyExpenseBudget;

    @NotNull
    private LocalDate receiptStartDate;
}
