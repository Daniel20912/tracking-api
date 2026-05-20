package com.danieloliveira.tracking.tracking;

import com.danieloliveira.tracking.events.EventResponseDTO;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
public class TrackingResponseDTO {
    private final String code;
    private final String email;
    private final OffsetDateTime date;
    private final boolean delivered;
    private final List<EventResponseDTO> events;


    public TrackingResponseDTO(Tracking tracking, List<EventResponseDTO> eventList) {
        this.code = tracking.getCode();
        this.email = tracking.getEmail();
        this.date = tracking.getCreatedAt();
        this.delivered = tracking.isDelivered();
        this.events = eventList;
    }
}
