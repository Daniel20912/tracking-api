package com.danieloliveira.tracking.events;

import com.danieloliveira.tracking.tracking.Tracking;
import com.danieloliveira.tracking.trackingClient.TrackResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public void saveEvent(TrackResponse.EventResponse eventResponse, Tracking trackingEntity) {

        var Event = toEventEntity(eventResponse, trackingEntity);

        eventRepository.save(Event);
    }

    private Event toEventEntity(TrackResponse.EventResponse eventResponse, Tracking tracking) {

        return Event.builder()
                .code(eventResponse.codigo())
                .description(eventResponse.descricao())
                .details(eventResponse.detalhe())
                .location(eventResponse.local())
                .dateEvent(eventResponse.data().toLocalDateTime())
                .destination(eventResponse.destino())
                .tracking(tracking)
                .build();
    }
}
