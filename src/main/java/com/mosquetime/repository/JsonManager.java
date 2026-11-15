package com.mosquetime.repository;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.lang.reflect.Type;

public class JsonManager {

    private static final Path DOSSIER = Paths.get("data");

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDate.class,
                    (JsonSerializer<LocalDate>) (d, t, c) -> new JsonPrimitive(d.toString()))
            .registerTypeAdapter(LocalDate.class,
                    (JsonDeserializer<LocalDate>) (j, t, c) -> LocalDate.parse(j.getAsString()))
            .registerTypeAdapter(LocalTime.class,
                    (JsonSerializer<LocalTime>) (d, t, c) -> new JsonPrimitive(d.toString()))
            .registerTypeAdapter(LocalTime.class,
                    (JsonDeserializer<LocalTime>) (j, t, c) -> LocalTime.parse(j.getAsString()))
            .create();

    public static <T> T lire(String fichier, Class<T> classe) {
        try {
            return GSON.fromJson(contenu(fichier), classe);
        } catch (IOException e) {
            throw new RuntimeException("Lecture impossible : " + fichier, e);
        }
    }

    public static <T> T lire(String fichier, TypeToken<T> type) {
        try {
            return GSON.fromJson(contenu(fichier), type.getType());
        } catch (IOException e) {
            throw new RuntimeException("Lecture impossible : " + fichier, e);
        }
    }
    public static <T> T lire(String fichier, Type type) {
       try {
          return GSON.fromJson(contenu(fichier), type);
       } catch (IOException e) {
        throw new RuntimeException("Lecture impossible : " + fichier, e);
        }
    }

    public static void ecrire(String fichier, Object donnees) {
        try {
            Files.createDirectories(DOSSIER);
            Files.writeString(DOSSIER.resolve(fichier), GSON.toJson(donnees), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Ecriture impossible : " + fichier, e);
        }
    }

    private static String contenu(String fichier) throws IOException {
        Path local = DOSSIER.resolve(fichier);
        if (Files.exists(local)) {
            return Files.readString(local, StandardCharsets.UTF_8);
        }
        try (InputStream in = JsonManager.class.getResourceAsStream("/data/" + fichier)) {
            if (in == null) {
                throw new IOException("Ressource absente : /data/" + fichier);
            }
            String texte = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            Files.createDirectories(DOSSIER);
            Files.writeString(local, texte, StandardCharsets.UTF_8);
            return texte;
        }
    }
}