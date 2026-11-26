# ---------- Etapa 1: compilar con Maven ----------
FROM maven:3.9.9-eclipse-temurin-17 AS compilacion
WORKDIR /fuente

# Primero solo el pom: las dependencias quedan en caché y no se descargan
# de nuevo mientras el pom no cambie.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# Las pruebas se corren aparte con "mvn test"; aquí solo se empaqueta.
RUN mvn -B -q package -DskipTests

# ---------- Etapa 2: imagen final, solo con el JRE ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Usuario sin privilegios para no correr la aplicación como root.
RUN groupadd --system vri && useradd --system --gid vri --no-create-home vri

COPY --from=compilacion /fuente/target/contratacion-vri-*.jar /app/app.jar
RUN chown vri:vri /app/app.jar
USER vri

ENV TZ=America/Bogota \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -Duser.timezone=America/Bogota"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
