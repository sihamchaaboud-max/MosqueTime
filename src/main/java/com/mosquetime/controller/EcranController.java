package com.mosquetime.controller;

import com.mosquetime.model.Priere;
import com.mosquetime.service.CountdownService;
import com.mosquetime.service.HoraireService;
import com.mosquetime.util.Etat;

import javax.swing.Timer;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.function.Consumer;

public class EcranController {

    private static final int DUREE_ADHAN_MIN_SECONDES = 30;
    private static final int DUREE_ADHAN_MAX_SECONDES = 300;
    private static final int DUREE_ATTENTE_MINUTES = 15;

    private final HoraireService horaireService = new HoraireService();
    private final CountdownService countdown = new CountdownService();
    private final Timer timer;

    private Etat etat = Etat.NORMAL;
    private Priere priereCourante;
    private LocalDateTime debutAdhan;
    private volatile boolean sonTermine;

    private Runnable surMiseAJour;
    private Consumer<Etat> surChangementEtat;

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

    public void setSurChangementEtat(Consumer<Etat> surChangementEtat) {
        this.surChangementEtat = surChangementEtat;
    }

    public void declencherAdhan(Priere priere) {
        if (etat == Etat.NORMAL) {
            demarrerAdhan(priere);
        }
    }

    public void terminerAdhan() {
        sonTermine = true;
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
            demarrerAdhan(priere.get());
        }
    }

    private void demarrerAdhan(Priere priere) {
        priereCourante = priere;
        debutAdhan = LocalDateTime.now();
        sonTermine = false;
        changerEtat(Etat.ADHAN);
    }

    private void verifierFinAdhan() {
        LocalDateTime maintenant = LocalDateTime.now();
        boolean minimumAtteint =
                !maintenant.isBefore(debutAdhan.plusSeconds(DUREE_ADHAN_MIN_SECONDES));
        boolean maximumAtteint =
                !maintenant.isBefore(debutAdhan.plusSeconds(DUREE_ADHAN_MAX_SECONDES));

        if (maximumAtteint || (sonTermine && minimumAtteint)) {
            countdown.demarrer(DUREE_ATTENTE_MINUTES);
            changerEtat(Etat.ATTENTE);
        }
    }

    private void verifierFinAttente() {
        if (countdown.estTermine()) {
            countdown.arreter();
            priereCourante = null;
            changerEtat(Etat.NORMAL);
        }
    }

    private void changerEtat(Etat nouveau) {
        etat = nouveau;
        if (surChangementEtat != null) {
            surChangementEtat.accept(nouveau);
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

    public long getSecondesRestantes() {
        return countdown.secondesRestantes();
    }

    public String getTexteCompteARebours() {
        return countdown.texteRestant();
    }
}