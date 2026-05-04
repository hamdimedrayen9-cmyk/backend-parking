# Configuration Nginx pour la Sécurité du Dashboard

Cette configuration permet de protéger l'accès au dashboard SuperAdmin au niveau du serveur Nginx, en plus de la sécurité applicative.

## Étapes pour appliquer la correction :

1. **Installer Nginx** (si ce n'est pas déjà fait).
2. **Générer un fichier .htpasswd** :
   Utilisez un générateur en ligne ou la commande suivante (nécessite apache2-utils ou similaire) :
   ```bash
   htpasswd -c C:/nginx/conf/.htpasswd admin
   ```
   Entrez le mot de passe souhaité quand demandé.

3. **Copier la configuration** :
   Copiez le contenu de `parking-app.conf` dans votre fichier de configuration Nginx (souvent `nginx.conf` ou un fichier dans `sites-enabled`).

4. **Redémarrer Nginx** :
   ```bash
   nginx -s reload
   ```

## Sécurité renforcée :
- L'application Spring Boot a été configurée pour n'écouter que sur `127.0.0.1:8087`. Cela empêche quiconque d'accéder directement au port 8087 depuis le réseau sans passer par Nginx.
- Nginx demande maintenant une authentification HTTP Basic pour toute URL commençant par `/superadmin`.
