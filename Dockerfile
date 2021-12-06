FROM maven AS build

COPY . /build

WORKDIR /build

RUN mvn package

FROM openjdk:17-slim

COPY --from=build /build/target/simpledav.jar /app/

VOLUME ["/app/data"]

EXPOSE 8080/tcp

ENV JAVA_TOOL_OPTIONS="-Xmx1G" AUTH_BASIC="user:password" HTTP_HOST="0.0.0.0" HTTP_PORT="8080" MONGODB_URL="mongodb://127.0.0.1" MONGODB_DB="files"

WORKDIR /app/data

ENTRYPOINT ["java", "-jar", "/app/simpledav.jar"]
