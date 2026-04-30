package com.danieloliveira.tracking.tracking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("tracking-api")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    public TrackingResponseDTO registerNewTracking(@RequestBody TrackingRequestDTO trackingRequestDTO) {
        return trackingService.registerNewTracking(trackingRequestDTO);
    }
}
