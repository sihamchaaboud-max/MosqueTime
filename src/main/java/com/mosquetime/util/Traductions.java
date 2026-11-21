package com.mosquetime.util;

import static java.util.Map.entry;

import java.util.EnumMap;
import java.util.Map;

import com.mosquetime.model.Langue;

/**
 * Tous les textes de l'application, dans les trois langues.
 * Pour ajouter un texte : ajouter la même clé dans FR, EN et AR,
 * puis écrire I18n.t("ma.cle") dans le code.
 */
final class Traductions {

    private static final Map<Langue, Map<String, String>> TEXTES = new EnumMap<>(Langue.class);

    static {
        TEXTES.put(Langue.FR, Map.ofEntries(
                entry("parametres.titre", "Paramètres"),
                entry("parametres.langue", "Langue"),
                entry("parametres.fermer", "Fermer"),
                entry("normal.prochaine", "Prochaine prière : {0} {1}"),
                entry("normal.aucune", "Plus de prière aujourd'hui"),
                entry("adhan.titre", "C'est l'heure de l'adhan"),
                entry("adhan.sous_titre", "Préparez-vous à prier"),
                entry("attente.titre", "Iqama dans"),
                entry("attente.consigne", "Veuillez rejoindre les rangs"),
                entry("priere.FAJR", "Fajr"),
                entry("priere.CHOROUK", "Chourouq"),
                entry("priere.DHUHR", "Dhuhr"),
                entry("priere.ASR", "Asr"),
                entry("priere.MAGHRIB", "Maghrib"),
                entry("priere.ISHA", "Isha")));

        TEXTES.put(Langue.EN, Map.ofEntries(
                entry("parametres.titre", "Settings"),
                entry("parametres.langue", "Language"),
                entry("parametres.fermer", "Close"),
                entry("normal.prochaine", "Next prayer: {0} {1}"),
                entry("normal.aucune", "No more prayers today"),
                entry("adhan.titre", "It is time for the adhan"),
                entry("adhan.sous_titre", "Get ready to pray"),
                entry("attente.titre", "Iqama in"),
                entry("attente.consigne", "Please join the rows"),
                entry("priere.FAJR", "Fajr"),
                entry("priere.CHOROUK", "Sunrise"),
                entry("priere.DHUHR", "Dhuhr"),
                entry("priere.ASR", "Asr"),
                entry("priere.MAGHRIB", "Maghrib"),
                entry("priere.ISHA", "Isha")));

        TEXTES.put(Langue.AR, Map.ofEntries(
                entry("parametres.titre", "الإعدادات"),
                entry("parametres.langue", "اللغة"),
                entry("parametres.fermer", "إغلاق"),
                entry("normal.prochaine", "الصلاة القادمة : {0} {1}"),
                entry("normal.aucune", "لا توجد صلوات أخرى اليوم"),
                entry("adhan.titre", "حان الآن وقت الأذان"),
                entry("adhan.sous_titre", "استعدوا للصلاة"),
                entry("attente.titre", "الإقامة بعد"),
                entry("attente.consigne", "يرجى الالتحاق بالصفوف"),
                entry("priere.FAJR", "الفجر"),
                entry("priere.CHOROUK", "الشروق"),
                entry("priere.DHUHR", "الظهر"),
                entry("priere.ASR", "العصر"),
                entry("priere.MAGHRIB", "المغرب"),
                entry("priere.ISHA", "العشاء")));
    }

    private Traductions() {
    }

    static String get(Langue langue, String cle) {
        return TEXTES.get(langue).get(cle);
    }
}
