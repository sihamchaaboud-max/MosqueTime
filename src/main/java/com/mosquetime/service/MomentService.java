package com.mosquetime.service;

import com.mosquetime.model.Horaire;
import com.mosquetime.model.Priere;

import java.time.LocalTime;
import java.util.Optional;

public class MomentService {

    private final HoraireService horaireService = new HoraireService();

    public String momentActuel(LocalTime maintenant) {
        Optional<Horaire> horaire = horaireService.horaireDuJour();
        if (horaire.isEmpty()) {
            return "tout";
        }

        LocalTime fajr = horaire.get().getHeure(Priere.FAJR);
        LocalTime dhuhr = horaire.get().getHeure(Priere.DHUHR);
        LocalTime asr = horaire.get().getHeure(Priere.ASR);
        LocalTime isha = horaire.get().getHeure(Priere.ISHA);

        if (fajr == null || dhuhr == null || asr == null || isha == null) {
            return "tout";
        }

        if (!maintenant.isBefore(fajr) && maintenant.isBefore(dhuhr)) {
            return "matin";
        }
        if (!maintenant.isBefore(asr) && maintenant.isBefore(isha)) {
            return "soir";
        }
        if (!maintenant.isBefore(isha) || maintenant.isBefore(fajr)) {
            return "nuit";
        }
        return "tout";
    }
}