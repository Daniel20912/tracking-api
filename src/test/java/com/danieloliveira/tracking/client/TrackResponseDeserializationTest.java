package com.danieloliveira.tracking.client;

import com.danieloliveira.tracking.client.dto.TrackResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@JsonTest
class TrackResponseDeserializationTest {

    private static final String JSON = """
            {
              "codigo": "BR123456789BR",
              "status": "found",
              "success": true,
              "eventoMaisRecente": {
                "codigo": "BDE",
                "descricao": "Objeto entregue ao destinatário",
                "detalhe": "Entrega realizada",
                "data": "2026-01-15T14:30:00.000Z",
                "local": "São Paulo/SP",
                "destino": null
              },
              "linkDetalhesCompletos": "https://seurastreio.com.br/objetos/BR123456789BR",
              "message": "Evento mais recente encontrado."
            }
            """;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve deserializar corretamente os campos raiz do TrackResponse")
    void shouldDeserializeRootFieldsCorrectly() throws Exception {
        var trackResponse = objectMapper.readValue(JSON, TrackResponse.class);

        assertThat(trackResponse.codigo()).isEqualTo("BR123456789BR");
        assertThat(trackResponse.status()).isEqualTo("found");
        assertThat(trackResponse.success()).isTrue();
        assertThat(trackResponse.message()).isEqualTo("Evento mais recente encontrado.");
    }

    @Test
    @DisplayName("Deve deserializar corretamente os campos do eventoMaisRecente")
    void shouldDeserializeEventResponseFieldsCorrectly() throws Exception {
        var event = objectMapper.readValue(JSON, TrackResponse.class).eventoMaisRecente();

        assertThat(event.codigo()).isEqualTo("BDE");
        assertThat(event.descricao()).isEqualTo("Objeto entregue ao destinatário");
        assertThat(event.detalhe()).isEqualTo("Entrega realizada");
        assertThat(event.local()).isEqualTo("São Paulo/SP");
        assertThat(event.destino()).isNull(); // destino null é válido
    }

    @Test
    @DisplayName("Deve deserializar a data corretamente como OffsetDateTime")
    void shouldDeserializeDateAsOffsetDateTime() throws Exception {
        var event = objectMapper.readValue(JSON, TrackResponse.class).eventoMaisRecente();

        assertThat(event.data()).isNotNull();
        assertThat(event.data().getYear()).isEqualTo(2026);
        assertThat(event.data().getMonthValue()).isEqualTo(1);
        assertThat(event.data().getDayOfMonth()).isEqualTo(15);
        assertThat(event.data().getHour()).isEqualTo(14);
        assertThat(event.data().getMinute()).isEqualTo(30);
    }

    @Test
    @DisplayName("Deve ignorar campos desconhecidos como linkDetalhesCompletos")
    void shouldIgnoreUnknownFields() {
        assertThatNoException().isThrownBy(() -> objectMapper.readValue(JSON, TrackResponse.class));
    }
}