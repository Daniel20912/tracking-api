package com.danieloliveira.tracking.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;

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
            OffsetDateTime data,
            String local,
            String destino
    ) {
    }
}
