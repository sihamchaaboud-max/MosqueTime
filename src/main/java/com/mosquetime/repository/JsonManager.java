
package com.mosquetime.repository;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonManager {

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    // Lire un fichier JSON et convertir son contenu en objet Java
    public static <T> T lire(String chemin, Type type) {

        InputStream fichier = JsonManager.class.getResourceAsStream(chemin);

        if (fichier == null) {
            throw new IllegalArgumentException(
                    "Fichier JSON introuvable : " + chemin
            );
        }

        try (InputStreamReader reader =
                     new InputStreamReader(fichier, StandardCharsets.UTF_8)) {

            T resultat = gson.fromJson(reader, type);

            if (resultat == null) {
                throw new IllegalStateException(
                        "Le fichier JSON est vide : " + chemin
                );
            }

            return resultat;

        } catch (IOException e) {
            throw new RuntimeException(
                    "Erreur lors de la lecture du fichier : " + chemin, e
            );
        }
    }
}
