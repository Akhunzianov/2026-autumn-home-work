package company.vk.edu.distrib.compute.rsmt98.urlshortener;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import company.vk.edu.distrib.compute.Dao;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.function.BooleanSupplier;

final class UrlShortenerHandler implements HttpHandler {
    private static final String LINKS_PATH = "/v0/links/";
    private final String shortLinkPrefix;
    private final LinkStore links;
    private final BasicAuthentication auth;
    private final BooleanSupplier available;

    UrlShortenerHandler(int port, Dao<String> links, Dao<String> users, BooleanSupplier available) {
        shortLinkPrefix = "http://localhost:" + port + '/';
        this.links = new LinkStore(links);
        auth = new BasicAuthentication(users);
        this.available = available;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            Response response = process(exchange);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            if (response.body().isEmpty()) {
                exchange.sendResponseHeaders(response.status(), -1);
            } else {
                byte[] body = response.body().getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(response.status(), body.length);
                exchange.getResponseBody().write(body);
            }
        }
    }

    private Response process(HttpExchange exchange) {
        try {
            return route(exchange);
        } catch (NoSuchElementException e) {
            return new Response(404);
        } catch (IllegalArgumentException | CharacterCodingException e) {
            return new Response(422);
        } catch (IOException e) {
            return new Response(503);
        }
    }

    private Response route(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getRawPath();
        String method = exchange.getRequestMethod();
        if ("/v0/status".equals(path)) {
            return status(exchange);
        }
        if ("/internal/users".equals(path)) {
            return registerUser(exchange);
        }
        boolean redirectPath = path.startsWith("/") && path.indexOf('/', 1) == -1;
        if (redirectPath && "GET".equals(method)) {
            String id = path.substring(1);
            String longLink = links.get(id);
            exchange.getResponseHeaders().set("Location", URI.create(longLink).toASCIIString());
            return new Response(301);
        }
        if (!auth.authenticate(exchange.getRequestHeaders())) {
            exchange.getResponseHeaders().set("WWW-Authenticate", BasicAuthentication.CHALLENGE);
            return new Response(401);
        }
        if (path.length() == LINKS_PATH.length() - 1 && LINKS_PATH.startsWith(path)) {
            return "POST".equals(method)
                    ? new Response(201, shortLinkPrefix + links.create(readBody(exchange)))
                    : methodNotAllowed(exchange, "POST");
        }
        if (path.startsWith(LINKS_PATH)) {
            String id = path.substring(LINKS_PATH.length());
            return accessLink(exchange, id);
        }
        return redirectPath ? methodNotAllowed(exchange, "GET") : new Response(404);
    }

    private Response status(HttpExchange exchange) {
        return "GET".equals(exchange.getRequestMethod())
                ? new Response(available.getAsBoolean() ? 200 : 503)
                : methodNotAllowed(exchange, "GET");
    }

    private Response registerUser(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            return methodNotAllowed(exchange, "POST");
        }
        auth.register(readBody(exchange));
        return new Response(200);
    }

    private Response accessLink(HttpExchange exchange, String id) throws IOException {
        return switch (exchange.getRequestMethod()) {
            case "GET" -> new Response(200, links.get(id));
            case "PUT" -> {
                links.update(id, readBody(exchange));
                yield new Response(200);
            }
            case "DELETE" -> {
                links.delete(id);
                yield new Response(202);
            }
            default -> {
                LinkStore.validateId(id);
                yield methodNotAllowed(exchange, "GET, PUT, DELETE");
            }
        };
    }

    private static Response methodNotAllowed(HttpExchange exchange, String methods) {
        exchange.getResponseHeaders().set("Allow", methods);
        return new Response(405);
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        byte[] bytes = exchange.getRequestBody().readAllBytes();
        return StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
    }

    private record Response(int status, String body) {
        private Response(int status) {
            this(status, "");
        }
    }
}
