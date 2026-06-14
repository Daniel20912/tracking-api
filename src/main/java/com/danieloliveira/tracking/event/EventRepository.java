package com.danieloliveira.tracking.event;

import com.danieloliveira.tracking.tracking.Tracking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByTrackingOrderByDateEventAsc(Tracking tracking);

    Event findFirstByTrackingOrderByDateEventDesc(Tracking tracking);
}
