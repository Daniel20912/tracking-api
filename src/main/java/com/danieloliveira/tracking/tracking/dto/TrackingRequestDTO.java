package com.danieloliveira.tracking.tracking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TrackingRequestDTO {

    @Pattern(regexp = "^[A-Z]{2}[0-9]{9}[A-Z]{2}$", message = "Invalid tracking code format")
    @NotBlank(message = "Code cannot be empty")
    private String code;

    @Email(message = "Invalid email format", regexp = "^[a-zA-Z0-9.+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
    private String email;
}
