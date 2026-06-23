package com.danieloliveira.tracking.event;

import com.danieloliveira.tracking.client.dto.TrackResponse;
import com.danieloliveira.tracking.tracking.Tracking;

import java.time.ZoneId;

public class EventMapper {

    public static Event toEventEntity(TrackResponse.EventResponse eventResponse, Tracking tracking) {

        return Event.builder()
                .code(eventResponse.codigo())
                .description(eventResponse.descricao())
                .details(eventResponse.detalhe())
                .location(eventResponse.local())
                .dateEvent(eventResponse.data().atZone(ZoneId.of("America/Sao_Paulo")).toOffsetDateTime())
                .destination(eventResponse.destino())
                .tracking(tracking)
                .build();
    }
}
