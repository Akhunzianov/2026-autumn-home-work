package company.vk.edu.distrib.compute.rsmt98.urlshortener;

import company.vk.edu.distrib.compute.Dao;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.NoSuchElementException;
import java.util.Random;

final class LinkStore {
    private static final String ALPHABET =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int ID_LENGTH = 10;
    private final Dao<String> links;
    private final Random random = new Random();

    LinkStore(Dao<String> links) {
        this.links = links;
    }

    synchronized String get(String id) throws IOException {
        validateId(id);
        return links.get(id);
    }

    synchronized String create(String longLink) throws IOException {
        validateLink(longLink);
        String id;
        do {
            id = randomId();
        } while (exists(id));
        links.upsert(id, longLink);
        return id;
    }

    synchronized void update(String id, String longLink) throws IOException {
        validateId(id);
        validateLink(longLink);
        links.get(id);
        links.upsert(id, longLink);
    }

    synchronized void delete(String id) throws IOException {
        validateId(id);
        links.delete(id);
    }

    private boolean exists(String id) throws IOException {
        try {
            links.get(id);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    private String randomId() {
        char[] id = new char[ID_LENGTH];
        for (int i = 0; i < id.length; ++i) {
            id[i] = ALPHABET.charAt(random.nextInt(ALPHABET.length()));
        }
        return new String(id);
    }

    static void validateId(String id) {
        if (id.length() != ID_LENGTH) {
            throw new IllegalArgumentException(
                    "Link ID must have 10 ASCII alphanumeric characters");
        }
        for (int i = 0; i < id.length(); ++i) {
            if (ALPHABET.indexOf(id.charAt(i)) < 0) {
                throw new IllegalArgumentException(
                        "Link ID must have 10 ASCII alphanumeric characters");
            }
        }
    }

    private static void validateLink(String value) {
        URI uri;
        try {
            uri = new URI(value);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid link", e);
        }
        if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                || uri.getHost() == null
                || uri.getPort() > 65535) {
            throw new IllegalArgumentException("Link must use HTTP or HTTPS and include a host");
        }
    }
}
