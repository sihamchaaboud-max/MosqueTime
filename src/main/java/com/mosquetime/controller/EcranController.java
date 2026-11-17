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

/**
 * Machine à états de l'écran : NORMAL -> ADHAN -> ATTENTE -> NORMAL.
 *
 *  - NORMAL  : l'heure d'une prière arrive      -> ADHAN
 *  - ADHAN   : le son est terminé (terminerAdhan) -> ATTENTE (compte à rebours 15 min)
 *              (sécurité : si le son ne finit pas, fin automatique après 180 s)
 *  - ATTENTE : le compte à rebours est fini      -> NORMAL
 */
public class EcranController {

    private static final int DUREE_ADHAN_SECONDES = 180;
    private static final int DUREE_ATTENTE_MINUTES = 15;

    private final HoraireService horaireService = new HoraireService();
    private final CountdownService countdown = new CountdownService();
    private final Timer timer;

    private Etat etat = Etat.NORMAL;
    private Priere priereCourante;
    private LocalDateTime finAdhan;
    private int dureeAttenteSecondes = DUREE_ATTENTE_MINUTES * 60;

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

    // Appelé chaque seconde (rafraîchir l'heure, le compteur...)
    public void setSurMiseAJour(Runnable surMiseAJour) {
        this.surMiseAJour = surMiseAJour;
    }

    // Appelé UNE fois à chaque changement d'état (changer d'écran, lancer le son...)
    public void setSurChangementEtat(Consumer<Etat> surChangementEtat) {
        this.surChangementEtat = surChangementEtat;
    }

    // ---------- Cycle automatique ----------

    private void tick() {
        switch (etat) {
            case NORMAL -> verifierAdhan();
            case ADHAN -> verifierFinAdhan();
            case ATTENTE -> verifierFinAttente();
        }
        notifierMiseAJour();
    }

    private void verifierAdhan() {
        Optional<Priere> priere = horaireService.priereAdhan(LocalTime.now());
        if (priere.isPresent()) {
            declencherAdhan(priere.get());
        }
    }

    private void verifierFinAdhan() {
        if (!LocalDateTime.now().isBefore(finAdhan)) {
            terminerAdhan();
        }
    }

    private void verifierFinAttente() {
        if (countdown.estTermine()) {
            retourNormal();
        }
    }

    // ---------- Transitions (aussi utilisables pour la démonstration) ----------

    // NORMAL -> ADHAN
    public void declencherAdhan(Priere priere) {
        countdown.arreter();
        priereCourante = priere;
        finAdhan = LocalDateTime.now().plusSeconds(DUREE_ADHAN_SECONDES);
        changerEtat(Etat.ADHAN);
    }

    // ADHAN -> ATTENTE (appelé à la fin du son de l'adhan)
    public void terminerAdhan() {
        if (etat != Etat.ADHAN) {
            return;
        }
        declencherAttente(DUREE_ATTENTE_MINUTES * 60);
    }

    // Démarre l'ATTENTE avec un compte à rebours en secondes
    public void declencherAttente(int secondes) {
        if (priereCourante == null) {
            priereCourante = Priere.DHUHR;
        }
        dureeAttenteSecondes = secondes;
        countdown.demarrerSecondes(secondes);
        changerEtat(Etat.ATTENTE);
    }

    // ATTENTE -> NORMAL
    public void retourNormal() {
        countdown.arreter();
        priereCourante = null;
        changerEtat(Etat.NORMAL);
    }

    private void changerEtat(Etat nouveau) {
        etat = nouveau;
        if (surChangementEtat != null) {
            surChangementEtat.accept(nouveau);
        }
        notifierMiseAJour();
    }

    private void notifierMiseAJour() {
        if (surMiseAJour != null) {
            surMiseAJour.run();
        }
    }

    // ---------- Lecture de l'état ----------

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

    public long getSecondesRestantes() {
        return countdown.secondesRestantes();
    }

    public int getDureeAttenteSecondes() {
        return dureeAttenteSecondes;
    }
}
