package com.danieloliveira.tracking.tracking;

import jakarta.validation.constraints.Email;
import lombok.Getter;

@Getter
public class TrackingRequestDTO {
    private String code;

    @Email
    private String email;
}
