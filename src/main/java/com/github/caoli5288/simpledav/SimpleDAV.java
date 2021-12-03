package com.github.caoli5288.simpledav;

import com.github.caoli5288.simpledav.fs.FileNode;
import com.github.caoli5288.simpledav.fs.IFileSystem;
import com.github.caoli5288.simpledav.fs.mongodb.MongodbFileSystem;
import io.javalin.Javalin;
import io.javalin.core.JavalinConfig;
import io.javalin.http.Context;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
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
        Javalin.create(configServlet())
                .options("/*", s -> s.result("OK"))// Always ok
                .get("/*", s -> cat(s.path(), s))
                .put("/*", s -> put(s.path(), s))
                .delete("/*", s -> del(s.path(), s))
                .after("/*", s -> apply(s.method(), s))
                .start(Integer.getInteger("http.port", 8080));
    }

    private static Consumer<JavalinConfig> configServlet() {
        return options -> {
            log.info("Context path {}", options.contextPath);
            options.requestLogger((context, mills) -> {
                String method = context.method();
                log.info("ip={} method={} url={} t={}", context.ip(), method, context.fullUrl(), mills);
            });
            String auth = System.getProperty("auth.basic", "");
            if (!auth.isEmpty()) {
            }
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

    private static void apply(String method, Context context) {
        switch (method) {
            case "PROPFIND":
                find(context);
                break;
            case "PROPPATCH": // Not supported
                context.status(200).result("");
                break;
            case "MKCOL":
                fs.mkdir(context.path());
                context.status(200).result("");
                break;
        }
    }

    private static void cat(String path, Context context) {
        InputStream stream = fs.cat(path);
        if (stream == null) {
            context.status(404);
        } else {
            context.result(stream);
        }
    }

    @SneakyThrows
    private static void reloadConfig() {
        File file = new File("server.properties");
        if (!file.exists()) {
            InputStream in = SimpleDAV.class.getClassLoader().getResourceAsStream("server.properties");
            Files.copy(Objects.requireNonNull(in), file.toPath());
        }
        Properties properties = new Properties();
        properties.load(new FileReader(file));
        properties.forEach(System.getProperties()::putIfAbsent);
        fs = new MongodbFileSystem(System.getProperty("mongodb.url"), System.getProperty("mongodb.db"));
        fs.setup();
    }

    private static void find(Context ctx) {
        String path = ctx.path();
        List<FileNode> ls = fs.ls(path);
        if (ls == null) {
            ctx.status(404);
        } else {
            ctx.status(207).result(Constants.XML_MULTI_STATUS
                    .replace("<!---->", FileNode.toString(ls)));
        }
    }
}
