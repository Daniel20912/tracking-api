package com.danieloliveira.tracking.tracking;

import com.danieloliveira.tracking.events.Event;
import com.danieloliveira.tracking.events.EventResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

public class TrackingResponseDTO {
    private String code;
    private String email;
    private LocalDateTime date;
    private boolean delivered;
    private List<EventResponseDTO> events;

    public TrackingResponseDTO(Tracking tracking, Event event) {
        this.code = tracking.getCode();
        this.email = tracking.getEmail();
        this.date = tracking.getCreatedAt();
        this.delivered = tracking.isDelivered();
        this.events = List.of(new EventResponseDTO(event));
    }
}
