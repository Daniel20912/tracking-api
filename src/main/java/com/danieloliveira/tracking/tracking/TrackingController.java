package com.danieloliveira.tracking.tracking;

import com.danieloliveira.tracking.exception.ErrorMessage;
import com.danieloliveira.tracking.tracking.dto.TrackingRequestDTO;
import com.danieloliveira.tracking.tracking.dto.TrackingResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("tracking-api")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    @Operation(
            summary = "Register a new tracking",
            description = "Creates and registers a new package tracking entry into the system. Validates the input and ensures no duplicate tracking codes exist."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tracking successfully registered",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrackingResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid input data, validation failure, or tracking code already exists",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error - An unexpected error occurred",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))
            )
    })
    @PostMapping
    public TrackingResponseDTO registerNewTracking(@Valid @RequestBody TrackingRequestDTO trackingRequestDTO) {
        return trackingService.registerNewTracking(trackingRequestDTO);
    }

    @Operation(
            summary = "Find tracking by code",
            description = "Retrieves the tracking details and current status using the provided unique tracking code."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tracking information retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrackingResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid arguments or business rule violation",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found - The provided tracking code does not exist",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error - An unexpected error occurred",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))
            )
    })
    @GetMapping("/findByCode/{code}")
    public TrackingResponseDTO findTrackingByCode(@PathVariable String code) {
        return trackingService.findTrackingByCode(code);
    }
}
