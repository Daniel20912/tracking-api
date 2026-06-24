package com.danieloliveira.tracking.email;

import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailTemplate {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private static final String SUBJECT = "Atualização da sua encomenda %s";

    private static final String HTML_TEMPLATE = """
            <html>
                <body>
                    <p>Olá!</p>
                    <p>Sua encomenda com o código %s foi atualizada.</p>
                    <p>
                        Status: %s<br>
                        Local: %s<br>
                        Data: %s
                    </p>
                    <p>Acompanhe sua encomenda pelo nosso site para ver o histórico completo de atualizações.</p>
                    <p>Você está recebendo este email pois cadastrou este código de rastreio em nosso sistema.</p>
                </body>
            </html>
            """;

    public String buildSubject(String code) {
        return String.format(SUBJECT, code);
    }

    public String buildHtml(String code, String description, String local, OffsetDateTime date) {
        return String.format(HTML_TEMPLATE, code, description, local, formatDate(date));
    }

    public String formatDate(OffsetDateTime date) {
        if (date == null)
            return "";

        return date.format(DATE_TIME_FORMATTER);
    }
}
