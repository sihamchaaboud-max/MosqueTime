package com.mosquetime.service;

import com.mosquetime.model.Apparence;
import com.mosquetime.repository.ApparenceRepository;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApparenceService {

    public static final String FOND_PAR_DEFAUT = "mosqimg.jpg";

    private static final Path DOSSIER_IMAGES = Paths.get("data", "images");
    private static final List<String> FONDS_INTEGRES = List.of(FOND_PAR_DEFAUT,
            "fond_emeraude.jpg", "fond_nuit.jpg", "fond_sable.jpg", "fond_bordeaux.jpg");

    private final ApparenceRepository repository = new ApparenceRepository();
    private Apparence apparence;

    public ApparenceService() {
        try {
            apparence = repository.lire();
        } catch (RuntimeException e) {
            System.err.println("apparence.json illisible : " + e.getMessage());
            apparence = new Apparence();
        }
    }

    public String getFond() {
        String fond = apparence.getFond();
        return fond == null ? FOND_PAR_DEFAUT : fond;
    }

    public List<String> fondsDisponibles() {
        List<String> noms = new ArrayList<>(FONDS_INTEGRES);
        File[] fichiers = DOSSIER_IMAGES.toFile().listFiles();
        if (fichiers != null) {
            Arrays.sort(fichiers);
            for (File f : fichiers) {
                if (estImage(f.getName()) && !noms.contains(f.getName())) {
                    noms.add(f.getName());
                }
            }
        }
        return noms;
    }

    public void choisirFond(String nom) {
        if (nom == null) {
            throw new IllegalArgumentException("Aucun fond choisi");
        }
        if (!nom.isEmpty() && !fondsDisponibles().contains(nom)) {
            throw new IllegalArgumentException("Image introuvable : " + nom);
        }
        apparence.setFond(nom);
        repository.enregistrer(apparence);
    }

    public String ajouterImage(File source) throws IOException {
        if (!estImage(source.getName())) {
            throw new IllegalArgumentException("Choisis une image .jpg ou .png");
        }
        Files.createDirectories(DOSSIER_IMAGES);
        Path destination = DOSSIER_IMAGES.resolve(source.getName());
        Files.copy(source.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
        return source.getName();
    }

    public Image chargerFond() {
        return chargerImage(getFond());
    }

    public Image chargerImage(String nom) {
        if (nom == null || nom.isEmpty()) {
            return null;
        }
        try {
            Path local = DOSSIER_IMAGES.resolve(nom);
            if (Files.exists(local)) {
                return ImageIO.read(local.toFile());
            }
            URL url = ApparenceService.class.getResource("/images/" + nom);
            if (url != null) {
                return ImageIO.read(url);
            }
        } catch (IOException e) {
            System.err.println("Image illisible : " + nom);
        }
        return null;
    }

    private static boolean estImage(String nomFichier) {
        String nom = nomFichier.toLowerCase();
        return nom.endsWith(".jpg") || nom.endsWith(".jpeg") || nom.endsWith(".png");
    }
}