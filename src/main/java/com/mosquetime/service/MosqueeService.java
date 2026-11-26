package com.mosquetime.service;

import com.mosquetime.model.Langue;
import com.mosquetime.model.Mosquee;
import com.mosquetime.repository.MosqueeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Liste des mosquées et mosquée affichée à l'écran.
 * Chaque mosquée a un nom en français (obligatoire) et, si on veut,
 * un nom en arabe et un nom en anglais : l'écran affiche celui de la langue choisie.
 * Les messages des exceptions sont des clés de traduction (I18n.t(message)).
 */
public class MosqueeService {

    private static final String NOM_PAR_DEFAUT = "MosqueTime";
    private static final int NOM_MAX = 80;

    private final MosqueeRepository repository;
    private final List<Mosquee> mosquees;

    public MosqueeService() {
        this(new MosqueeRepository());
    }

    // Constructeur avec repository fourni (utile pour les tests)
    public MosqueeService(MosqueeRepository repository) {
        this.repository = repository;
        this.mosquees = new ArrayList<>(repository.trouverToutes());
    }

    public synchronized List<Mosquee> getMosquees() {
        return new ArrayList<>(mosquees);
    }

    public synchronized Optional<Mosquee> mosqueeActive() {
        return mosquees.stream().filter(Mosquee::isActive).findFirst();
    }

    // Nom français de la mosquée affichée (compatibilité)
    public String nomMosqueeActive() {
        return nomMosqueeActive(Langue.FR);
    }

    // Nom de la mosquée affichée, dans la langue demandée (repli : nom français)
    public synchronized String nomMosqueeActive(Langue langue) {
        Optional<Mosquee> choisie = mosqueeActive();
        if (choisie.isPresent()) {
            return choisie.get().getNom(langue);
        }
        if (!mosquees.isEmpty() && mosquees.get(0).getNom() != null) {
            return mosquees.get(0).getNom(langue);
        }
        return NOM_PAR_DEFAUT;
    }

    public void ajouter(String nom, String ville) {
        ajouter(nom, null, null, ville);
    }

    // Ajoute une mosquée avec ses noms en français, arabe et anglais
    public synchronized void ajouter(String nomFr, String nomAr, String nomEn, String ville) {
        String fr = nettoyer(nomFr);
        String ar = nettoyer(nomAr);
        String en = nettoyer(nomEn);
        verifierNoms(fr, ar, en);

        boolean existe = mosquees.stream().anyMatch(m -> fr.equalsIgnoreCase(m.getNom()));
        if (existe) {
            throw new IllegalArgumentException("mosquees.erreur_existe");
        }

        Mosquee nouvelle = new Mosquee();
        nouvelle.setNom(fr);
        nouvelle.setNomAr(ar.isEmpty() ? null : ar);
        nouvelle.setNomEn(en.isEmpty() ? null : en);
        nouvelle.setVille(ville);
        nouvelle.setActive(mosquees.isEmpty());

        mosquees.add(nouvelle);
        repository.enregistrer(mosquees);
    }

    // Change les noms arabe et anglais d'une mosquée existante (identifiée par son nom français)
    public synchronized void traduire(String nomFr, String nomAr, String nomEn) {
        Mosquee mosquee = trouver(nomFr);
        String ar = nettoyer(nomAr);
        String en = nettoyer(nomEn);
        verifierNoms(nettoyer(nomFr), ar, en);

        mosquee.setNomAr(ar.isEmpty() ? null : ar);
        mosquee.setNomEn(en.isEmpty() ? null : en);
        repository.enregistrer(mosquees);
    }

    public synchronized void choisir(String nom) {
        trouver(nom);
        for (Mosquee m : mosquees) {
            m.setActive(nom.equals(m.getNom()));
        }
        repository.enregistrer(mosquees);
    }

    private Mosquee trouver(String nomFr) {
        return mosquees.stream()
                .filter(m -> nomFr != null && nomFr.equals(m.getNom()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("mosquees.erreur_introuvable"));
    }

    private static void verifierNoms(String fr, String ar, String en) {
        if (fr.isEmpty()) {
            throw new IllegalArgumentException("mosquees.erreur_nom");
        }
        if (fr.length() > NOM_MAX || ar.length() > NOM_MAX || en.length() > NOM_MAX) {
            throw new IllegalArgumentException("mosquees.erreur_longueur");
        }
    }

    private static String nettoyer(String texte) {
        return texte == null ? "" : texte.trim();
    }
}
