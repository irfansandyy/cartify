
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./

RUN mvn -q dependency:go-offline
COPY src ./src

RUN mvn -q -DskipTests package

FROM tomcat:10.1-jdk17
ENV JAVA_OPTS="-Xms256m -Xmx512m"
WORKDIR /usr/local/tomcat

RUN rm -rf webapps/ROOT

COPY --from=build /app/target/cartify.war webapps/cartify.war

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=3s --start-period=20s --retries=3 \
  CMD curl -f http://localhost:8080/cartify/ || exit 1

