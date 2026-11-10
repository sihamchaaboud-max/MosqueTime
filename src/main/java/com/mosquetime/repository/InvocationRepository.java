
package com.mosquetime.repository;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.mosquetime.model.Invocation;

public class InvocationRepository {

    private static final String FICHIER =
            "/data/invocations.json";

    private List<Invocation> invocations;

    public InvocationRepository() {
        chargerInvocations();
    }

    // Charger toutes les invocations
    private void chargerInvocations() {

        Type type = new TypeToken<List<Invocation>>() {}.getType();

        invocations = JsonManager.lire(FICHIER, type);
    }

    // Récupérer toutes les invocations
    public List<Invocation> getTous() {
        return invocations;
    }
}
