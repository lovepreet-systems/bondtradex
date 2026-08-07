FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .

RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline

COPY src src

RUN ./mvnw clean package -DskipTests


FROM eclipse-temurin:21-jre AS runtime

RUN addgroup --system appadmins \
    && adduser --system \
        --ingroup appadmins \
        bondtradex-dev

WORKDIR /app

RUN mkdir -p /app/logs \
    && chown -R bondtradex-dev:appadmins /app

COPY --from=build \
    --chown=bondtradex-dev:appadmins \
    /app/target/*.jar app.jar

USER bondtradex-dev

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]