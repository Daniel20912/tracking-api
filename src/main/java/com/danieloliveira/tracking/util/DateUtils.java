package com.danieloliveira.tracking.util;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class DateUtils {

    private static final ZoneId BRAZIL_ZONE = ZoneId.of("America/Sao_Paulo");

    private DateUtils() {
        // classe utilitária, não deve ser instanciada
    }

    public static OffsetDateTime toOffsetDateTime(LocalDateTime localDateTime) {
        return localDateTime.atZone(BRAZIL_ZONE).toOffsetDateTime();
    }
}
