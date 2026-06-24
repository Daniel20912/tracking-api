package com.danieloliveira.tracking.email;

import com.danieloliveira.tracking.client.dto.TrackResponse;
import com.danieloliveira.tracking.email.dto.BrevoEmailRequest;
import com.danieloliveira.tracking.email.dto.BrevoRecipient;
import com.danieloliveira.tracking.email.dto.BrevoSender;
import com.danieloliveira.tracking.exception.EmailSendException;
import com.danieloliveira.tracking.tracking.Tracking;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class EmailSender {

    private static final Logger log = LoggerFactory.getLogger(EmailSender.class);
    private final BrevoClient brevoClient;
    private final EmailTemplate emailTemplate;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    public void sendEmail(Tracking tracking, TrackResponse.EventResponse lastTracking) {

        try {
            BrevoEmailRequest request = new BrevoEmailRequest(
                    new BrevoSender(senderName, senderEmail),
                    List.of(new BrevoRecipient(tracking.getEmail())),
                    emailTemplate.buildSubject(tracking.getCode()),
                    emailTemplate.buildHtml(
                            tracking.getCode(),
                            lastTracking.descricao(),
                            lastTracking.local(),
                            OffsetDateTime.from(lastTracking.data()))
            );

            brevoClient.sendTransactionalEmail(request);
            log.info("Email sent successfully to {}", tracking.getEmail());

        } catch (feign.FeignException e) {
            log.error("Brevo API integration error: Status {} - Response: {}", e.status(), e.contentUTF8());
            throw new EmailSendException("Failed to send email via Brevo: " + e.contentUTF8());
        } catch (Exception e) {
            log.error("Unexpected error occurred while sending email", e);
            throw new EmailSendException("Unexpected error during email sending: " + e.getMessage());
        }
    }
}
