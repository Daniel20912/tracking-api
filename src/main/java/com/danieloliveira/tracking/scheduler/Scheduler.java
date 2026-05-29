package com.danieloliveira.tracking.scheduler;

import com.danieloliveira.tracking.email.EmailSender;
import com.danieloliveira.tracking.events.EventMapper;
import com.danieloliveira.tracking.events.EventRepository;
import com.danieloliveira.tracking.tracking.Tracking;
import com.danieloliveira.tracking.tracking.TrackingRepository;
import com.danieloliveira.tracking.trackingClient.TrackingClient;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
class Scheduler {

    private final TrackingRepository trackingRepository;
    private final TrackingClient trackingClient;
    private final EventRepository eventRepository;
    private final EmailSender emailSender;

    @Scheduled(fixedDelayString = "${scheduler.interval}")
    void checkUpdates() {

        List<Tracking> trackings = trackingRepository.findAllByDeliveredFalse();

        for (Tracking tracking : trackings) {
            try {

                // check if the code exists
                var lastTracking = trackingClient.findTrack(tracking.getCode());

                if (lastTracking == null || !lastTracking.success()) {
                    System.err.printf("Tracking with code " + tracking.getCode() + " not found");
                    continue;
                }

                // find the last tracking event
                var lastEvent = eventRepository.findFirstByTrackingOrderByDateEventDesc(tracking);

                // compare the dates
                if (lastTracking.eventoMaisRecente().data().isEqual(lastEvent.getDateEvent()))
                    continue;

                // save event in database
                var newEvent = EventMapper.toEventEntity(lastTracking.eventoMaisRecente(), lastEvent.getTracking());
                eventRepository.save(newEvent);

                // send email
                emailSender.sendEmail(tracking, lastTracking.eventoMaisRecente());

                // check if code is BDE
                if (Objects.equals(lastTracking.eventoMaisRecente().codigo(), "BDE")) {
                    tracking.setDelivered(true);
                    trackingRepository.save(tracking);
                }

            } catch (Exception e) {
                System.err.printf("Error during checking tracking with code " + tracking.getCode());
            }
        }
    }
}
