package com.danieloliveira.tracking.tracking;

import com.danieloliveira.tracking.events.Event;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Tracking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    @Column(nullable = false)
    private boolean delivered;

    @OneToMany(mappedBy = "tracking", orphanRemoval = true)
    private List<Event> events;

    @PrePersist
    private void prePersist() {
        this.createdAt = OffsetDateTime.now();
    }

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
