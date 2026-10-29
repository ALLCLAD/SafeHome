# ==========================================
# Etape 1 : Construction du jar (Build)
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Mettre en cache les dependances Maven
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copier le code source et compiler
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# Etape 2 : Image d'execution legere (Alpine)
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Port par defaut (Render injecte sa propre variable PORT)
ENV PORT=8080
EXPOSE ${PORT}

# Copier le fichier JAR produit a l'etape 1
COPY --from=build /app/target/*.jar app.jar

# Optimisation memoire JVM pour le plan gratuit Render (512 Mo max)
ENTRYPOINT ["java", "-Xmx384m", "-jar", "app.jar"]
