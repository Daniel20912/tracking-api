package com.danieloliveira.tracking.email;

import org.springframework.stereotype.Service;

@Service
public class EmailTemplate {

    private static final String SUBJECT = "Atualização da sua encomenda %s";

    private static final String TEXT = """
            Olá!
            Sua encomenda com o código %s foi atualizada.
            Status: %s
            Local: %s
            Data: %s
            
            Acompanhe sua encomenda pelo nosso site para ver o histórico completo de atualizações.
            
            Você está recebendo este email pois cadastrou este código de rastreio em nosso sistema.""";

    public String buildSubject(String code) {
        return String.format(SUBJECT, code);
    }

    public String buildText(String code, String description, String local, String date) {
        return String.format(TEXT, code, description, local, date);
    }
}
