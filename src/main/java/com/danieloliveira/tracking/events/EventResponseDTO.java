package com.danieloliveira.tracking.events;

import lombok.Getter;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Getter
public class EventResponseDTO implements Serializable {
    String code;
    String description;
    String details;
    String location;
    OffsetDateTime dateEvent;
    String destination;

    public EventResponseDTO(Event event) {
        this.code = event.getCode();
        this.description = event.getDescription();
        this.details = event.getDetails();
        this.location = event.getLocation();
        this.dateEvent = event.getCreatedAt();
        this.destination = event.getDestination();
    }
}