package com.mosquetime.repository;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.mosquetime.model.Invocation;

public class InvocationRepository {

    private static final String FICHIER = "invocations.json";

    private List<Invocation> invocations;

    public InvocationRepository() {
        chargerInvocations();
    }

    private void chargerInvocations() {
        Type type = new TypeToken<List<Invocation>>() {}.getType();
        List<Invocation> lues = JsonManager.lire(FICHIER, type);
        invocations = (lues == null) ? new ArrayList<>() : lues;
    }

    public List<Invocation> getTous() {
        return invocations;
    }
}