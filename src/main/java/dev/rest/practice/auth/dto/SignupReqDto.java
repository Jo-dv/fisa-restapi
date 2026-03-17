package dev.rest.practice.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record SignupReqDto(
        @NotBlank String username,
        @NotBlank String password
) {}