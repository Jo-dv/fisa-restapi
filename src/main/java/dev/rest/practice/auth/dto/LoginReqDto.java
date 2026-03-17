package dev.rest.practice.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginReqDto(
        @NotBlank String username,
        @NotBlank String password
) {}