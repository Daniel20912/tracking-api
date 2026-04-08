package com.danieloliveira.tracking.trackingClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TrackResponse(
        String codigo,
        String status,
        boolean success,
        EventResponse eventoMaisRecente,
        String message
) {

    public record EventResponse(
            String codigo,
            String descricao,
            String detalhe,
            java.time.OffsetDateTime data,
            String local,
            String destino
    ) {
    }
}
