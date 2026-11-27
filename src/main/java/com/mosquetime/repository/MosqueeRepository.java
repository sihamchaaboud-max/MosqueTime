package com.mosquetime.repository;

import com.google.gson.reflect.TypeToken;
import com.mosquetime.model.Mosquee;

import java.util.ArrayList;
import java.util.List;

public class MosqueeRepository {

    private static final String FICHIER = "mosquees.json";

    private static final TypeToken<List<Mosquee>> TYPE =
            new TypeToken<>() {};

    public List<Mosquee> trouverToutes() {
        List<Mosquee> liste = JsonManager.lire(FICHIER, TYPE);
        if (liste == null) {
            liste = new ArrayList<>();
        }
        completerTraductions(liste);
        return liste;
    }

    public void enregistrer(List<Mosquee> mosquees) {
        JsonManager.ecrire(FICHIER, mosquees);
    }

    // La copie locale (dossier data/) peut être ancienne et ne pas avoir les noms
    // arabe et anglais. Pour chaque mosquée sans traduction, on reprend celle du fichier
    // livré avec l'application (même nom français) : le nom suit alors toujours la langue.
    private void completerTraductions(List<Mosquee> liste) {
        List<Mosquee> modele = JsonManager.lireRessource(FICHIER, TYPE);
        if (modele == null) {
            return;
        }

        boolean modifie = false;
        for (Mosquee m : liste) {
            for (Mosquee ref : modele) {
                if (m.getNom() == null || !m.getNom().equalsIgnoreCase(ref.getNom())) {
                    continue;
                }
                if (estVide(m.getNomAr()) && !estVide(ref.getNomAr())) {
                    m.setNomAr(ref.getNomAr());
                    modifie = true;
                }
                if (estVide(m.getNomEn()) && !estVide(ref.getNomEn())) {
                    m.setNomEn(ref.getNomEn());
                    modifie = true;
                }
            }
        }

        if (modifie) {
            enregistrer(liste);
        }
    }

    private static boolean estVide(String texte) {
        return texte == null || texte.isBlank();
    }
}
