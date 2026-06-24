package com.danieloliveira.tracking.email.dto;

import java.util.List;

public record BrevoEmailRequest(
        BrevoSender sender,
        List<BrevoRecipient> to,
        String subject,
        String htmlContent
) {
}
