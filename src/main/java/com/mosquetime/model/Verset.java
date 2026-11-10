package com.mosquetime.model;

public class Verset {

    private int id;
    private int themeId;
    private String texteArabe;
    private String reference;

    public Verset() {
    }

    public Verset(int id, int themeId, String texteArabe, String reference) {
        this.id = id;
        this.themeId = themeId;
        this.texteArabe = texteArabe;
        this.reference = reference;
    }

    public int getId() {
        return id;
    }

    public int getThemeId() {
        return themeId;
    }

    public String getTexteArabe() {
        return texteArabe;
    }

    public String getReference() {
        return reference;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setThemeId(int themeId) {
        this.themeId = themeId;
    }

    public void setTexteArabe(String texteArabe) {
        this.texteArabe = texteArabe;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}