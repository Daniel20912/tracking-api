package com.danieloliveira.tracking.scheduler;

import com.danieloliveira.tracking.event.EventRepository;
import com.danieloliveira.tracking.event.EventService;
import com.danieloliveira.tracking.tracking.Tracking;
import com.danieloliveira.tracking.tracking.TrackingRepository;
import com.danieloliveira.tracking.client.dto.TrackResponse;
import com.danieloliveira.tracking.client.TrackingClient;
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
    @DisplayName("Deve enviar email e salvar novo evento quando há atualização")
    void shouldSendEmailWhenNewEventExists() {
        when(trackingClient.findTrack("BR123456789")).thenReturn(
                new TrackResponse("BR123456789", "Em trânsito", true,
                        new TrackResponse.EventResponse("OEC", "Objeto em trânsito",
                                "Em São Paulo", NEW_DATE, "São Paulo/SP", "São Paulo/SP"), null)
        );

        scheduler.checkUpdates();

        verify(javaMailSender).send(any(SimpleMailMessage.class));
        assertThat(eventRepository.findAll()).hasSize(2); // 1 do setUp + 1 novo
    }

    @Test
    @DisplayName("Não deve enviar email nem salvar evento quando não há atualização")
    void shouldNotSendEmailWhenEventIsTheSame() {
        when(trackingClient.findTrack("BR123456789")).thenReturn(
                new TrackResponse("BR123456789", "Em trânsito", true,
                        new TrackResponse.EventResponse("OEC", "Objeto em trânsito",
                                "De Curitiba para São Paulo", OLD_DATE, "Curitiba/PR", "São Paulo/SP"), null)
        );

        scheduler.checkUpdates();

        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
        assertThat(eventRepository.findAll()).hasSize(1); // nenhum novo evento salvo
    }

    @Test
    @DisplayName("Deve marcar como entregue, enviar email e salvar evento quando código é BDE")
    void shouldMarkAsDeliveredAndSendEmailWhenBDE() {
        when(trackingClient.findTrack("BR123456789")).thenReturn(
                new TrackResponse("BR123456789", "Entregue", true,
                        new TrackResponse.EventResponse("BDE", "Objeto entregue ao destinatário",
                                "Entregue", NEW_DATE, "São Paulo/SP", "São Paulo/SP"), null)
        );

        scheduler.checkUpdates();

        verify(javaMailSender).send(any(SimpleMailMessage.class));
        assertThat(eventRepository.findAll()).hasSize(2); // 1 do setUp + 1 novo
        assertThat(trackingRepository.findAll().getFirst().isDelivered()).isTrue();
    }

    @Test
    @DisplayName("Não deve enviar email nem salvar evento quando API externa falha")
    void shouldNotSendEmailWhenApiFails() {
        when(trackingClient.findTrack("BR123456789")).thenReturn(
                new TrackResponse(null, null, false, null, "Não encontrado")
        );

        scheduler.checkUpdates();

        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
        assertThat(eventRepository.findAll()).hasSize(1); // nenhum novo evento salvo
    }

    @Test
    @DisplayName("Deve enviar um email e salvar um evento para cada nova atualização do mesmo código")
    void shouldSendOneEmailPerNewEvent() {
        var secondEvent = new TrackResponse.EventResponse(
                "OEC", "Objeto em trânsito", "Em São Paulo", NEW_DATE, "São Paulo/SP", "São Paulo/SP"
        );
        var thirdEvent = new TrackResponse.EventResponse(
                "OEC", "Objeto em trânsito", "Em Campinas", NEW_DATE.plusDays(1), "Campinas/SP", "São Paulo/SP"
        );

        when(trackingClient.findTrack("BR123456789")).thenReturn(
                new TrackResponse("BR123456789", "Em trânsito", true, secondEvent, null)
        );
        scheduler.checkUpdates();

        when(trackingClient.findTrack("BR123456789")).thenReturn(
                new TrackResponse("BR123456789", "Em trânsito", true, thirdEvent, null)
        );
        scheduler.checkUpdates();

        verify(javaMailSender, times(2)).send(any(SimpleMailMessage.class));
        assertThat(eventRepository.findAll()).hasSize(3); // 1 do setUp + 2 novos
    }
}