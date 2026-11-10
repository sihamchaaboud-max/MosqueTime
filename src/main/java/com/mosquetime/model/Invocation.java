package com.mosquetime.model;

public class Invocation {

    private int id;
    private String texteArabe;
    private String reference;

    public Invocation() {
    }

    public Invocation(int id, String texteArabe, String reference) {
        this.id = id;
        this.texteArabe = texteArabe;
        this.reference = reference;
    }

    public int getId() {
        return id;
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

    public void setTexteArabe(String texteArabe) {
        this.texteArabe = texteArabe;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}