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
                entry("priere.ISHA", "Isha"),
                entry("mosquees.titre", "Mosquées"),
                entry("mosquees.active", "Mosquée affichée à l'écran : {0}"),
                entry("mosquees.nom_fr", "Nom (français) :"),
                entry("mosquees.nom_ar", "Nom (arabe) :"),
                entry("mosquees.nom_en", "Nom (anglais) :"),
                entry("mosquees.ville", "Ville :"),
                entry("mosquees.ajouter", "Ajouter"),
                entry("mosquees.traduire", "Enregistrer les noms traduits"),
                entry("mosquees.afficher", "Afficher la mosquée sélectionnée"),
                entry("mosquees.enregistre", "Enregistré"),
                entry("mosquees.erreur_nom", "Le nom en français est obligatoire"),
                entry("mosquees.erreur_longueur", "80 caractères au maximum par nom"),
                entry("mosquees.erreur_existe", "Cette mosquée existe déjà"),
                entry("mosquees.erreur_introuvable", "Mosquée introuvable"),
                entry("mosquees.erreur_selection", "Sélectionnez d'abord une mosquée dans la liste"),
                entry("mosquees.erreur_ecriture", "Enregistrement impossible")));

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
                entry("priere.ISHA", "Isha"),
                entry("mosquees.titre", "Mosques"),
                entry("mosquees.active", "Mosque shown on screen: {0}"),
                entry("mosquees.nom_fr", "Name (French):"),
                entry("mosquees.nom_ar", "Name (Arabic):"),
                entry("mosquees.nom_en", "Name (English):"),
                entry("mosquees.ville", "City:"),
                entry("mosquees.ajouter", "Add"),
                entry("mosquees.traduire", "Save translated names"),
                entry("mosquees.afficher", "Show the selected mosque"),
                entry("mosquees.enregistre", "Saved"),
                entry("mosquees.erreur_nom", "The French name is required"),
                entry("mosquees.erreur_longueur", "80 characters maximum per name"),
                entry("mosquees.erreur_existe", "This mosque already exists"),
                entry("mosquees.erreur_introuvable", "Mosque not found"),
                entry("mosquees.erreur_selection", "Select a mosque in the list first"),
                entry("mosquees.erreur_ecriture", "Could not save")));

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
                entry("priere.ISHA", "العشاء"),
                entry("mosquees.titre", "المساجد"),
                entry("mosquees.active", "المسجد المعروض على الشاشة : {0}"),
                entry("mosquees.nom_fr", "الاسم (بالفرنسية) :"),
                entry("mosquees.nom_ar", "الاسم (بالعربية) :"),
                entry("mosquees.nom_en", "الاسم (بالإنجليزية) :"),
                entry("mosquees.ville", "المدينة :"),
                entry("mosquees.ajouter", "إضافة"),
                entry("mosquees.traduire", "حفظ الأسماء المترجمة"),
                entry("mosquees.afficher", "عرض المسجد المحدد"),
                entry("mosquees.enregistre", "تم الحفظ"),
                entry("mosquees.erreur_nom", "الاسم بالفرنسية إلزامي"),
                entry("mosquees.erreur_longueur", "80 حرفا كحد أقصى لكل اسم"),
                entry("mosquees.erreur_existe", "هذا المسجد موجود بالفعل"),
                entry("mosquees.erreur_introuvable", "المسجد غير موجود"),
                entry("mosquees.erreur_selection", "اختر مسجدا من القائمة أولا"),
                entry("mosquees.erreur_ecriture", "تعذر الحفظ")));
    }

    private Traductions() {
    }

    static String get(Langue langue, String cle) {
        return TEXTES.get(langue).get(cle);
    }
}
