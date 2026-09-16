package company.vk.edu.distrib.compute.sghdjsdfhgfj.urlshortener;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import company.vk.edu.distrib.compute.urlshortener.UrlShortenerService;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;

public class MyUrlShortenerService implements UrlShortenerService {
    private final HttpServer server;
    private final PersistentDao<String> urls;
    private final PersistentDao<String> users;

    private static final int ID_LENGTH = 10;
    private final Random random = new Random();

    public MyUrlShortenerService(int port) throws IOException {
        InetSocketAddress addr = new InetSocketAddress(port);
        server = HttpServer.create(addr, 0);
        urls = new PersistentDao<>("urls.dat");
        users = new PersistentDao<>("users.dat");

        server.createContext("/v0/status", new StatusHandler());
        server.createContext("/v0/links", new LinksHandler(this));
        server.createContext("/internal/users", new UsersHandler(this));
        server.createContext("/", new RedirectHandler(this));
    }

    @Override
    public void start() {
        server.start();
    }

    @Override
    public void stop() {
        server.stop(0);
    }

    public String responseBody(HttpExchange xch) throws IOException {
        byte[] contents = xch.getRequestBody().readAllBytes();
        return new String(contents, StandardCharsets.UTF_8);
    }

    public String host() {
        return "http://localhost:" + server.getAddress().getPort();
    }

    public boolean isValidUrl(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }
    }

    public boolean isValidId(String id) {
        return id.length() == ID_LENGTH && id.matches("^[a-zA-Z0-9]+$");
    }

    public String generateId() {
        String characters = "0123456789qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ID_LENGTH; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

    public boolean isAuthenticated(HttpExchange xch) throws IOException {
        String header = xch.getRequestHeaders().getFirst("Authorization");
        if (header == null) {
            return false;
        }
        if (!header.startsWith("Basic ")) {
            return false;
        }
        Base64.Decoder decoder = Base64.getDecoder();
        String auth = new String(decoder.decode(header.substring(6)), StandardCharsets.UTF_8);
        String[] creds = auth.split(":");
        return creds.length == 2 && users.containsKey(creds[0]) && users.get(creds[0]).equals(creds[1]);
    }

    public boolean isUrlRegistered(String id) {
        return urls.containsKey(id);
    }

    public void upsertUrl(String id, String url) throws IOException {
        urls.upsert(id, url);
    }

    public String getLongUrl(String id) throws IOException {
        return urls.get(id);
    }

    public void deleteUrl(String id) throws IOException {
        urls.delete(id);
    }

    public void upsertUser(String username, String password) throws IOException {
        users.upsert(username, password);
    }
}
