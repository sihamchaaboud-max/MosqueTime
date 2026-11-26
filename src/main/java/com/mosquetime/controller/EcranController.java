package com.mosquetime.controller;

import com.mosquetime.model.Priere;
import com.mosquetime.service.CountdownService;
import com.mosquetime.service.HoraireService;
import com.mosquetime.service.IqamaService;
import com.mosquetime.util.Etat;

import javax.swing.Timer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Machine à états de l'écran : NORMAL -> ADHAN -> ATTENTE -> PRIERE -> NORMAL.
 *
 *  - NORMAL  : l'heure d'une prière arrive      -> ADHAN
 *  - ADHAN   : le son est terminé (terminerAdhan) -> ATTENTE (compte à rebours
 *              dont la durée dépend de la prière : voir iqama.json)
 *              (sécurité : si le son ne finit pas, fin automatique après 180 s)
 *  - ATTENTE : le compte à rebours de l'iqama est fini -> PRIERE
 *  - PRIERE  : écran « téléphone interdit » pendant la prière (5 minutes) -> NORMAL
 */
public class EcranController {

    // Message affiché quand une prière est proche : « il reste X min pour la prière Y »
    public record Alerte(Priere priere, long minutes) {}

    private static final int SEUIL_ALERTE_MINUTES = 5;
    private static final int DUREE_PRIERE_SECONDES = 5 * 60;

    // Sécurité : si le son ne signale jamais sa fin, l'adhan s'arrête après 10 minutes
    private static final int DUREE_ADHAN_SECONDES = 10 * 60;

    private final HoraireService horaireService;
    private final CountdownService countdown = new CountdownService();
    private final IqamaService iqamaService;
    private final Timer timer;

    private Etat etat = Etat.NORMAL;
    private Priere priereCourante;
    private LocalDateTime finAdhan;
    private int dureeAttenteSecondes = 15 * 60;
    private int dureePriereSecondes = DUREE_PRIERE_SECONDES;

    // Dernière prière déclenchée automatiquement (évite de la relancer dans la même minute)
    private Priere derniereAdhan;
    private LocalDate dateDerniereAdhan;

    private Runnable surMiseAJour;
    private Consumer<Etat> surChangementEtat;

    public EcranController() {
        this(new IqamaService());
    }

    public EcranController(IqamaService iqamaService) {
        this(iqamaService, new HoraireService());
    }

    public EcranController(IqamaService iqamaService, HoraireService horaireService) {
        this.iqamaService = iqamaService;
        this.horaireService = horaireService;
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
            case PRIERE -> verifierFinPriere();
        }
        notifierMiseAJour();
    }

    private void verifierAdhan() {
        Optional<Priere> priere = horaireService.priereAdhan(LocalTime.now());
        if (priere.isEmpty()) {
            return;
        }

        // Une même prière ne se déclenche qu'une fois par jour
        LocalDate aujourdhui = LocalDate.now();
        if (priere.get() == derniereAdhan && aujourdhui.equals(dateDerniereAdhan)) {
            return;
        }

        derniereAdhan = priere.get();
        dateDerniereAdhan = aujourdhui;
        declencherAdhan(priere.get());
    }

    private void verifierFinAdhan() {
        if (!LocalDateTime.now().isBefore(finAdhan)) {
            terminerAdhan();
        }
    }

    private void verifierFinAttente() {
        if (countdown.estTermine()) {
            declencherPriere();
        }
    }

    private void verifierFinPriere() {
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
    // La durée de l'attente est celle réglée pour cette prière (iqama.json)
    public void terminerAdhan() {
        if (etat != Etat.ADHAN) {
            return;
        }
        declencherAttente(iqamaService.getDelaiSecondes(priereCourante));
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

    // ATTENTE -> PRIERE : écran « téléphone interdit », pendant la durée de la prière
    // (aussi utilisable seul pour la démonstration, depuis l'état NORMAL)
    public void declencherPriere() {
        if (etat != Etat.NORMAL && etat != Etat.ATTENTE) {
            return;
        }
        countdown.demarrerSecondes(dureePriereSecondes);
        changerEtat(Etat.PRIERE);
    }

    // Durée de l'écran de prière (5 minutes par défaut) ; utile pour tester plus vite
    public void setDureePriereSecondes(int secondes) {
        this.dureePriereSecondes = secondes;
    }

    // PRIERE (ou ATTENTE) -> NORMAL
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

    // Une prière est dans moins de 5 minutes ? (uniquement à l'écran normal)
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
