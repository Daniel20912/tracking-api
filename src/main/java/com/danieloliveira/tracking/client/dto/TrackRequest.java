package com.danieloliveira.tracking.client.dto;

public record TrackRequest(String code, String carrier) {

    private static final String CORREIOS = "CARRIER_CORREIOS";

    public static TrackRequest correios(String code) {
        return new TrackRequest(code, CORREIOS);
    }
}
