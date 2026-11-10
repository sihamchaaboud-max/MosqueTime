package com.mosquetime.util;

import java.io.BufferedInputStream;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.swing.SwingUtilities;

/**
 * Lecture du son de l'adhan.
 * Le fichier doit être un WAV (PCM) placé dans src/main/resources/audio/adhan.wav
 * (Java Sound ne lit pas le MP3 sans bibliothèque externe).
 */
public class SonAdhan {

    private static final String FICHIER = "/audio/adhan.wav";

    private Clip clip;
    private Runnable quandTermine;

    /**
     * Joue l'adhan. Le Runnable est exécuté dans le thread Swing (EDT)
     * quand le son est terminé, ou immédiatement si la lecture est impossible
     * (fichier absent, format non supporté) afin de ne jamais bloquer la
     * machine à états ADHAN -> ATTENTE.
     */
    public synchronized void jouer(Runnable quandTermine) {

        arreter();

        this.quandTermine = quandTermine;

        try (InputStream brut = SonAdhan.class.getResourceAsStream(FICHIER)) {

            if (brut == null) {
                throw new IllegalStateException("Fichier audio introuvable : " + FICHIER);
            }

            try (AudioInputStream audio =
                         AudioSystem.getAudioInputStream(new BufferedInputStream(brut))) {

                final Clip nouveau = AudioSystem.getClip();
                clip = nouveau;

                // Chaque clip écoute uniquement ses propres événements :
                // un ancien clip arrêté ne peut pas déclencher la fin du nouveau
                nouveau.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        finDeLecture(nouveau);
                    }
                });

                nouveau.open(audio);
                nouveau.start();
            }

        } catch (Exception e) {
            System.err.println("SonAdhan : lecture impossible (" + e.getMessage() + ")");
            fermerClip();
            notifierFin();
        }
    }

    // Arrêter l'adhan (sans déclencher le callback de fin)
    public synchronized void arreter() {

        quandTermine = null;
        fermerClip();
    }

    public synchronized boolean estEnLecture() {
        return clip != null && clip.isRunning();
    }

    // Appelé quand un clip s'arrête (ignoré si le clip a été remplacé ou arrêté à la main)
    private synchronized void finDeLecture(Clip source) {

        if (source != clip) {
            return;
        }

        fermerClip();
        notifierFin();
    }

    private void notifierFin() {

        Runnable action = quandTermine;
        quandTermine = null;

        if (action != null) {
            SwingUtilities.invokeLater(action);
        }
    }

    private void fermerClip() {

        if (clip != null) {
            Clip ancien = clip;
            clip = null;
            ancien.stop();
            ancien.close();
        }
    }
}
