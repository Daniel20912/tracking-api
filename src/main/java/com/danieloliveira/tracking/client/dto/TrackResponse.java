package com.danieloliveira.tracking.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

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
            LocalDateTime data,
            String local,
            String destino
    ) {
    }
}
