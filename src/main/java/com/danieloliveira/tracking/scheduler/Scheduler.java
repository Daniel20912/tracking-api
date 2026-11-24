package com.danieloliveira.tracking.scheduler;

import com.danieloliveira.tracking.client.TrackingClient;
import com.danieloliveira.tracking.client.dto.TrackResponse;
import com.danieloliveira.tracking.tracking.Tracking;
import com.danieloliveira.tracking.tracking.TrackingRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
class Scheduler {

    private final TrackingRepository trackingRepository;
    private final TrackingClient trackingClient;
    private final TrackingUpdateService trackingUpdateService;

    @Scheduled(fixedDelayString = "${scheduler.interval}")
    void checkUpdates() {
        // Get active tracking records that have not reached the delivered status.
        List<Tracking> trackings = trackingRepository.findAllByDeliveredFalse();

        for (Tracking tracking : trackings) {
            try {
                // Ask the provider for the latest tracking information.
                TrackResponse response = trackingClient.findTrack(tracking.getCode());

                // Skip records whose provider response contains no usable tracking payload.
                if (response == null || !response.found() || response.payload() == null) {
                    log.warn("Tracking with code {} not found by the provider", tracking.getCode());
                    continue;
                }

                // Save new events and trigger delivery updates or email notifications.
                trackingUpdateService.processTrackingUpdate(tracking, response.payload().eventos());

            } catch (FeignException e) {
                // Abort the cycle only for provider authentication or configuration failures.
                if (isFatalProviderError(e)) {
                    log.error("Provider rejected the request (status {}): {}. Aborting this cycle.",
                            e.status(), e.contentUTF8());
                    return;
                }
                log.warn("Provider error while checking {} (status {}). Will retry next cycle.",
                        tracking.getCode(), e.status());
            } catch (Exception e) {
                log.error("Error during checking tracking with code {}: {}", tracking.getCode(), e.getMessage());
            }
        }
    }

    private boolean isFatalProviderError(FeignException e) {
        return e.status() == 401 || e.contentUTF8().contains("failed_precondition");
    }
}
