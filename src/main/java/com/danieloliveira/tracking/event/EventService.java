package com.danieloliveira.tracking.event;

import com.danieloliveira.tracking.client.dto.TrackResponse;
import com.danieloliveira.tracking.exception.BusinessException;
import com.danieloliveira.tracking.exception.TrackingNotFoundException;
import com.danieloliveira.tracking.tracking.Tracking;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    @Transactional
    public List<Event> saveNewEvents(List<TrackResponse.EventResponse> incoming, Tracking tracking) {
        if (tracking == null)
            throw new IllegalArgumentException("Tracking entity is null");

        if (tracking.isDelivered())
            throw new BusinessException("Tracking is already delivered");

        if (incoming == null || incoming.isEmpty())
            return List.of();

        Set<String> known = findAllEvents(tracking).stream()
                .map(e -> key(e.getCode(), e.getDateEvent()))
                .collect(Collectors.toCollection(HashSet::new));

        List<Event> toSave = incoming.stream()
                .filter(e -> e.dtHrCriado() != null && e.dtHrCriado().date() != null)
                .sorted(Comparator.comparing(TrackResponse.EventResponse::occurredAt))
                .filter(e -> known.add(key(e.codigo(), e.occurredAt())))
                .map(e -> EventMapper.toEventEntity(e, tracking))
                .toList();

        return eventRepository.saveAll(toSave);
    }

    public List<Event> findAllEvents(Tracking tracking) {
        if (tracking == null || tracking.getId() == null)
            throw new TrackingNotFoundException("Tracking entity is null");

        return eventRepository.findAllByTrackingOrderByDateEventAsc(tracking);
    }

    private String key(String code, OffsetDateTime date) {
        return code + "|" + date.toInstant();
    }
}
