package com.danieloliveira.tracking.events;

import com.danieloliveira.tracking.tracking.Tracking;
import com.danieloliveira.tracking.client.dto.TrackResponse;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    public static Event toEventEntity(TrackResponse.EventResponse eventResponse, Tracking tracking) {

        return Event.builder()
                .code(eventResponse.codigo())
                .description(eventResponse.descricao())
                .details(eventResponse.detalhe())
                .location(eventResponse.local())
                .dateEvent(eventResponse.data())
                .destination(eventResponse.destino())
                .tracking(tracking)
                .build();
    }
}
