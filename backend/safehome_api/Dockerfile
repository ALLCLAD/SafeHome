# ==========================================
# Etape 1 : Construction du jar (Build)
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Mettre en cache les dependances Maven
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copier le code source et compiler le JAR executable
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# Etape 2 : Image d'execution legere (Alpine JRE)
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Installation de tzdata pour la gestion des fuseaux horaires
RUN apk add --no-cache tzdata
ENV TZ=UTC

# Port dynamique (Render injecte la variable PORT automatiquement)
ENV PORT=8080
EXPOSE ${PORT}

# Copier le fichier JAR produit a l'etape 1
COPY --from=build /app/target/*.jar app.jar

# Optimisation memoire JVM (384Mo max pour respecter le quota gratuit 512Mo de Render)
# et passage explicite du port pour eviter tout conflit reseau
ENTRYPOINT ["sh", "-c", "java -Xmx384m -Dserver.port=${PORT} -jar app.jar"]
