package com.danieloliveira.tracking.tracking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrackingRepository extends JpaRepository<Tracking, Long> {
    boolean existsByCode(String code);

    Optional<Tracking> findByCode(String code);
}
