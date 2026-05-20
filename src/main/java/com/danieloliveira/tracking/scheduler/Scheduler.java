package com.danieloliveira.tracking.scheduler;

import com.danieloliveira.tracking.email.EmailSender;
import com.danieloliveira.tracking.events.EventRepository;
import com.danieloliveira.tracking.events.EventService;
import com.danieloliveira.tracking.tracking.TrackingRepository;
import com.danieloliveira.tracking.trackingClient.TrackingClient;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class Scheduler {

    private final TrackingRepository trackingRepository;
    private final TrackingClient trackingClient;
    private final EventRepository eventRepository;
    private final EventService eventService;
    private final EmailSender emailSender;

    @Scheduled(fixedRate = 1000)
    private void CheckUpdates() {
        System.out.printf("Checking updates " + LocalDateTime.now() + "\n");
    }
}
