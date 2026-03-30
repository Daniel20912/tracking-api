package com.danieloliveira.tracking.tracking;

import com.danieloliveira.tracking.Events.Event;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Tracking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String email;

    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean delivered;

    @OneToMany(mappedBy = "tracking", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<Event> events;

    public void addEvent(Event event) {
        events.add(event);
        event.setTracking(this);
    }
}
