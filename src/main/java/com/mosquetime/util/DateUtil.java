package com.mosquetime.util;

import java.time.LocalDate;
import java.time.chrono.HijrahDate;
import java.time.temporal.ChronoField;

/** Date hijri (calendrier Oumm al-Qora), écrite dans la langue choisie. */
public final class DateUtil {

    private DateUtil() {
    }

    // decalageJours : correction (-1, 0 ou +1) si la date officielle du Maroc diffère
    public static String dateHijri(LocalDate date, int decalageJours) {
        HijrahDate hijri = HijrahDate.from(date.plusDays(decalageJours));
        int jour = hijri.get(ChronoField.DAY_OF_MONTH);
        int mois = hijri.get(ChronoField.MONTH_OF_YEAR);
        int annee = hijri.get(ChronoField.YEAR_OF_ERA);
        return I18n.t("hijri.format", jour, I18n.t("hijri.mois." + mois), annee);
    }
}
