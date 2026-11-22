package com.mosquetime.util;

import java.time.LocalDate;
import java.time.chrono.HijrahDate;
import java.time.temporal.ChronoField;

public final class DateUtil {

    private static final String[] MOIS_HIJRI = {
            "Mouharram", "Safar", "Rabi' al-awwal", "Rabi' ath-thani",
            "Joumada al-oula", "Joumada ath-thania", "Rajab", "Cha'abane",
            "Ramadan", "Chawwal", "Dhou al-qi'da", "Dhou al-hijja"
    };

    private DateUtil() {
    }

    public static String dateHijri(LocalDate date, int decalageJours) {
        HijrahDate hijri = HijrahDate.from(date.plusDays(decalageJours));
        int jour = hijri.get(ChronoField.DAY_OF_MONTH);
        int mois = hijri.get(ChronoField.MONTH_OF_YEAR);
        int annee = hijri.get(ChronoField.YEAR_OF_ERA);
        return jour + " " + MOIS_HIJRI[mois - 1] + " " + annee + " H";
    }
}