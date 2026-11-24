package com.danieloliveira.tracking.scheduler;

import com.danieloliveira.tracking.client.TrackingClient;
import com.danieloliveira.tracking.client.dto.TrackResponse;
import com.danieloliveira.tracking.event.EventRepository;
import com.danieloliveira.tracking.tracking.Tracking;
import com.danieloliveira.tracking.tracking.TrackingRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SchedulerIntegrationTest {

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private TrackingRepository trackingRepository;

    @Autowired
    private EventRepository eventRepository;

    @MockitoBean
    private TrackingClient trackingClient;

    @MockitoBean
    private TrackingUpdateService trackingUpdateService;

    private static TrackResponse foundResponse(TrackResponse.EventResponse event) {
        var payload = new TrackResponse.Payload(
                "BR123456789",
                "2024-01-20",
                "Em trânsito",
                List.of(event)
        );
        return new TrackResponse("CARRIER_CORREIOS", "TRACKING_STATUS_FOUND", payload);
    }

    private static TrackResponse.EventResponse event(String code, String description) {
        return new TrackResponse.EventResponse(
                code,
                description,
                "Detalhes",
                new TrackResponse.EventDate("2024-01-15 10:00:00", "America/Sao_Paulo"),
                new TrackResponse.Unidade("UNIDADE", new TrackResponse.Endereco("São Paulo", "SP"))
        );
    }

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        trackingRepository.deleteAll();

        trackingRepository.save(Tracking.builder()
                .code("BR123456789")
                .email("teste@email.com")
                .delivered(false)
                .build());
    }

    @AfterEach
    void tearDown() {
        eventRepository.deleteAll();
        trackingRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve enviar todos os eventos encontrados para processamento")
    void shouldProcessAllProviderEvents() {
        var event = event("OEC", "Objeto em trânsito");
        when(trackingClient.findTrack("BR123456789")).thenReturn(foundResponse(event));

        scheduler.checkUpdates();

        verify(trackingUpdateService).processTrackingUpdate(
                argThat(tracking -> tracking.getCode().equals("BR123456789")),
                argThat(events -> events.size() == 1 && events.getFirst().codigo().equals("OEC"))
        );
    }

    @Test
    @DisplayName("Deve processar uma resposta que contém evento entregue")
    void shouldProcessDeliveredEvent() {
        var event = event("BDE", "Objeto entregue ao destinatário");
        when(trackingClient.findTrack("BR123456789")).thenReturn(foundResponse(event));

        scheduler.checkUpdates();

        verify(trackingUpdateService).processTrackingUpdate(
                argThat(tracking -> tracking.getCode().equals("BR123456789")),
                argThat(events -> events.getFirst().codigo().equals("BDE"))
        );
    }

    @Test
    @DisplayName("Não deve processar quando a resposta não contém rastreamento")
    void shouldNotProcessUpdateWhenTrackingIsNotFound() {
        when(trackingClient.findTrack("BR123456789")).thenReturn(
                new TrackResponse("CARRIER_CORREIOS", "TRACKING_STATUS_NOT_FOUND", null)
        );

        scheduler.checkUpdates();

        verify(trackingUpdateService, never()).processTrackingUpdate(any(), any());
    }

    @Test
    @DisplayName("Não deve processar quando o payload é nulo")
    void shouldNotProcessUpdateWhenPayloadIsMissing() {
        when(trackingClient.findTrack("BR123456789")).thenReturn(
                new TrackResponse("CARRIER_CORREIOS", "TRACKING_STATUS_FOUND", null)
        );

        scheduler.checkUpdates();

        verify(trackingUpdateService, never()).processTrackingUpdate(any(), any());
    }
}
