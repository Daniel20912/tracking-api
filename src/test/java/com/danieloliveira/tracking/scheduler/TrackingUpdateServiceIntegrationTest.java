package com.danieloliveira.tracking.scheduler;

import com.danieloliveira.tracking.client.dto.TrackResponse;
import com.danieloliveira.tracking.email.EmailSender;
import com.danieloliveira.tracking.event.EventRepository;
import com.danieloliveira.tracking.exception.EmailSendException;
import com.danieloliveira.tracking.tracking.Tracking;
import com.danieloliveira.tracking.tracking.TrackingRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TrackingUpdateServiceIntegrationTest {

    private static final OffsetDateTime EVENT_DATE = OffsetDateTime.of(2024, 1, 15, 10, 0, 0, 0, ZoneOffset.UTC);
    @Autowired
    private TrackingUpdateService trackingUpdateService;
    @Autowired
    private TrackingRepository trackingRepository;
    @Autowired
    private EventRepository eventRepository;
    @MockitoBean
    private EmailSender emailSender;
    @MockitoBean
    private JavaMailSender javaMailSender;
    private Tracking savedTracking;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        trackingRepository.deleteAll();

        savedTracking = trackingRepository.save(
                Tracking.builder()
                        .code("BR123456789")
                        .email("teste@email.com")
                        .delivered(false)
                        .build()
        );
    }

    @AfterEach
    void tearDown() {
        eventRepository.deleteAll();
        trackingRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve salvar o novo evento no banco")
    void shouldSaveEventInDatabase() {
        var eventData = new TrackResponse.EventResponse(
                "OEC", "Objeto em trânsito", "Em trânsito", EVENT_DATE.toLocalDateTime(), "Curitiba/PR", "São Paulo/SP"
        );

        trackingUpdateService.processTrackingUpdate(savedTracking, eventData);

        assertThat(eventRepository.findAll()).hasSize(1);
        var savedEvent = eventRepository.findAll().getFirst();
        assertThat(savedEvent.getCode()).isEqualTo("OEC");
        assertThat(savedEvent.getDescription()).isEqualTo("Objeto em trânsito");
    }

    @Test
    @DisplayName("Deve marcar tracking como entregue quando código do evento é BDE")
    void shouldMarkTrackingAsDeliveredWhenBDE() {
        var bdeEvent = new TrackResponse.EventResponse(
                "BDE", "Objeto entregue ao destinatário", "Entregue", EVENT_DATE.toLocalDateTime(), "São Paulo/SP", null
        );

        trackingUpdateService.processTrackingUpdate(savedTracking, bdeEvent);

        assertThat(trackingRepository.findAll().getFirst().isDelivered()).isTrue();
    }

    @Test
    @DisplayName("Não deve marcar tracking como entregue quando código não é BDE")
    void shouldNotMarkTrackingAsDeliveredWhenNotBDE() {
        var eventData = new TrackResponse.EventResponse(
                "OEC", "Objeto em trânsito", "Em trânsito", EVENT_DATE.toLocalDateTime(), "Curitiba/PR", "São Paulo/SP"
        );

        trackingUpdateService.processTrackingUpdate(savedTracking, eventData);

        assertThat(trackingRepository.findAll().getFirst().isDelivered()).isFalse();
    }

    @Test
    @DisplayName("Deve enviar email após salvar o evento")
    void shouldSendEmailAfterSavingEvent() {
        var eventData = new TrackResponse.EventResponse(
                "OEC", "Objeto em trânsito", "Em trânsito", EVENT_DATE.toLocalDateTime(), "Curitiba/PR", "São Paulo/SP"
        );

        trackingUpdateService.processTrackingUpdate(savedTracking, eventData);

        verify(emailSender).sendEmail(
                argThat(t -> t.getCode().equals("BR123456789")),
                any(TrackResponse.EventResponse.class)
        );
    }

    @Test
    @DisplayName("Não deve lançar exceção quando envio de email falha, e deve salvar o evento mesmo assim")
    void shouldNotThrowExceptionWhenEmailFailsAndShouldStillSaveEvent() {
        var eventData = new TrackResponse.EventResponse(
                "OEC", "Objeto em trânsito", "Em trânsito", EVENT_DATE.toLocalDateTime(), "Curitiba/PR", "São Paulo/SP"
        );
        doThrow(new EmailSendException("Falha no envio"))
                .when(emailSender).sendEmail(any(), any());

        assertThatNoException().isThrownBy(() ->
                trackingUpdateService.processTrackingUpdate(savedTracking, eventData)
        );
        assertThat(eventRepository.findAll()).hasSize(1); // evento salvo mesmo com email falhando
    }
}
