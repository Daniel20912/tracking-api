package com.danieloliveira.tracking.scheduler;

import com.danieloliveira.tracking.client.dto.TrackResponse;
import com.danieloliveira.tracking.email.BrevoClient;
import com.danieloliveira.tracking.email.EmailSender;
import com.danieloliveira.tracking.event.Event;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TrackingUpdateServiceIntegrationTest {

    @Autowired
    private TrackingUpdateService trackingUpdateService;
    @Autowired
    private TrackingRepository trackingRepository;
    @Autowired
    private EventRepository eventRepository;
    @MockitoBean
    private EmailSender emailSender;
    @MockitoBean
    private BrevoClient brevoClient;
    private Tracking savedTracking;

    private static TrackResponse.EventResponse event(String code, String description, String details) {
        return new TrackResponse.EventResponse(
                code,
                description,
                details,
                new TrackResponse.EventDate("2024-01-15 10:00:00", "America/Sao_Paulo"),
                new TrackResponse.Unidade("UNIDADE", new TrackResponse.Endereco("Curitiba", "PR"))
        );
    }

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
        var eventData = event("OEC", "Objeto em trânsito", "Em trânsito");

        trackingUpdateService.processTrackingUpdate(savedTracking, List.of(eventData));

        assertThat(eventRepository.findAll()).hasSize(1);
        var savedEvent = eventRepository.findAll().getFirst();
        assertThat(savedEvent.getCode()).isEqualTo("OEC");
        assertThat(savedEvent.getDescription()).isEqualTo("Objeto em trânsito");
    }

    @Test
    @DisplayName("Deve marcar tracking como entregue quando código do evento é BDE")
    void shouldMarkTrackingAsDeliveredWhenBDE() {
        var bdeEvent = event("BDE", "Objeto entregue ao destinatário", "Entregue");

        trackingUpdateService.processTrackingUpdate(savedTracking, List.of(bdeEvent));

        assertThat(trackingRepository.findAll().getFirst().isDelivered()).isTrue();
    }

    @Test
    @DisplayName("Não deve marcar tracking como entregue quando código não é BDE")
    void shouldNotMarkTrackingAsDeliveredWhenNotBDE() {
        var eventData = event("OEC", "Objeto em trânsito", "Em trânsito");

        trackingUpdateService.processTrackingUpdate(savedTracking, List.of(eventData));

        assertThat(trackingRepository.findAll().getFirst().isDelivered()).isFalse();
    }

    @Test
    @DisplayName("Deve enviar email após salvar o evento")
    void shouldSendEmailAfterSavingEvent() {
        var eventData = event("OEC", "Objeto em trânsito", "Em trânsito");

        trackingUpdateService.processTrackingUpdate(savedTracking, List.of(eventData));

        verify(emailSender).sendEmail(
                argThat(t -> t.getCode().equals("BR123456789")),
                any(Event.class)
        );
    }

    @Test
    @DisplayName("Não deve lançar exceção quando envio de email falha, e deve salvar o evento mesmo assim")
    void shouldNotThrowExceptionWhenEmailFailsAndShouldStillSaveEvent() {
        var eventData = event("OEC", "Objeto em trânsito", "Em trânsito");
        doThrow(new EmailSendException("Falha no envio"))
                .when(emailSender).sendEmail(any(), any());

        assertThatNoException().isThrownBy(() ->
                trackingUpdateService.processTrackingUpdate(savedTracking, List.of(eventData))
        );
        assertThat(eventRepository.findAll()).hasSize(1); // evento salvo mesmo com email falhando
    }
}
