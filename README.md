# 🏠 SafeHome

**SafeHome** est une solution complète de maison intelligente et sécurisée combinant une application mobile réactive, un backend robuste et une intégration IoT pour la surveillance et l'automatisation en temps réel.

---

## 📌 Présentation du Projet

Ce dépôt regroupe l'ensemble du projet SafeHome sous la forme d'un monorepo contenant :
- **Backend (`backend/safehome_api`)** : API REST développée avec **Spring Boot** (Java 21), connectée à **PostgreSQL (Neon DB)** et intégrant un broker **MQTT (HiveMQ)** pour les communications avec les objets connectés.
- **Frontend (`frontend/safe_home`)** : Application mobile moderne multiplateforme développée avec **Flutter**, architecturée selon les principes de la **Clean Architecture** (Feature-first).

---

## 🏗️ Architecture Globale

```text
SafeHome/
│
├── backend/
│   └── safehome_api/                 # API Spring Boot (Java 21)
│       ├── src/main/java/com/safehome/
│       │   ├── config/               # Configurations (Sécurité, JWT, MQTT, CORS)
│       │   ├── controller/           # Points d'entrée REST (Endpoints)
│       │   ├── dto/                  # Data Transfer Objects (Requêtes / Réponses)
│       │   ├── model/                # Entités JPA / Modèles de données
│       │   ├── repository/           # Interfaces Spring Data JPA
│       │   └── service/              # Logique métier & services applicatifs
│       ├── src/main/resources/       # Configuration Spring (application.properties)
│       ├── pom.xml                   # Dépendances Maven (Security, JPA, JWT, MQTT...)
│       ├── .env.exemple              # Exemple des variables d'environnement
│       └── mvnw / mvnw.cmd           # Wrapper Maven
│
├── frontend/
│   └── safe_home/                    # Application Mobile Flutter
│       ├── lib/
│       │   ├── app.dart              # Configuration globale de l'application (Routes, Thème)
│       │   ├── main.dart             # Point d'entrée de l'application
│       │   ├── core/                 # Éléments transversaux
│       │   │   ├── constants/        # Constantes, couleurs, chaînes
│       │   │   ├── local_storage/    # Gestion du cache / stockage local (tokens, etc.)
│       │   │   └── widgets/          # Widgets réutilisables partagés
│       │   └── features/             # Modules organisés par fonctionnalité (Clean Architecture)
│       │       ├── auth/             # Authentification (Login, Register, Mot de passe)
│       │       ├── dashboard/        # Tableau de bord principal
│       │       ├── surveillance/     # Gestion des caméras et capteurs
│       │       └── automations/      # Règles et scénarios d'automatisation
│       ├── pubspec.yaml              # Dépendances et assets Flutter
│       └── .env.exemple              # Exemple des variables d'environnement frontend
│
├── Document architectural.pdf        # Spécifications et diagrammes d'architecture
├── Document technique.pdf            # Cahier des charges et spécifications techniques
└── README.md                         # Ce fichier
```

### 📐 Organisation interne de chaque Feature Flutter
Chaque feature dans `frontend/safe_home/lib/features/<nom_feature>/` respecte le découpage :
- **`data/`** : `datasources/`, `models/`, `repositories_impl/` (Appels API, parsing JSON, implémentation des contrats)
- **`domain/`** : `entities/`, `repositories/`, `usecases/` (Règles métiers pures, indépendantes de Flutter)
- **`presentation/`** : `providers/` (State Management), `screens/` (Vues), `widgets/` (Composants dédiés)

---

## 🛠️ Stack Technique

| Composant | Technologies & Outils |
| :--- | :--- |
| **Backend** | Java 21, Spring Boot 4.x / 3.x, Spring Data JPA, Spring Security, JJWT |
| **Base de données** | PostgreSQL (Neon DB Cloud) |
| **IoT & Messaging** | MQTT (HiveMQ Broker), Spring Integration MQTT |
| **Frontend Mobile** | Flutter, Dart, Clean Architecture (Feature-driven) |
| **Gestion de versions** | Git & GitHub |

---

## 🚀 Guide de Démarrage Rapide

### Prérequis
- [Java JDK 21](https://www.oracle.com/java/technologies/downloads/#java21) ou supérieur
- [Flutter SDK](https://docs.flutter.dev/get-started/install) (version compatible Dart `^3.11`)
- [Maven](https://maven.apache.org/) (optionnel, le wrapper `mvnw` est fourni)
- Un compte ou une instance [Neon DB](https://neon.tech/) (PostgreSQL)

---

### 1. Cloner le Répertoire

```bash
git clone https://github.com/ALLCLAD/SafeHome.git
cd SafeHome
```

---

### 2. Démarrer le Backend (`backend/safehome_api`)

1. Déplacez-vous dans le dossier backend :
   ```bash
   cd backend/safehome_api
   ```
2. Créez votre fichier `.env` à partir du template :
   ```bash
   # Sur Windows PowerShell :
   Copy-Item .env.exemple .env

   # Sur Linux / macOS :
   cp .env.exemple .env
   ```
3. Renseignez vos identifiants dans `.env` :
   - `DB_URL` : URL JDBC Neon PostgreSQL
   - `DB_USERNAME` : Nom d'utilisateur de la base
   - `DB_PASSWORD` : Mot de passe de la base
   - `JWT_SECRET` : Clé secrète d'au moins 256 bits pour la signature des tokens
   - `MQTT_BROKER_URL` : URL de votre broker MQTT (ex: `tcp://broker.hivemq.com:1883`)
4. Lancez le serveur Spring Boot :
   ```bash
   # Windows :
   .\mvnw.cmd spring-boot:run

   # Linux / macOS :
   ./mvnw spring-boot:run
   ```
   L'API sera accessible sur `http://localhost:8080`.

---

### 3. Démarrer le Frontend (`frontend/safe_home`)

1. Déplacez-vous dans le dossier frontend :
   ```bash
   cd ../../frontend/safe_home
   ```
2. Créez votre fichier `.env` :
   ```bash
   # Sur Windows PowerShell :
   Copy-Item .env.exemple .env

   # Sur Linux / macOS :
   cp .env.exemple .env
   ```
3. Adaptez les valeurs dans le `.env` :
   - `API_BASE_URL` : URL de votre backend Spring Boot (ex: `http://10.0.2.2:8080` sur émulateur Android ou `http://localhost:8080`)
   - `MQTT_BROKER_URL` & `MQTT_PORT` : Coordonnées de connexion au broker
4. Récupérez les dépendances Flutter :
   ```bash
   flutter pub get
   ```
5. Lancez l'application sur un simulateur, émulateur ou appareil connecté :
   ```bash
   flutter run
   ```

---

## 🌿 Bonnes Pratiques & Workflow Git

Pour collaborer efficacement en équipe :
1. **Branches de fonctionnalités** : Ne développez jamais directement sur `main`. Créez des branches dédiées :
   - `feature/auth-login`
   - `feature/mqtt-service`
   - `feature/dashboard-ui`
2. **Fichiers secrets** : Ne **commitez jamais** les fichiers `.env`. Les fichiers `.gitignore` sont déjà configurés pour les exclure. Utilisez toujours les fichiers `.env.exemple` pour documenter les nouvelles variables nécessaires.
3. **Pull Requests (PR)** : Ouvrez une PR vers `main` ou `develop` avec une description claire des modifications avant tout merge.

---

## 👥 Équipe

Projet développé dans le cadre de la solution **SafeHome**.
Pour toute question technique, référez-vous au document technique [`Document technique.pdf`](./Document%20technique.pdf) ou architectural [`Document architectural.pdf`](./Document%20architectural.pdf).
