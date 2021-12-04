# SimpleDAV

GridFS as a WebDAV server.

## Build

```
$ mvn clean package
```

## Run

### Standalone

```
java -jar simpledav.jar
```

### Docker Compose

```yaml
version: '3.1'

services:
  mongo:
    image: mongo
    restart: unless-stopped

  simpledav:
    build:
      context: .
    restart: unless-stopped
    ports:
      - '8080:8080'
    environment:
      JAVA_TOOL_OPTIONS: "-Xmx1G"
      AUTH_BASIC: "user:password"
      HTTP_HOST: "0.0.0.0"
      HTTP_PORT: "8080"
      MONGODB_URL: "mongodb://127.0.0.1"
      MONGODB_DB: "files"
```
