package com.mosquetime.controller;

import com.mosquetime.model.Priere;
import com.mosquetime.service.CountdownService;
import com.mosquetime.service.HoraireService;
import com.mosquetime.service.IqamaService;
import com.mosquetime.util.Etat;

import javax.swing.Timer;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.function.Consumer;

public class EcranController {

    public record Alerte(Priere priere, long minutes) {}

    private static final int SEUIL_ALERTE_MINUTES = 5;
    private static final int DUREE_ADHAN_MIN_SECONDES = 30;
    private static final int DUREE_ADHAN_MAX_SECONDES = 300;
    private static final int DUREE_PRIERE_MINUTES = 5;

    private final HoraireService horaireService = new HoraireService();
    private final CountdownService countdown = new CountdownService();
    private final IqamaService iqamaService = new IqamaService();
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

    public void declencherPriere() {
        if (etat == Etat.NORMAL) {
            countdown.demarrer(DUREE_PRIERE_MINUTES);
            changerEtat(Etat.PRIERE);
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
            case PRIERE -> verifierFinPriere();
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
            countdown.demarrer(iqamaService.getDureeMinutes(priereCourante));
            changerEtat(Etat.ATTENTE);
        }
    }

    private void verifierFinAttente() {
        if (countdown.estTermine()) {
            countdown.demarrer(DUREE_PRIERE_MINUTES);
            changerEtat(Etat.PRIERE);
        }
    }

    private void verifierFinPriere() {
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

    public Optional<Alerte> getAlerte() {
        if (etat != Etat.NORMAL) {
            return Optional.empty();
        }
        LocalTime maintenant = LocalTime.now();
        Optional<Priere> prochaine = horaireService.prochaineAdhan(maintenant);
        if (prochaine.isEmpty()) {
            return Optional.empty();
        }
        long minutes = horaireService.minutesAvant(prochaine.get(), maintenant);
        if (minutes >= 1 && minutes <= SEUIL_ALERTE_MINUTES) {
            return Optional.of(new Alerte(prochaine.get(), minutes));
        }
        return Optional.empty();
    }

    public long getSecondesRestantes() {
        return countdown.secondesRestantes();
    }

    public String getTexteCompteARebours() {
        return countdown.texteRestant();
    }
}