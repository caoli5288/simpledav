package com.github.caoli5288.simpledav;

import com.github.caoli5288.simpledav.fs.FileNode;
import com.github.caoli5288.simpledav.fs.IFileSystem;
import com.github.caoli5288.simpledav.fs.mongodb.GridFileSystem;
import io.javalin.Javalin;
import io.javalin.core.JavalinConfig;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import io.javalin.http.UnauthorizedResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.UrlEncoded;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Consumer;

@Slf4j
public class SimpleDAV {

    private static IFileSystem fs;

    @SneakyThrows
    public static void main(String[] args) {
        reloadConfig();
        configServer(Javalin.create(configServlet()))
                .options("/*", s -> s.status(200))// Always ok
                .get("/*", s -> s.result(fs.cat(UrlEncoded.decodeString(s.path()))))
                .put("/*", s -> put(UrlEncoded.decodeString(s.path()), s))
                .delete("/*", s -> del(UrlEncoded.decodeString(s.path()), s))
                .addHandler(HandlerType.INVALID, "/*", s -> apply(s.method(), s))
                .exception(FileNotFoundException.class, (e, context) -> context.status(404).result(e.toString()))
                .exception(UnauthorizedResponse.class, (e, context) -> context.status(401).header("WWW-Authenticate", "Basic realm=WebDAV"))
                .start();
    }

    private static Javalin configServer(Javalin javalin) {
        Utils.let(System.getProperty("auth.basic"), s -> {
            BasicAccessor accessor = BasicAccessor.extract(s);
            javalin.before(accessor);
            log.info("{} initialized", accessor.getCredentials());
        });
        return javalin;
    }

    private static Consumer<JavalinConfig> configServlet() {
        return options -> {
            log.info("Context path {}", options.contextPath);
            options.requestLogger((context, mills) -> {
                String method = context.method();
                log.info("ip={} ops={},{} t={}", context.ip(), method, context.fullUrl(), mills);
            });
            options.server(() -> {
                Server server = new Server();
                Utils.let(System.getProperty("http.host"), it -> {
                    ServerConnector connector = new ServerConnector(server);
                    connector.setHost(it);
                    connector.setPort(Integer.getInteger("http.port", 8080));
                    server.addConnector(connector);
                });
                Utils.let(System.getProperty("https.host"), it -> {
                    SslContextFactory sslCtx = new SslContextFactory();
                    sslCtx.setKeyStorePath(Utils.asUrl(System.getProperty("https.jks")).toExternalForm());
                    Utils.let(System.getProperty("https.jks.password"), sslCtx::setKeyStorePassword);
                    ServerConnector connector = new ServerConnector(server, sslCtx);
                    connector.setHost(System.getProperty("https.host"));
                    connector.setPort(Integer.getInteger("https.port", 4433));
                    server.addConnector(connector);
                });
                return server;
            });
        };
    }

    private static void put(String path, Context s) {
        fs.put(path, s.bodyAsInputStream());
        s.status(204);
    }

    private static void del(String path, Context s) {
        fs.rm(path);
        s.status(204);
    }

    private static void apply(String method, Context context) throws IOException {
        switch (method) {
            case "PROPFIND":
                find(context);
                break;
            case "PROPPATCH": // TODO ret OK currently
                break;
            case "MKCOL":
                fs.mkdir(UrlEncoded.decodeString(context.path()));
                break;
            case "MOVE":
                move(context);
                break;
        }
    }

    private static void move(Context context) throws IOException {
        String s = context.header("Destination");
        Objects.requireNonNull(s, "Destination not defined");
        String des = URI.create(s).getPath();// URI context decided unicode chars
        boolean force = "F".equalsIgnoreCase(context.header("Overwrite"));
        fs.mv(UrlEncoded.decodeString(context.path()), des, force);
    }

    @SneakyThrows
    private static void reloadConfig() {
        // env
        envMap("AUTH_BASIC", "auth.basic");
        envMap("MONGODB_URL", "mongodb.url");
        envMap("MONGODB_DB", "mongodb.db");
        envMap("HTTP_HOST", "http.host");
        envMap("HTTP_PORT", "http.port");
        // props
        File file = new File("server.properties");
        if (!file.exists()) {
            InputStream in = SimpleDAV.class.getClassLoader().getResourceAsStream("server.properties");
            Files.copy(Objects.requireNonNull(in), file.toPath());
        }
        Properties properties = new Properties();
        properties.load(new FileReader(file));
        properties.forEach(System.getProperties()::putIfAbsent);
        fs = new GridFileSystem(System.getProperty("mongodb.url"), System.getProperty("mongodb.db"));
        fs.setup();
    }

    private static void find(Context ctx) throws IOException {
        String path = UrlEncoded.decodeString(ctx.path());
        List<FileNode> ls = fs.ls(path, "1".equalsIgnoreCase(ctx.header("Depth")));
        ctx.status(207).result(Constants.XML_MULTI_STATUS
                .format(Utils.concat(FileNode.toString(ls))));
    }

    static void envMap(String env, String props) {
        Utils.let(System.getenv(env), s -> System.setProperty(props, s));
    }
}
