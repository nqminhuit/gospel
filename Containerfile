FROM docker.io/maven:3.9.7-eclipse-temurin-21-alpine AS build
COPY pom.xml /app/gospel/
WORKDIR /app/gospel
RUN mvn -q verify clean --fail-never
COPY . /app/gospel
RUN mvn -B \
    -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
    clean package

FROM quay.io/quarkus/ubi-quarkus-mandrel-builder-image:23.1-jdk-21
USER root
WORKDIR /app/gospel
COPY --from=build /app/gospel/target/gospel-*.jar target/
COPY --from=build /app/gospel/target/lib target/lib
RUN native-image -march=compatibility -cp target/gospel-*.jar "org.nqm.Gospel" --no-fallback
RUN mv org.nqm.gospel gospel
RUN chmod +x gospel
RUN ./gospel --date=2024/09/22 --center --width=80
