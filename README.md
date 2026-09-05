# IPTV Player Mobile — version de test sur téléphone

Version Android téléphone (portrait/paysage), pensée pour tester le concept
avant de revenir sur la version TV. Réutilise le cœur déjà construit pour la
TV (parseur M3U/Xtream Codes, modèles, stockage local).

## Ce qui est fait, en fonction de tes derniers ajustements

- **Qualité maximale forcée** : le lecteur (ExoPlayer/Media3) utilise
  `forceHighestSupportedBitrate` — il ne descend jamais la qualité pour
  fluidifier. Le buffer est aussi plus généreux (15 à 50 secondes) pour
  privilégier la stabilité de l'image plutôt que la réactivité.
- **Onglet Vitesse VPN** : test de débit **uniquement à la demande**, via le
  bouton "Tester maintenant". Aucune vérification en arrière-plan, aucune
  notification automatique. Le test télécharge un fichier depuis un serveur
  public (Cloudflare) et mesure le débit réel obtenu à cet instant — donc à
  travers Proton VPN si tu es connecté.
- **Aucune alerte n'est bloquante ou permanente** : toute alerte (erreur de
  lecture, résultat du test de débit) s'affiche dans un bandeau fermable —
  au tap sur la croix, ou avec le bouton retour du téléphone.
- **Pont Proton VPN** : bouton "Ouvrir Proton VPN" dans Réglages et dans
  l'onglet Vitesse — amène l'appli Proton VPN au premier plan pour que tu
  changes de pays toi-même (toujours pas d'API publique pour l'automatiser).
- Navigation par onglets en bas (Chaînes / Favoris / Vitesse / Réglages),
  plus adaptée au tactile que le menu de la version TV.

## Ce qui n'a volontairement PAS été fait (sur ta demande)

- Pas de surveillance continue du débit pendant la lecture
- Pas de notification automatique en cas de débit faible
- Pas de changement de pays automatique (juste le bouton pour ouvrir Proton VPN)

## Comment builder et installer sur ton téléphone

1. Ouvre le dossier `iptv-mobile-app` dans Android Studio.
2. Laisse Gradle télécharger les dépendances (nécessite Internet — mon
   environnement d'exécution actuel n'y a pas accès, donc je n'ai pas pu
   compiler l'APK moi-même, seulement écrire le code source).
3. Branche ton téléphone en USB avec le débogage USB activé, puis
   **Run > Run 'app'** — ou **Build > Build APK(s)** pour récupérer un
   fichier `.apk` à transférer et installer manuellement.

## Prochaines étapes possibles

1. Tester avec une vraie playlist M3U ou des identifiants Xtream Codes.
2. Ajouter la création de thèmes de favoris personnalisés (actuellement tout
   tombe dans "Général").
3. Une fois validé sur mobile, reporter les ajustements (qualité max, écran
   Vitesse VPN, bandeaux fermables) sur la version TV.
