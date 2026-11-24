package com.danieloliveira.tracking.event;

import com.danieloliveira.tracking.client.dto.TrackResponse;
import com.danieloliveira.tracking.tracking.Tracking;

public class EventMapper {

    public static Event toEventEntity(TrackResponse.EventResponse response, Tracking tracking) {
        return Event.builder()
                .code(response.codigo())
                .description(response.descricao())
                .details(response.details())
                .location(response.location())
                .dateEvent(response.occurredAt())
                .tracking(tracking)
                .build();
    }
}
