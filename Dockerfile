# Imagen para desplegar en un servidor (por ejemplo, Easypanel).
# Para correrlo en la facultad o en la PC NO hace falta Docker: ver README.md.

# --- Etapa 1: compila el proyecto ---
FROM maven:3.9-eclipse-temurin-17 AS compilacion
WORKDIR /proyecto
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

# --- Etapa 2: solo lo necesario para ejecutar ---
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=compilacion /proyecto/target/ppai-bolsines-1.0.jar app.jar
# Dentro del contenedor hay que escuchar en todas las interfaces, no solo en localhost.
ENV SERVER_ADDRESS=0.0.0.0
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
