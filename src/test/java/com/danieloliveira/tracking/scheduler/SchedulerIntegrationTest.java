package com.danieloliveira.tracking.scheduler;

import com.danieloliveira.tracking.client.TrackingClient;
import com.danieloliveira.tracking.client.dto.TrackResponse;
import com.danieloliveira.tracking.email.EmailSender;
import com.danieloliveira.tracking.event.Event;
import com.danieloliveira.tracking.event.EventRepository;
import com.danieloliveira.tracking.tracking.Tracking;
import com.danieloliveira.tracking.tracking.TrackingRepository;
import com.danieloliveira.tracking.util.DateUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SchedulerIntegrationTest {

    // Gerando a data usando o MESMO conversor que o Scheduler usa
    private static final OffsetDateTime OLD_DATE = DateUtils.toOffsetDateTime(LocalDateTime.of(2024, 1, 1, 10, 0, 0));
    private static final OffsetDateTime NEW_DATE = DateUtils.toOffsetDateTime(LocalDateTime.of(2024, 1, 2, 10, 0, 0));
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
    @MockitoBean
    private EmailSender emailSender;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        trackingRepository.deleteAll();

        Tracking savedTracking = trackingRepository.save(
                Tracking.builder()
                        .code("BR123456789")
                        .email("teste@email.com")
                        .delivered(false)
                        .build()
        );

        eventRepository.save(Event.builder()
                .code("OEC")
                .description("Objeto em trânsito")
                .details("De Curitiba para São Paulo")
                .dateEvent(OLD_DATE)
                .location("Curitiba/PR")
                .destination("São Paulo/SP")
                .tracking(savedTracking)
                .build());
    }

    @AfterEach
    void tearDown() {
        eventRepository.deleteAll();
        trackingRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve acionar processamento quando há novo evento")
    void shouldProcessUpdateWhenNewEventExists() {
        when(trackingClient.findTrack("BR123456789")).thenReturn(
                new TrackResponse("BR123456789", "Em trânsito", true,
                        new TrackResponse.EventResponse("OEC", "Objeto em trânsito",
                                "Em São Paulo", NEW_DATE.toLocalDateTime(), "São Paulo/SP", "São Paulo/SP"), null)
        );

        scheduler.checkUpdates();

        verify(trackingUpdateService).processTrackingUpdate(
                argThat(t -> t.getCode().equals("BR123456789")),
                any(TrackResponse.EventResponse.class)
        );
    }

    @Test
    @DisplayName("Não deve acionar processamento quando o evento é o mesmo")
    void shouldNotProcessUpdateWhenEventIsTheSame() {
        when(trackingClient.findTrack("BR123456789")).thenReturn(
                new TrackResponse("BR123456789", "Em trânsito", true,
                        new TrackResponse.EventResponse("OEC", "Objeto em trânsito",
                                "De Curitiba para São Paulo", OLD_DATE.toLocalDateTime(), "Curitiba/PR", "São Paulo/SP"), null)
        );

        scheduler.checkUpdates();

        verify(trackingUpdateService, never()).processTrackingUpdate(any(), any());
    }

    @Test
    @DisplayName("Deve acionar processamento quando código do evento é BDE")
    void shouldProcessUpdateWhenEventCodeIsBDE() {
        when(trackingClient.findTrack("BR123456789")).thenReturn(
                new TrackResponse("BR123456789", "Entregue", true,
                        new TrackResponse.EventResponse("BDE", "Objeto entregue ao destinatário",
                                "Entregue", NEW_DATE.toLocalDateTime(), "São Paulo/SP", null), null)
        );

        scheduler.checkUpdates();

        verify(trackingUpdateService).processTrackingUpdate(
                argThat(t -> t.getCode().equals("BR123456789")),
                argThat(e -> e.codigo().equals("BDE"))
        );
    }

    @Test
    @DisplayName("Não deve acionar processamento quando API externa falha")
    void shouldNotProcessUpdateWhenApiFails() {
        when(trackingClient.findTrack("BR123456789")).thenReturn(
                new TrackResponse(null, null, false, null, "Não encontrado")
        );

        scheduler.checkUpdates();

        verify(trackingUpdateService, never()).processTrackingUpdate(any(), any());
    }

    @Test
    @DisplayName("Deve acionar processamento quando não há eventos anteriores")
    void shouldProcessUpdateWhenNoEventsExist() {
        eventRepository.deleteAll();

        when(trackingClient.findTrack("BR123456789")).thenReturn(
                new TrackResponse("BR123456789", "Em trânsito", true,
                        new TrackResponse.EventResponse("OEC", "Objeto em trânsito",
                                "Em São Paulo", NEW_DATE.toLocalDateTime(), "São Paulo/SP", "São Paulo/SP"), null)
        );

        scheduler.checkUpdates();

        verify(trackingUpdateService).processTrackingUpdate(
                argThat(t -> t.getCode().equals("BR123456789")),
                any(TrackResponse.EventResponse.class)
        );
    }

    @Test
    @DisplayName("Deve acionar processamento uma vez para cada novo evento")
    void shouldProcessUpdateForEachNewEvent() {
        when(trackingClient.findTrack("BR123456789")).thenReturn(new TrackResponse("BR123456789", "Em trânsito", true,
                new TrackResponse.EventResponse("OEC", "Em trânsito", "Em São Paulo", NEW_DATE.toLocalDateTime(), "São Paulo/SP", null), null)
        );
        scheduler.checkUpdates();

        when(trackingClient.findTrack("BR123456789")).thenReturn(new TrackResponse("BR123456789", "Em trânsito", true,
                new TrackResponse.EventResponse("OEC", "Em trânsito", "Em Campinas", NEW_DATE.plusDays(1).toLocalDateTime(), "Campinas/SP", null), null)
        );
        scheduler.checkUpdates();

        verify(trackingUpdateService, times(2)).processTrackingUpdate(
                argThat(t -> t.getCode().equals("BR123456789")),
                any(TrackResponse.EventResponse.class)
        );
    }
}
