package com.mosquetime.service;

import com.mosquetime.model.Mosquee;
import com.mosquetime.repository.MosqueeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MosqueeService {

    private static final String NOM_PAR_DEFAUT = "MosqueTime";

    private final MosqueeRepository repository = new MosqueeRepository();
    private final List<Mosquee> mosquees;

    public MosqueeService() {
        this.mosquees = repository.trouverToutes();
    }

    public List<Mosquee> getMosquees() {
        return new ArrayList<>(mosquees);
    }

    public Optional<Mosquee> mosqueeActive() {
        return mosquees.stream().filter(Mosquee::isActive).findFirst();
    }

    public String nomMosqueeActive() {
        Optional<Mosquee> choisie = mosqueeActive();
        if (choisie.isPresent()) {
            return choisie.get().getNom();
        }
        if (!mosquees.isEmpty() && mosquees.get(0).getNom() != null) {
            return mosquees.get(0).getNom();
        }
        return NOM_PAR_DEFAUT;
    }

    public void ajouter(String nom, String ville) {
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("Le nom est obligatoire");
        }
        String nomPropre = nom.trim();
        boolean existe = mosquees.stream()
                .anyMatch(m -> nomPropre.equalsIgnoreCase(m.getNom()));
        if (existe) {
            throw new IllegalArgumentException("Cette mosquée existe déjà");
        }

        Mosquee nouvelle = new Mosquee();
        nouvelle.setNom(nomPropre);
        nouvelle.setVille(ville);
        nouvelle.setActive(mosquees.isEmpty());

        mosquees.add(nouvelle);
        repository.enregistrer(mosquees);
    }

    public void choisir(String nom) {
        boolean existe = mosquees.stream().anyMatch(m -> nom.equals(m.getNom()));
        if (!existe) {
            throw new IllegalArgumentException("Mosquée introuvable : " + nom);
        }
        for (Mosquee m : mosquees) {
            m.setActive(nom.equals(m.getNom()));
        }
        repository.enregistrer(mosquees);
    }
}