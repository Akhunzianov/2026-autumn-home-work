package company.vk.edu.distrib.compute.rsmt98.urlshortener;

import com.sun.net.httpserver.HttpServer;

import company.vk.edu.distrib.compute.urlshortener.UrlShortenerService;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class PersistentUrlShortenerService implements UrlShortenerService {
    private static final Path DATA_DIR = Path.of("data");
    private final int port;
    private boolean startCalled;
    @Nullable private HttpServer server;
    @Nullable private ExecutorService executor;
    @Nullable private FileStringDao linkDao;
    @Nullable private FileStringDao userDao;

    public PersistentUrlShortenerService(int port) {
        this.port = port;
    }

    @Override
    public synchronized void start() {
        if (startCalled) {
            throw new IllegalStateException("Service can only be started once");
        }
        startCalled = true;
        try {
            FileStringDao links = new FileStringDao(DATA_DIR.resolve("links"));
            linkDao = links;
            FileStringDao users = new FileStringDao(DATA_DIR.resolve("users"));
            userDao = users;
            HttpServer httpServer = HttpServer.create(new InetSocketAddress("localhost", port), 0);
            server = httpServer;
            ExecutorService workers = Executors.newVirtualThreadPerTaskExecutor();
            executor = workers;
            httpServer.setExecutor(workers);
            httpServer.createContext(
                    "/",
                    new UrlShortenerHandler(
                            port, links, users, () -> links.isAvailable() && users.isAvailable()));
            httpServer.start();
        } catch (IOException | RuntimeException e) {
            try {
                stop();
            } catch (RuntimeException ee) {
                e.addSuppressed(ee);
            }
            throw new IllegalStateException("Cannot start URL shortener on port " + port, e);
        }
    }

    @Override
    public synchronized void stop() {
        HttpServer httpServer = server;
        server = null;
        if (httpServer != null) {
            httpServer.stop(0);
        }
        ExecutorService workers = executor;
        executor = null;
        if (workers != null) {
            workers.close();
        }
        FileStringDao links = linkDao;
        FileStringDao users = userDao;
        try (links;
                users) {
            linkDao = null;
            userDao = null;
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot close URL shortener storage", e);
        }
    }
}
