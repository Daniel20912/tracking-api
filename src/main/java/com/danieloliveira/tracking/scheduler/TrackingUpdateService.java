package com.danieloliveira.tracking.scheduler;

import com.danieloliveira.tracking.client.dto.TrackResponse;
import com.danieloliveira.tracking.email.EmailSender;
import com.danieloliveira.tracking.event.Event;
import com.danieloliveira.tracking.event.EventService;
import com.danieloliveira.tracking.exception.EmailSendException;
import com.danieloliveira.tracking.tracking.Tracking;
import com.danieloliveira.tracking.tracking.TrackingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
class TrackingUpdateService {

    private final EventService eventService;
    private final TrackingRepository trackingRepository;
    private final EmailSender emailSender;

    @Transactional
    void processTrackingUpdate(Tracking tracking, List<TrackResponse.EventResponse> incoming) {

        // save only the events that are not in the database yet
        List<Event> newEvents = eventService.saveNewEvents(incoming, tracking);

        if (newEvents.isEmpty()) return;

        // check if any new event is BDE
        if (newEvents.stream().anyMatch(e -> "BDE".equals(e.getCode()))) {
            tracking.setDelivered(true);
            trackingRepository.save(tracking);
        }

        try {
            // send email about the most recent new event
            emailSender.sendEmail(tracking, newEvents.getLast());
        } catch (EmailSendException e) {
            log.error("Error sending email, but the status was updated successfully.");
        }
    }
}