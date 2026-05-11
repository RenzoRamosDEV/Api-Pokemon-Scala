FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/scala-3.4.3/pokeapi-assembly.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
