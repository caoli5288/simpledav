package com.github.caoli5288.simpledav;

import io.javalin.core.security.BasicAuthCredentials;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.UnauthorizedResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class BasicAccessor implements Handler {

    @Getter
    private final BasicAuthCredentials credentials;

    @Override
    public void handle(@NotNull Context context) {
        if (context.basicAuthCredentialsExist()) {
            BasicAuthCredentials auth = context.basicAuthCredentials();
            if (auth.equals(credentials)) {
                return;
            }
        }
        throw new UnauthorizedResponse();
    }

    public static BasicAccessor extract(String s) {
        if (!s.matches("\\w+:\\w+")) {
            throw new IllegalArgumentException("Config auth.basic syntax is <user>:<pass>");
        }
        String[] split = s.split(":");
        return new BasicAccessor(new BasicAuthCredentials(split[0], split[1]));
    }
}
