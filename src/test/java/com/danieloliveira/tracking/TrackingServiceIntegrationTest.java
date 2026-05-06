package com.danieloliveira.tracking;

import com.danieloliveira.tracking.events.EventRepository;
import com.danieloliveira.tracking.tracking.TrackingRepository;
import com.danieloliveira.tracking.tracking.TrackingRequestDTO;
import com.danieloliveira.tracking.tracking.TrackingService;
import com.danieloliveira.tracking.trackingClient.TrackResponse;
import com.danieloliveira.tracking.trackingClient.TrackingClient;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class TrackingServiceIntegrationTest {

    @Autowired
    private TrackingService trackingService;

    @Autowired
    private TrackingRepository trackingRepository;

    @Autowired
    private EventRepository eventRepository;

    @MockitoBean
    private TrackingClient trackingClient; // mock do cliente externo

    private TrackingRequestDTO requestDTO;
    private TrackResponse trackResponse;

    @BeforeEach
    void setUp() {
        requestDTO = new TrackingRequestDTO("BR123456789", "teste@email.com");

        var eventResponse = new TrackResponse.EventResponse(
                "OEC",
                "Objeto em trânsito",
                "De Curitiba para São Paulo",
                OffsetDateTime.now(),
                "Curitiba/PR",
                "São Paulo/SP"
        );

        trackResponse = new TrackResponse(
                "BR123456789",
                "Em trânsito",
                true,
                eventResponse,
                null
        );
    }

    @Test
    @DisplayName("Deve salvar o Tracking e o Event no banco quando o código é válido")
    void shouldSaveTrackingAndEventWhenCodeIsValid() {
        when(trackingClient.findTrack("BR123456789")).thenReturn(trackResponse);

        var response = trackingService.registerNewTracking(requestDTO);

        // verifica o retorno do service
        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo("BR123456789");
        assertThat(response.getEmail()).isEqualTo("teste@email.com");
        assertThat(response.isDelivered()).isFalse();
        assertThat(response.getEvents()).hasSize(1);

        // verifica se foi salvo no banco de verdade
        assertThat(trackingRepository.existsByCode("BR123456789")).isTrue();
        assertThat(eventRepository.findAll()).hasSize(1);

        var savedEvent = eventRepository.findAll().getFirst();
        assertThat(savedEvent.getCode()).isEqualTo("OEC");
        assertThat(savedEvent.getDescription()).isEqualTo("Objeto em trânsito");
        assertThat(savedEvent.getTracking().getCode()).isEqualTo("BR123456789");
    }

    @Test
    @DisplayName("Deve marcar como entregue quando o código do evento for BDE")
    void shouldMarkAsDeliveredWhenEventCodeIsBDE() {
        var deliveredEvent = new TrackResponse.EventResponse(
                "BDE",
                "Objeto entregue ao destinatário",
                "Entregue",
                OffsetDateTime.now(),
                "São Paulo/SP",
                "São Paulo/SP"
        );
        var deliveredTrackResponse = new TrackResponse(
                "BR123456789", "Entregue", true, deliveredEvent, null
        );

        when(trackingClient.findTrack("BR123456789")).thenReturn(deliveredTrackResponse);

        var response = trackingService.registerNewTracking(requestDTO);

        assertThat(response.isDelivered()).isTrue();

        var savedTracking = trackingRepository.findAll().getFirst();
        assertThat(savedTracking.isDelivered()).isTrue();
    }

    @Test
    @DisplayName("Deve lançar exceção quando o código já existe no banco")
    void shouldThrowExceptionWhenCodeAlreadyExists() {
        when(trackingClient.findTrack("BR123456789")).thenReturn(trackResponse);

        // primeiro cadastro funciona
        trackingService.registerNewTracking(requestDTO);

        // segundo cadastro com o mesmo código deve falhar
        assertThatThrownBy(() -> trackingService.registerNewTracking(requestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Code already exists!");

        // garante que não duplicou no banco
        assertThat(trackingRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("Deve lançar exceção e não salvar nada quando a API externa retornar sucesso false")
    void shouldThrowExceptionAndSaveNothingWhenApiReturnsFalse() {
        var failedResponse = new TrackResponse(
                null, null, false, null, "Código não encontrado"
        );
        when(trackingClient.findTrack("BR123456789")).thenReturn(failedResponse);

        assertThatThrownBy(() -> trackingService.registerNewTracking(requestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Tracking code not found!");

        // nada deve ter sido persistido
        assertThat(trackingRepository.findAll()).isEmpty();
        assertThat(eventRepository.findAll()).isEmpty();
    }
}