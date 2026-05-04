-- ============================================================
-- ParkManager - Script SQL pour XAMPP (MySQL)
-- Créer la base de données et les tables
-- ============================================================

CREATE DATABASE IF NOT EXISTS parking_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE parking_db;

-- ============================================================
-- Tables (Hibernate les crée automatiquement avec ddl-auto=update)
-- Ce script est fourni pour référence / initialisation manuelle
-- ============================================================

-- Table principale utilisateurs (héritage JOINED)
CREATE TABLE IF NOT EXISTS utilisateurs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS super_admins (
    utilisateur_id BIGINT PRIMARY KEY,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS admins_parking (
    utilisateur_id BIGINT PRIMARY KEY,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS clients (
    utilisateur_id BIGINT PRIMARY KEY,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS parkings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    adresse VARCHAR(255) NOT NULL,
    capacite INT NOT NULL,
    admin_parking_id BIGINT,
    FOREIGN KEY (admin_parking_id) REFERENCES admins_parking(utilisateur_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS places (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero INT NOT NULL,
    disponible TINYINT(1) NOT NULL DEFAULT 1,
    type VARCHAR(30) NOT NULL,
    parking_id BIGINT NOT NULL,
    FOREIGN KEY (parking_id) REFERENCES parkings(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS vehicules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    immatriculation VARCHAR(20) NOT NULL UNIQUE,
    marque VARCHAR(50),
    modele VARCHAR(50),
    type VARCHAR(30) NOT NULL,
    client_id BIGINT NOT NULL,
    FOREIGN KEY (client_id) REFERENCES clients(utilisateur_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date_debut DATETIME NOT NULL,
    date_fin DATETIME NOT NULL,
    statut VARCHAR(30) NOT NULL DEFAULT 'EN_ATTENTE',
    client_id BIGINT NOT NULL,
    place_id BIGINT NOT NULL,
    vehicule_id BIGINT NOT NULL,
    FOREIGN KEY (client_id) REFERENCES clients(utilisateur_id),
    FOREIGN KEY (place_id) REFERENCES places(id),
    FOREIGN KEY (vehicule_id) REFERENCES vehicules(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS paiements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    montant DOUBLE NOT NULL,
    date_paiement DATETIME,
    statut VARCHAR(30) NOT NULL DEFAULT 'EN_ATTENTE',
    reservation_id BIGINT NOT NULL UNIQUE,
    FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tarifs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prix_heure DOUBLE NOT NULL,
    type_vehicule VARCHAR(30) NOT NULL,
    parking_id BIGINT NOT NULL,
    FOREIGN KEY (parking_id) REFERENCES parkings(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS statistiques (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_reservations INT DEFAULT 0,
    revenus DOUBLE DEFAULT 0.0,
    parking_id BIGINT NOT NULL UNIQUE,
    FOREIGN KEY (parking_id) REFERENCES parkings(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- Note: Le SuperAdmin par défaut est créé automatiquement
-- au démarrage de l'application (DataInitializer.java)
-- Email: superadmin@parking.com | Mot de passe: admin123
-- ============================================================
