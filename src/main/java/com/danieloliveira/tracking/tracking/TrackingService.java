package com.danieloliveira.tracking.tracking;

import com.danieloliveira.tracking.client.TrackingClient;
import com.danieloliveira.tracking.client.dto.TrackResponse;
import com.danieloliveira.tracking.event.Event;
import com.danieloliveira.tracking.event.EventService;
import com.danieloliveira.tracking.event.dto.EventResponseDTO;
import com.danieloliveira.tracking.exception.ExternalServiceException;
import com.danieloliveira.tracking.exception.TrackingCodeAlreadyExistsException;
import com.danieloliveira.tracking.exception.TrackingNotFoundException;
import com.danieloliveira.tracking.tracking.dto.TrackingRequestDTO;
import com.danieloliveira.tracking.tracking.dto.TrackingResponseDTO;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingService {

    private final TrackingRepository trackingRepository;
    private final TrackingClient trackingClient;
    private final EventService eventService;

    @Transactional
    public TrackingResponseDTO registerNewTracking(TrackingRequestDTO request) {
        String code = request.getCode();

        if (trackingRepository.existsByCode(code)) {
            throw new TrackingCodeAlreadyExistsException("Code already exists!");
        }

        TrackResponse trackResponse;
        try {
            trackResponse = trackingClient.findTrack(code);
        } catch (FeignException ex) {
            log.error("Tracking provider error. Status {}: {}", ex.status(), ex.contentUTF8());
            throw new ExternalServiceException("Unable to access tracking provider");
        }

        if (trackResponse == null || !trackResponse.found() || trackResponse.payload() == null) {
            throw new TrackingNotFoundException("Tracking code not found!");
        }

        Tracking tracking = trackingRepository.save(toTrackingEntity(code, request.getEmail()));

        List<Event> events = eventService.saveNewEvents(trackResponse.payload().eventos(), tracking);

        if (!events.isEmpty() && "BDE".equals(events.getLast().getCode())) {
            tracking.setDelivered(true);
            trackingRepository.save(tracking);
        }

        return new TrackingResponseDTO(tracking, events.stream().map(EventResponseDTO::new).toList());
    }

    public TrackingResponseDTO findTrackingByCode(String code) {
        Tracking tracking = trackingRepository.findByCode(code).orElseThrow(() -> new TrackingNotFoundException("Tracking code not found!"));
        List<Event> eventList = eventService.findAllEvents(tracking);
        List<EventResponseDTO> responseList = eventList.stream()
                .map(EventResponseDTO::new).toList();

        return new TrackingResponseDTO(tracking, responseList);
    }


    /// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Tracking toTrackingEntity(String code, String email) {
        return Tracking.builder()
                .code(code)
                .email(email)
                .delivered(false)
                .build();
    }
}
