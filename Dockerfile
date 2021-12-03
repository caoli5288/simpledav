FROM maven AS build

COPY . /build

WORKDIR /build

RUN mvn package

FROM openjdk:16-slim

COPY --from build /build/target/simpledav.jar /app/

VOLUME ["/app/data"]

EXPOSE 8080/tcp

ENV JAVA_TOOL_OPTIONS="-Xmx1G"

WORKDIR /app/data

ENTRYPOINT ["java", "-jar", "/app/simpledav.jar"]
