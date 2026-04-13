package com.danieloliveira.tracking.tracking;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackingRepository extends JpaRepository<Tracking, Long> {
    boolean existsByCode(String code);
}
