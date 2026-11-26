package com.mosquetime.model;

public class Mosquee {

    // nom = nom en français (nom par défaut) ; nomAr et nomEn sont facultatifs
    private String nom;
    private String nomAr;
    private String nomEn;
    private String adresse;
    private String ville;
    private String pays;
    private String informations;
    private boolean active;

    public Mosquee() {
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getNomAr() {
        return nomAr;
    }

    public void setNomAr(String nomAr) {
        this.nomAr = nomAr;
    }

    public String getNomEn() {
        return nomEn;
    }

    public void setNomEn(String nomEn) {
        this.nomEn = nomEn;
    }

    // Nom à afficher dans la langue choisie ; repli sur le nom français
    public String getNom(Langue langue) {
        String choisi = switch (langue) {
            case AR -> nomAr;
            case EN -> nomEn;
            default -> nom;
        };
        if (estRempli(choisi)) {
            return choisi.trim();
        }
        return estRempli(nom) ? nom.trim() : "MosqueTime";
    }

    private static boolean estRempli(String texte) {
        return texte != null && !texte.isBlank();
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getPays() {
        return pays;
    }

    public void setPays(String pays) {
        this.pays = pays;
    }

    public String getInformations() {
        return informations;
    }

    public void setInformations(String informations) {
        this.informations = informations;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}