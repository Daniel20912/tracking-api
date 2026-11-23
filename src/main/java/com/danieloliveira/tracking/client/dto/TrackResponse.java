package com.danieloliveira.tracking.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TrackResponse(String carrier, String status, Payload payload) {

    public boolean found() {
        return "TRACKING_STATUS_FOUND".equals(status);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(String codObjeto, String dtPrevista,
                          String situacao, List<EventResponse> eventos) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EventResponse(String codigo, String descricao, String detalhe,
                                EventDate dtHrCriado, Unidade unidade) {

        public OffsetDateTime occurredAt() {
            return dtHrCriado.toOffsetDateTime();
        }

        public String location() {
            if (unidade == null || unidade.endereco() == null) return null;
            var e = unidade.endereco();
            if (e.cidade() == null || e.cidade().isBlank()) return null;
            return (e.uf() == null || e.uf().isBlank()) ? e.cidade() : e.cidade() + "/" + e.uf();
        }

        public String details() {
            return (detalhe == null || detalhe.isBlank()) ? null : detalhe;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EventDate(String date, String timezone) {

        private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd HH:mm:ss")
                .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                .toFormatter();

        public OffsetDateTime toOffsetDateTime() {
            ZoneId zone = ZoneId.of(timezone != null ? timezone : "America/Sao_Paulo");
            return LocalDateTime.parse(date, FORMATTER).atZone(zone).toOffsetDateTime();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Unidade(String tipo, Endereco endereco) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Endereco(String cidade, String uf) {
    }
}
