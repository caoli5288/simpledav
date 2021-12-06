# SimpleDAV

MongoDB's GridFS as a WebDAV server witten by Java.

## Build

```
$ mvn clean package
```

## Run

### Standalone

```
$ mvn clean package
$ java -jar target/simpledav.jar
```

### Docker Compose

```yaml
version: '3.4'

services:
  simpledav:
    image: ghcr.io/caoli5288/simpledav:latest
    restart: unless-stopped
    ports:
      - '8080:8080'
    environment:
      JAVA_TOOL_OPTIONS: "-Xmx1G"
      AUTH_BASIC: "user:pass"
      HTTP_HOST: "0.0.0.0"
      HTTP_PORT: "8080"
      MONGODB_URL: "mongodb://example.com"
      MONGODB_DB: "files"
```

## Configurations

### System env

```
$ export AUTH_BASIC="user:pass"
$ export MONGODB_URL="mongodb://127.0.0.1"
$ export MONGODB_DB="files"
$ export HTTP_HOST="0.0.0.0"
$ export HTTP_PORT="8080"
```

### Java commandline

```
$ java -Dauth.basic="user:pass" \
    -Dhttp.host="0.0.0.0" \
    -Dhttp.port="8080" \
    -Dmongodb.url="mongodb://127.0.0.1" \
    -Dmongodb.db="files" \
    -jar simpledav.jar
```

### Configuration file

```
$ cat server.properties
auth.basic=
http.host=0.0.0.0
http.port=8080
mongodb.url=mongodb://127.0.0.1
mongodb.db=files
```