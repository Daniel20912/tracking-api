package com.danieloliveira.tracking.client;


import com.danieloliveira.tracking.client.dto.TrackRequest;
import com.danieloliveira.tracking.client.dto.TrackResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "trackingClient",
        url = "${config.api.url}",
        configuration = FeignConfig.class
)
public interface TrackingClient {

    @PostMapping("/Track")
    TrackResponse track(@RequestBody TrackRequest request);

    default TrackResponse findTrack(String code) {
        return track(TrackRequest.correios(code));
    }
}