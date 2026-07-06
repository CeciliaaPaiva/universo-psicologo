package br.com.unipsi.plantao.domain;

import java.time.DayOfWeek;

public enum DiaSemana {
    SEG,
    TER,
    QUA,
    QUI,
    SEX,
    SAB,
    DOM;

    public static DiaSemana from(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> SEG;
            case TUESDAY -> TER;
            case WEDNESDAY -> QUA;
            case THURSDAY -> QUI;
            case FRIDAY -> SEX;
            case SATURDAY -> SAB;
            case SUNDAY -> DOM;
        };
    }
}
