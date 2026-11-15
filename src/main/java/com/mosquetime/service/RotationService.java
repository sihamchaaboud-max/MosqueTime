package com.mosquetime.service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.Timer;

import com.mosquetime.model.Invocation;
import com.mosquetime.model.Verset;
import com.mosquetime.repository.InvocationRepository;
import com.mosquetime.repository.VersetRepository;

public class RotationService {

    private static final int DELAI = 30_000;

    private final List<Verset> versets;
    private final List<Invocation> invocations;
    private final MomentService momentService = new MomentService();

    private int indexVerset = 0;
    private int indexInvocation = 0;

    private final Timer timerVerset;
    private final Timer timerInvocation;

    private final Consumer<Verset> afficherVerset;
    private final Consumer<Invocation> afficherInvocation;

    public RotationService(
            VersetRepository versetRepository,
            InvocationRepository invocationRepository,
            Consumer<Verset> afficherVerset,
            Consumer<Invocation> afficherInvocation) {

        this.afficherVerset = afficherVerset;
        this.afficherInvocation = afficherInvocation;

        this.versets = new ArrayList<>(versetRepository.getTous());
        this.invocations = new ArrayList<>(invocationRepository.getTous());

        this.timerVerset = new Timer(DELAI, e -> suivantVerset());
        this.timerInvocation = new Timer(DELAI, e -> suivantInvocation());
    }

    public void demarrer() {
        afficherVersetActuel();
        afficherInvocationActuelle();
        timerVerset.start();
        timerInvocation.start();
    }

    public void arreter() {
        timerVerset.stop();
        timerInvocation.stop();
    }

    public boolean estEnMarche() {
        return timerVerset.isRunning() && timerInvocation.isRunning();
    }

    private void suivantVerset() {
        if (versets.isEmpty()) {
            return;
        }
        indexVerset = (indexVerset + 1) % versets.size();
        afficherVersetActuel();
    }

    private void suivantInvocation() {
        List<Invocation> liste = invocationsDuMoment();
        if (liste.isEmpty()) {
            return;
        }
        indexInvocation = (indexInvocation + 1) % liste.size();
        afficherInvocation.accept(liste.get(indexInvocation));
    }

    private void afficherVersetActuel() {
        if (!versets.isEmpty()) {
            afficherVerset.accept(versets.get(indexVerset));
        }
    }

    private void afficherInvocationActuelle() {
        List<Invocation> liste = invocationsDuMoment();
        if (!liste.isEmpty()) {
            afficherInvocation.accept(liste.get(indexInvocation % liste.size()));
        }
    }

    private List<Invocation> invocationsDuMoment() {
        String moment = momentService.momentActuel(LocalTime.now());
        List<Invocation> liste = new ArrayList<>();

        for (Invocation i : invocations) {
            String m = i.getMoment();
            if (m == null || m.equals("tout") || m.equals(moment)) {
                liste.add(i);
            }
        }
        return liste.isEmpty() ? invocations : liste;
    }
}