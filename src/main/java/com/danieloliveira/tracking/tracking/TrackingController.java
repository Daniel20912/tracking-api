package com.danieloliveira.tracking.tracking;

import com.danieloliveira.tracking.tracking.dto.TrackingRequestDTO;
import com.danieloliveira.tracking.tracking.dto.TrackingResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("tracking-api")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    @PostMapping
    public TrackingResponseDTO registerNewTracking(@Valid @RequestBody TrackingRequestDTO trackingRequestDTO) {
        return trackingService.registerNewTracking(trackingRequestDTO);
    }

    @GetMapping("/findByCode/{code}")
    public TrackingResponseDTO findTrackingByCode(@PathVariable String code) {
        return trackingService.findTrackingByCode(code);
    }
}
