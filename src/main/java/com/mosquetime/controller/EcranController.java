package com.mosquetime.controller;

import com.mosquetime.model.Priere;
import com.mosquetime.service.CountdownService;
import com.mosquetime.service.HoraireService;
import com.mosquetime.util.Etat;

import javax.swing.Timer;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

public class EcranController {

    private static final int DUREE_ADHAN_SECONDES = 180;
    private static final int DUREE_ATTENTE_MINUTES = 15;

    private final HoraireService horaireService = new HoraireService();
    private final CountdownService countdown = new CountdownService();
    private final Timer timer;

    private Etat etat = Etat.NORMAL;
    private Priere priereCourante;
    private LocalDateTime finAdhan;
    private Runnable surMiseAJour;

    public EcranController() {
        timer = new Timer(1000, e -> tick());
    }

    public void demarrer() {
        timer.start();
    }

    public void arreter() {
        timer.stop();
    }

    public void setSurMiseAJour(Runnable surMiseAJour) {
        this.surMiseAJour = surMiseAJour;
    }

    private void tick() {
        switch (etat) {
            case NORMAL -> verifierAdhan();
            case ADHAN -> verifierFinAdhan();
            case ATTENTE -> verifierFinAttente();
        }
        if (surMiseAJour != null) {
            surMiseAJour.run();
        }
    }

    private void verifierAdhan() {
        Optional<Priere> priere = horaireService.priereAdhan(LocalTime.now());
        if (priere.isPresent()) {
            priereCourante = priere.get();
            finAdhan = LocalDateTime.now().plusSeconds(DUREE_ADHAN_SECONDES);
            etat = Etat.ADHAN;
        }
    }

    private void verifierFinAdhan() {
        if (!LocalDateTime.now().isBefore(finAdhan)) {
            countdown.demarrer(DUREE_ATTENTE_MINUTES);
            etat = Etat.ATTENTE;
        }
    }

    private void verifierFinAttente() {
        if (countdown.estTermine()) {
            countdown.arreter();
            priereCourante = null;
            etat = Etat.NORMAL;
        }
    }

    public Etat getEtat() {
        return etat;
    }

    public Priere getPriereCourante() {
        return priereCourante;
    }

    public Optional<Priere> getProchainePriere() {
        return horaireService.prochainePriere(LocalTime.now());
    }

    public String getTexteCompteARebours() {
        return countdown.texteRestant();
    }
}