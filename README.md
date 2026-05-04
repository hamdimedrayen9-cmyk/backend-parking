# 🅿️ ParkManager – Application de Gestion de Parking

Application web Spring Boot + Thymeleaf + MySQL (XAMPP)

---

## 🚀 Démarrage rapide

### Prérequis
- Java 17+
- Maven 3.8+
- XAMPP (MySQL activé)

---

## ⚙️ Configuration

### 1. Démarrer XAMPP
- Ouvrez XAMPP Control Panel
- Démarrez **Apache** et **MySQL**

### 2. Créer la base de données
- Ouvrez **phpMyAdmin** → http://localhost/phpmyadmin
- Cliquez sur **"Nouvelle base de données"**
- Nom : `parking_db1`, Encodage : `utf8mb4_unicode_ci`
- Cliquez **Créer**

> 💡 Hibernate créera les tables automatiquement au premier démarrage grâce à `ddl-auto=update`

### 3. Vérifier la configuration
Ouvrez `src/main/resources/application.properties` :
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/parking_db
spring.datasource.username=root
spring.datasource.password=        ← Laissez vide si XAMPP sans mot de passe
```

### 4. Lancer l'application
```bash
mvn spring-boot:run
```

Ou depuis votre IDE (IntelliJ / Eclipse) : exécutez `ParkingApplication.java`

### 5. Accéder à l'application
Ouvrez : **http://localhost:8080**

---

## 👤 Comptes par défaut

| Rôle | Email | Mot de passe |
|------|-------|-------------|
| **SuperAdmin** | superadmin@parking.com | admin123 |

> Les comptes Admin et Client se créent depuis l'interface SuperAdmin ou la page `/register`

---

## 🗺️ Fonctionnalités par rôle

### 🔑 SuperAdmin
- Dashboard global avec statistiques
- Créer / modifier / supprimer des parkings
- Créer des admins et les assigner à un parking
- Voir la liste des clients
- Statistiques globales (revenus, réservations)

### 🛠️ Admin Parking
- Dashboard de son parking
- Gérer les places (ajouter, supprimer, types)
- Gérer les tarifs par type de véhicule
- Valider ou refuser les réservations
- Consulter les statistiques de son parking

### 👥 Client
- Inscription / connexion
- Ajouter / gérer ses véhicules
- Réserver une place (choix parking → place → dates → véhicule)
- Payer une réservation validée
- Annuler une réservation
- Historique de ses réservations

---

## 🏗️ Architecture

```
src/main/java/com/parking/
├── ParkingApplication.java
├── config/
│   ├── SecurityConfig.java       ← Spring Security
│   └── DataInitializer.java      ← Création SuperAdmin par défaut
├── entity/                        ← Entités JPA (héritage JOINED)
│   ├── Utilisateur.java
│   ├── SuperAdmin.java
│   ├── AdminParking.java
│   ├── Client.java
│   ├── Parking.java
│   ├── Place.java
│   ├── Vehicule.java
│   ├── Reservation.java
│   ├── Paiement.java
│   ├── Tarif.java
│   └── Statistique.java
├── repository/                    ← Interfaces JPA
├── service/                       ← Logique métier
│   ├── CustomUserDetailsService.java
│   ├── UtilisateurService.java
│   ├── ParkingService.java
│   ├── ReservationService.java
│   └── VehiculeService.java
└── controller/                    ← Controllers HTTP
    ├── AuthController.java
    ├── SuperAdminController.java
    ├── AdminParkingController.java
    └── ClientController.java

src/main/resources/
├── application.properties
└── templates/
    ├── login.html
    ├── register.html
    ├── superadmin/
    │   ├── dashboard.html
    │   ├── parkings.html
    │   ├── parking-form.html
    │   ├── admins.html
    │   ├── clients.html
    │   └── statistiques.html
    ├── admin/
    │   ├── dashboard.html
    │   ├── places.html
    │   ├── tarifs.html
    │   ├── reservations.html
    │   └── statistiques.html
    └── client/
        ├── dashboard.html
        ├── vehicules.html
        ├── reservations.html
        ├── reserver.html
        └── places-disponibles.html
```

---

## 🗄️ Modèle de données

**Héritage JOINED :**
- `Utilisateur` ← `SuperAdmin`
- `Utilisateur` ← `AdminParking`
- `Utilisateur` ← `Client`

**Relations principales :**
- `Parking` 1—1 `AdminParking`
- `Parking` 1—N `Place`
- `Parking` 1—N `Tarif`
- `Parking` 1—1 `Statistique`
- `Client` 1—N `Vehicule`
- `Client` 1—N `Reservation`
- `Reservation` N—1 `Place`
- `Reservation` N—1 `Vehicule`
- `Reservation` 1—0..1 `Paiement`

---

## 🔐 Sécurité

- Authentification par Spring Security
- Mots de passe hachés avec BCrypt
- Redirections automatiques selon le rôle (SUPERADMIN / ADMIN_PARKING / CLIENT)
- Pages protégées par rôle (`/superadmin/**`, `/admin/**`, `/client/**`)

---

## 🛠️ Technologies utilisées

| Technologie | Version |
|-------------|---------|
| Spring Boot | 3.2.0 |
| Spring Security | 6.x |
| Spring Data JPA | 3.x |
| Thymeleaf | 3.x |
| MySQL Connector | 8.x |
| Lombok | latest |
| Java | 17 |
