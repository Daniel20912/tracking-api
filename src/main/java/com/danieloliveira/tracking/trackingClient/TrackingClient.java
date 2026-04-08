package com.danieloliveira.tracking.trackingClient;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "trackingClient",
        url = "${config.api.url}",
        configuration = FeignConfig.class
)
public interface TrackingClient {

    @GetMapping("/api/public/rastreio/{code}")
    TrackResponse findTrack(@PathVariable String code);
}