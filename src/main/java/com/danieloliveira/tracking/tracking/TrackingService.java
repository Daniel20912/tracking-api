package com.danieloliveira.tracking.tracking;

import com.danieloliveira.tracking.events.EventService;
import com.danieloliveira.tracking.trackingClient.TrackResponse;
import com.danieloliveira.tracking.trackingClient.TrackingClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TrackingService {

    private final TrackingRepository trackingRepository;
    private final TrackingClient trackingClient;
    private final EventService eventService;

    public void registerNewTracking(TrackingRequestDTO trackingRequestDTO) {
        if (trackingRepository.existsByCode(trackingRequestDTO.getCode())) {
            throw new RuntimeException("Code already exists!");
        }

        var trackResponse = trackingClient.findTrack(trackingRequestDTO.getCode());
        if (!trackResponse.success()) {
            throw new RuntimeException("Tracking code not found!");
        }

        Tracking trackingEntity = toTrackingEntity(trackResponse, trackingRequestDTO);
        trackingRepository.save(trackingEntity);

        eventService.saveEvent(trackResponse.eventoMaisRecente(), trackingEntity);
    }

    private Tracking toTrackingEntity(TrackResponse trackResponse, TrackingRequestDTO trackingRequestDTO) {
        boolean isDelivered = Objects.equals(trackResponse.eventoMaisRecente().codigo(), "BDE");

        return Tracking.builder()
                .code(trackResponse.codigo())
                .email(trackingRequestDTO.getEmail())
                .delivered(isDelivered)
                .build();
    }
}
