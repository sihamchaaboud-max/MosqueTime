
package com.mosquetime.service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.Timer;

import com.mosquetime.model.Invocation;
import com.mosquetime.model.Verset;
import com.mosquetime.repository.InvocationRepository;
import com.mosquetime.repository.VersetRepository;

public class RotationService {

    // Durée de rotation : 30 secondes
    private static final int DELAI = 30_000;

    // Repositories
    private final VersetRepository versetRepository;
    private final InvocationRepository invocationRepository;

    // Listes des contenus
    private final List<Verset> versets;
    private final List<Invocation> invocations;

    // Indices indépendants
    private int indexVerset = 0;
    private int indexInvocation = 0;

    // Timers indépendants
    private Timer timerVerset;
    private Timer timerInvocation;

    // Fonctions de mise à jour de l'interface
    private final Consumer<Verset> afficherVerset;
    private final Consumer<Invocation> afficherInvocation;

    // Constructeur
    public RotationService(
            VersetRepository versetRepository,
            InvocationRepository invocationRepository,
            Consumer<Verset> afficherVerset,
            Consumer<Invocation> afficherInvocation) {

        this.versetRepository = versetRepository;
        this.invocationRepository = invocationRepository;

        this.afficherVerset = afficherVerset;
        this.afficherInvocation = afficherInvocation;

        this.versets = new ArrayList<>(
                versetRepository.getTous()
        );

        this.invocations = new ArrayList<>(
                invocationRepository.getTous()
        );

        initialiserTimers();
    }

    // Initialisation des deux timers
    private void initialiserTimers() {

        timerVerset = new Timer(DELAI, e -> suivantVerset());

        timerInvocation = new Timer(DELAI, e -> suivantInvocation());
    }

    // Démarrer les deux rotations
    public void demarrer() {

        // Afficher les premiers contenus immédiatement
        afficherVersetActuel();
        afficherInvocationActuelle();

        // Démarrer les timers
        timerVerset.start();
        timerInvocation.start();
    }

    // Passer au verset suivant
    private void suivantVerset() {

        if (versets.isEmpty()) {
            return;
        }

        indexVerset = (indexVerset + 1) % versets.size();

        afficherVersetActuel();
    }

    // Passer à l'invocation suivante
    private void suivantInvocation() {

        if (invocations.isEmpty()) {
            return;
        }

        indexInvocation =
                (indexInvocation + 1) % invocations.size();

        afficherInvocationActuelle();
    }

    // Afficher le verset actuel
    private void afficherVersetActuel() {

        if (!versets.isEmpty()) {
            afficherVerset.accept(versets.get(indexVerset));
        }
    }

    // Afficher l'invocation actuelle
    private void afficherInvocationActuelle() {

        if (!invocations.isEmpty()) {
            afficherInvocation.accept(
                    invocations.get(indexInvocation)
            );
        }
    }

    // Arrêter les deux rotations
    public void arreter() {

        timerVerset.stop();
        timerInvocation.stop();
    }

    // Vérifier si les rotations fonctionnent
    public boolean estEnMarche() {

        return timerVerset.isRunning()
                && timerInvocation.isRunning();
    }
}
