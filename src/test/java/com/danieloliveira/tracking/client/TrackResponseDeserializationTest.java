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
              "carrier": "CARRIER_CORREIOS",
              "status": "TRACKING_STATUS_FOUND",
              "payload": {
                "codObjeto": "BR123456789BR",
                "dtPrevista": "2026-01-20",
                "situacao": "Entregue",
                "eventos": [
                  {
                    "codigo": "BDE",
                    "descricao": "Objeto entregue ao destinatário",
                    "detalhe": "Entrega realizada",
                    "dtHrCriado": {
                      "date": "2026-01-15 14:30:00.000",
                      "timezone": "America/Sao_Paulo"
                    },
                    "unidade": {
                      "tipo": "UNIDADE_DE_DISTRIBUICAO",
                      "endereco": {
                        "cidade": "São Paulo",
                        "uf": "SP"
                      }
                    }
                  }
                ]
              },
              "linkDetalhesCompletos": "https://seurastreio.com.br/objetos/BR123456789BR"
            }
            """;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve deserializar corretamente os campos raiz do TrackResponse")
    void shouldDeserializeRootFieldsCorrectly() throws Exception {
        var trackResponse = objectMapper.readValue(JSON, TrackResponse.class);

        assertThat(trackResponse.carrier()).isEqualTo("CARRIER_CORREIOS");
        assertThat(trackResponse.status()).isEqualTo("TRACKING_STATUS_FOUND");
        assertThat(trackResponse.found()).isTrue();
        assertThat(trackResponse.payload().codObjeto()).isEqualTo("BR123456789BR");
    }

    @Test
    @DisplayName("Deve deserializar corretamente os campos do eventoMaisRecente")
    void shouldDeserializeEventResponseFieldsCorrectly() throws Exception {
        var event = objectMapper.readValue(JSON, TrackResponse.class).payload().eventos().getFirst();

        assertThat(event.codigo()).isEqualTo("BDE");
        assertThat(event.descricao()).isEqualTo("Objeto entregue ao destinatário");
        assertThat(event.details()).isEqualTo("Entrega realizada");
        assertThat(event.location()).isEqualTo("São Paulo/SP");
    }

    @Test
    @DisplayName("Deve deserializar a data corretamente como OffsetDateTime")
    void shouldDeserializeDateAsOffsetDateTime() throws Exception {
        var event = objectMapper.readValue(JSON, TrackResponse.class).payload().eventos().getFirst();

        assertThat(event.occurredAt()).isNotNull();
        assertThat(event.occurredAt().getYear()).isEqualTo(2026);
        assertThat(event.occurredAt().getMonthValue()).isEqualTo(1);
        assertThat(event.occurredAt().getDayOfMonth()).isEqualTo(15);
        assertThat(event.occurredAt().getHour()).isEqualTo(14);
    }

    @Test
    @DisplayName("Deve ignorar campos desconhecidos como linkDetalhesCompletos")
    void shouldIgnoreUnknownFields() {
        assertThatNoException().isThrownBy(() -> objectMapper.readValue(JSON, TrackResponse.class));
    }
}