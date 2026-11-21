# MosqueTime

Application desktop Java Swing qui affiche les horaires de prière sur l'écran d'une mosquée
(inspirée de tawkit.net) : date et heure, horaires des 6 prières, écran de l'adhan avec le son,
compte à rebours avant l'iqama, versets et invocations qui défilent, et trois langues
(français, arabe, anglais).

## Prérequis
- Java 17 ou plus
- Maven 3.9 ou plus

## Lancer
```
mvn clean compile
mvn compile org.codehaus.mojo:exec-maven-plugin:3.5.0:java "-Dexec.mainClass=com.mosquetime.Main"
```
Banc de test (affiche chaque écran sans attendre une prière) :
```
mvn compile org.codehaus.mojo:exec-maven-plugin:3.5.0:java "-Dexec.mainClass=com.mosquetime.TestEcran"
```
Touche Échap : quitter. La roue dentée en haut de l'écran ouvre le choix de la langue.

## Fonctionnement
`EcranController` pilote trois états : NORMAL, puis ADHAN (le son de l'adhan est joué),
puis ATTENTE (compte à rebours avant l'iqama), puis retour à NORMAL.
La durée de l'attente dépend de la prière (`iqama.json`).

## Données (`src/main/resources/data/`)
| Fichier | Contenu |
|---|---|
| `horaires.json` | heures de prière, une entrée par jour (format `HH:mm`) |
| `iqama.json` | délai avant l'iqama, en minutes, par prière |
| `versets.json`, `invocations.json`, `themes.json` | contenu du bandeau |
| `mosquee.json` | nom et informations de la mosquée |
| `parametres.json` | langue choisie par l'utilisateur |

Au premier lancement, chaque fichier est copié dans le dossier `data/` à la racine du projet,
et c'est cette copie qui est lue ensuite. Après avoir modifié un fichier de
`src/main/resources/data/`, supprimer le dossier `data/` pour que le changement soit pris en compte.

Les horaires de `horaires.json` sont des données de test jusqu'au 31 décembre 2026 :
les remplacer par le calendrier officiel de la ville. Sans ligne pour la date du jour,
l'écran affiche `--:--`.

## Son de l'adhan
`src/main/resources/audio/adhan.wav` doit être un vrai fichier WAV PCM
(un MP3 ou un MP4 renommé en `.wav` ne se lit pas).
Conversion : `ffmpeg -i adhan.mp4 -ac 1 -ar 22050 -acodec pcm_s16le adhan.wav`

## Structure
```
controller/  EcranController (machine à états)
model/       Priere, Horaire, Verset, Invocation, Theme, Mosquee, ConfigIqama, Langue, Parametre
repository/  lecture et écriture des fichiers JSON
service/     HoraireService, IqamaService, CountdownService, RotationService, MomentService
util/        I18n + Traductions (langues), SonAdhan, Style, Etat
view/        NormalPanel, AdhanPanel, AttentePanel, ContenuPanel, ParametrePanel
```
Prévu mais pas encore écrit : l'espace administrateur (connexion, gestion des horaires,
de la mosquée et des annonces).
