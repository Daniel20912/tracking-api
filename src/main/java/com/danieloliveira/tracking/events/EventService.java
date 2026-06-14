package com.danieloliveira.tracking.events;

import com.danieloliveira.tracking.tracking.Tracking;
import com.danieloliveira.tracking.client.dto.TrackResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public Event saveEvent(TrackResponse.EventResponse eventResponse, Tracking trackingEntity) {

        var Event = EventMapper.toEventEntity(eventResponse, trackingEntity);

        eventRepository.save(Event);

        return Event;
    }

    public List<Event> findAllEvents(Tracking tracking) {
        return eventRepository.findAllByTrackingOrderByDateEventAsc(tracking);
    }
}
