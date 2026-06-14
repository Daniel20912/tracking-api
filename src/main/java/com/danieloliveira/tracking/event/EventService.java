package com.danieloliveira.tracking.event;

import com.danieloliveira.tracking.client.dto.TrackResponse;
import com.danieloliveira.tracking.exception.BusinessException;
import com.danieloliveira.tracking.exception.TrackingNotFoundException;
import com.danieloliveira.tracking.tracking.Tracking;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public Event saveEvent(TrackResponse.EventResponse eventResponse, Tracking trackingEntity) {
        if (eventResponse == null || trackingEntity == null)
            throw new IllegalArgumentException("Event or Tracking entity is null");

        if (trackingEntity.isDelivered())
            throw new BusinessException("Event is already delivered");


        var Event = EventMapper.toEventEntity(eventResponse, trackingEntity);

        eventRepository.save(Event);

        return Event;
    }

    public List<Event> findAllEvents(Tracking tracking) {
        if (tracking == null || tracking.getId() == null)
            throw new TrackingNotFoundException("Tracking entity is null");

        return eventRepository.findAllByTrackingOrderByDateEventAsc(tracking);
    }
}
