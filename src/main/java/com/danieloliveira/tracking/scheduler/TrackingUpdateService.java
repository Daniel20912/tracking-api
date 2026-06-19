package com.danieloliveira.tracking.scheduler;

import com.danieloliveira.tracking.client.dto.TrackResponse;
import com.danieloliveira.tracking.email.EmailSender;
import com.danieloliveira.tracking.event.EventMapper;
import com.danieloliveira.tracking.event.EventRepository;
import com.danieloliveira.tracking.exception.EmailSendException;
import com.danieloliveira.tracking.tracking.Tracking;
import com.danieloliveira.tracking.tracking.TrackingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
class TrackingUpdateService {

    private final EventRepository eventRepository;
    private final TrackingRepository trackingRepository;
    private final EmailSender emailSender;

    @Transactional
    void processTrackingUpdate(Tracking tracking, TrackResponse.EventResponse newEventData) {

        // save event in database
        var newEvent = EventMapper.toEventEntity(newEventData, tracking);
        eventRepository.save(newEvent);

        // check if code is BDE
        if ("BDE".equals(newEventData.codigo())) {
            tracking.setDelivered(true);
            trackingRepository.save(tracking);
        }

        try {
            // send email
            emailSender.sendEmail(tracking, newEventData);
        } catch (EmailSendException e) {
            log.error("Error sending email, but the status was updated successfully.");
        }
    }
}