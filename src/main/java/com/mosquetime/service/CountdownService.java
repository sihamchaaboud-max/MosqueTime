package com.mosquetime.service;

import java.time.Duration;
import java.time.LocalDateTime;

public class CountdownService {

    private LocalDateTime fin;

    public void demarrer(int minutes) {
        fin = LocalDateTime.now().plusMinutes(minutes);
    }

    public void arreter() {
        fin = null;
    }

    public boolean estActif() {
        return fin != null;
    }

    public long secondesRestantes() {
        if (fin == null) {
            return 0;
        }
        long secondes = Duration.between(LocalDateTime.now(), fin).getSeconds();
        return Math.max(secondes, 0);
    }

    public boolean estTermine() {
        return fin != null && secondesRestantes() == 0;
    }

    public String texteRestant() {
        long total = secondesRestantes();
        long minutes = total / 60;
        long secondes = total % 60;
        return String.format("%02d:%02d", minutes, secondes);
    }
}