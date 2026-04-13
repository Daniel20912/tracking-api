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

    // TODO retornar o status de crated e um DTO de resposta
    public void registerNewTracking(@RequestBody TrackingRequestDTO trackingRequestDTO) {
        trackingService.registerNewTracking(trackingRequestDTO);
    }
}
