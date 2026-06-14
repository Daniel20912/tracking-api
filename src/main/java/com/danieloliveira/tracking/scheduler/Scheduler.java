package com.danieloliveira.tracking.scheduler;

import com.danieloliveira.tracking.client.TrackingClient;
import com.danieloliveira.tracking.event.EventRepository;
import com.danieloliveira.tracking.tracking.Tracking;
import com.danieloliveira.tracking.tracking.TrackingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
class Scheduler {

    private final TrackingRepository trackingRepository;
    private final TrackingClient trackingClient;
    private final EventRepository eventRepository;
    private final TrackingUpdateService trackingUpdateService;

    @Scheduled(fixedDelayString = "${scheduler.interval}")
    void checkUpdates() {

        List<Tracking> trackings = trackingRepository.findAllByDeliveredFalse();

        for (Tracking tracking : trackings) {
            try {

                // check if the code exists
                var lastTracking = trackingClient.findTrack(tracking.getCode());

                if (lastTracking == null || !lastTracking.success()) {
                    log.error("Tracking with code {} not found", tracking.getCode());
                    continue;
                }

                // find the last tracking event
                var lastEvent = eventRepository.findFirstByTrackingOrderByDateEventDesc(tracking);

                if (lastEvent != null && lastTracking.eventoMaisRecente().data().equals(lastEvent.getDateEvent()))
                    continue;

                // compare the dates
                assert lastEvent != null;
                if (lastTracking.eventoMaisRecente().data().isEqual(lastEvent.getDateEvent()))
                    continue;

                // process tracking update
                trackingUpdateService.processTrackingUpdate(tracking, lastTracking.eventoMaisRecente());

            } catch (Exception e) {
                log.error("Error during checking tracking with code {}", tracking.getCode());
            }
        }
    }
}
