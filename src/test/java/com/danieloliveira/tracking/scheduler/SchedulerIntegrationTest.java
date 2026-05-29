package com.danieloliveira.tracking.scheduler;

import com.danieloliveira.tracking.events.EventRepository;
import com.danieloliveira.tracking.events.EventService;
import com.danieloliveira.tracking.tracking.Tracking;
import com.danieloliveira.tracking.tracking.TrackingRepository;
import com.danieloliveira.tracking.trackingClient.TrackResponse;
import com.danieloliveira.tracking.trackingClient.TrackingClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SchedulerIntegrationTest {

    private static final OffsetDateTime OLD_DATE = OffsetDateTime.of(2024, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC);
    private static final OffsetDateTime NEW_DATE = OffsetDateTime.of(2024, 1, 2, 10, 0, 0, 0, ZoneOffset.UTC);
    @Autowired
    Scheduler scheduler;
    @Autowired
    private TrackingRepository trackingRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private EventService eventService;
    @MockitoBean
    private TrackingClient trackingClient;
    @MockitoBean
    private JavaMailSender javaMailSender;

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

        eventService.saveEvent(new TrackResponse.EventResponse(
                "OEC", "Objeto em trânsito", "De Curitiba para São Paulo",
                OLD_DATE, "Curitiba/PR", "São Paulo/SP"
        ), savedTracking);
    }

    @AfterEach
    void tearDown() {
        eventRepository.deleteAll();
        trackingRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve enviar email quando há novo evento")
    void shouldSendEmailWhenNewEventExists() {
        when(trackingClient.findTrack("BR123456789")).thenReturn(
                new TrackResponse("BR123456789", "Em trânsito", true,
                        new TrackResponse.EventResponse("OEC", "Objeto em trânsito",
                                "Em São Paulo", NEW_DATE, "São Paulo/SP", "São Paulo/SP"), null)
        );

        scheduler.checkUpdates();

        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Não deve enviar email quando o evento é o mesmo")
    void shouldNotSendEmailWhenEventIsTheSame() {
        when(trackingClient.findTrack("BR123456789")).thenReturn(
                new TrackResponse("BR123456789", "Em trânsito", true,
                        new TrackResponse.EventResponse("OEC", "Objeto em trânsito",
                                "De Curitiba para São Paulo", OLD_DATE, "Curitiba/PR", "São Paulo/SP"), null)
        );

        scheduler.checkUpdates();

        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Deve marcar como entregue e enviar email quando código é BDE")
    void shouldMarkAsDeliveredAndSendEmailWhenBDE() {
        when(trackingClient.findTrack("BR123456789")).thenReturn(
                new TrackResponse("BR123456789", "Entregue", true,
                        new TrackResponse.EventResponse("BDE", "Objeto entregue ao destinatário",
                                "Entregue", NEW_DATE, "São Paulo/SP", "São Paulo/SP"), null)
        );

        scheduler.checkUpdates();

        verify(javaMailSender).send(any(SimpleMailMessage.class));
        assertThat(trackingRepository.findAll().getFirst().isDelivered()).isTrue();
    }

    @Test
    @DisplayName("Não deve enviar email quando API externa falha")
    void shouldNotSendEmailWhenApiFails() {
        when(trackingClient.findTrack("BR123456789")).thenReturn(
                new TrackResponse(null, null, false, null, "Não encontrado")
        );

        scheduler.checkUpdates();

        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Deve enviar um email para cada tracking com novo evento")
    void shouldSendOneEmailPerTrackingWithNewEvent() {
        Tracking savedTracking2 = trackingRepository.save(
                Tracking.builder()
                        .code("BR987654321")
                        .email("outro@email.com")
                        .delivered(false)
                        .build()
        );

        eventService.saveEvent(new TrackResponse.EventResponse(
                "OEC", "Objeto em trânsito", "De Porto Alegre para Curitiba",
                OLD_DATE, "Porto Alegre/RS", "Curitiba/PR"
        ), savedTracking2);

        when(trackingClient.findTrack("BR123456789")).thenReturn(
                new TrackResponse("BR123456789", "Em trânsito", true,
                        new TrackResponse.EventResponse("OEC", "Objeto em trânsito",
                                "Em São Paulo", NEW_DATE, "São Paulo/SP", "São Paulo/SP"), null)
        );

        when(trackingClient.findTrack("BR987654321")).thenReturn(
                new TrackResponse("BR987654321", "Em trânsito", true,
                        new TrackResponse.EventResponse("OEC", "Objeto em trânsito",
                                "Em Curitiba", NEW_DATE, "Curitiba/PR", "São Paulo/SP"), null)
        );

        scheduler.checkUpdates();

        verify(javaMailSender, times(2)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Deve enviar um email para cada nova atualização do mesmo código")
    void shouldSendOneEmailPerNewEvent() {
        var secondEvent = new TrackResponse.EventResponse(
                "OEC", "Objeto em trânsito", "Em São Paulo", NEW_DATE, "São Paulo/SP", "São Paulo/SP"
        );
        var thirdEvent = new TrackResponse.EventResponse(
                "OEC", "Objeto em trânsito", "Em Campinas", NEW_DATE.plusDays(1), "Campinas/SP", "São Paulo/SP"
        );

        // primeira atualização
        when(trackingClient.findTrack("BR123456789")).thenReturn(
                new TrackResponse("BR123456789", "Em trânsito", true, secondEvent, null)
        );
        scheduler.checkUpdates();

        // segunda atualização
        when(trackingClient.findTrack("BR123456789")).thenReturn(
                new TrackResponse("BR123456789", "Em trânsito", true, thirdEvent, null)
        );
        scheduler.checkUpdates();

        verify(javaMailSender, times(2)).send(any(SimpleMailMessage.class));
    }
}