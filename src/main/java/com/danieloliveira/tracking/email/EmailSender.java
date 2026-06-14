package com.danieloliveira.tracking.email;

import com.danieloliveira.tracking.exception.EmailSendException;
import com.danieloliveira.tracking.tracking.Tracking;
import com.danieloliveira.tracking.client.dto.TrackResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class EmailSender {

    private static final Logger log = LoggerFactory.getLogger(EmailSender.class);
    private final JavaMailSender javaMailSender;
    private final EmailTemplate emailTemplate;

    @Value("${spring.mail.username}")
    private String sender;

    public void sendEmail(Tracking tracking, TrackResponse.EventResponse lastTracking) {

        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();

            mailMessage.setFrom(sender);
            mailMessage.setTo(tracking.getEmail());
            mailMessage.setSubject(emailTemplate.buildSubject(tracking.getCode()));
            mailMessage.setText(emailTemplate.buildText(
                    tracking.getCode(),
                    lastTracking.descricao(),
                    lastTracking.local(),
                    lastTracking.data()));

            javaMailSender.send(mailMessage);
            log.info("Email sent successfully");

        } catch (Exception e) {
            log.error("Email sent failed");
            throw new EmailSendException("Error sending email");
        }
    }
}
