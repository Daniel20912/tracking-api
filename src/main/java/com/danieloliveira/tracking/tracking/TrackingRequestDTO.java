package com.danieloliveira.tracking.tracking;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TrackingRequestDTO {
    private String code;

    @Email
    private String email;
}
