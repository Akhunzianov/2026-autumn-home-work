package company.vk.edu.distrib.compute.sghdjsdfhgfj.urlshortener;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class LinksHandler implements HttpHandler {
    private final MyUrlShortenerService service;

    public LinksHandler(MyUrlShortenerService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange xch) throws IOException {
        if (!service.isAuthenticated(xch)) {
            xch.sendResponseHeaders(401, 0);
            xch.close();
            return;
        }
        String method = xch.getRequestMethod();
        if ("GET".equalsIgnoreCase(method)) {
            handleGet(xch);
        } else if ("POST".equalsIgnoreCase(method)) {
            handlePost(xch);
        } else if ("PUT".equalsIgnoreCase(method)) {
            handlePut(xch);
        } else if ("DELETE".equalsIgnoreCase(method)) {
            handleDelete(xch);
        } else {
            xch.sendResponseHeaders(405, 0);
            xch.close();
        }
    }

    private void handleGet(HttpExchange xch) throws IOException {
        String[] separatedPath = xch.getRequestURI().getPath().split("/");
        if (separatedPath.length < 4) {
            xch.sendResponseHeaders(405, 0);
            xch.close();
            return;
        }
        String urlId = separatedPath[3];
        if (!RequestUtils.isValidId(urlId)) {
            xch.sendResponseHeaders(422, 0);
            xch.close();
            return;
        }
        if (!service.isUrlRegistered(urlId)) {
            xch.sendResponseHeaders(404, 0);
            xch.close();
            return;
        }
        String url = service.getLongUrl(urlId);
        xch.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        xch.sendResponseHeaders(200, 0);
        xch.getResponseBody().write(url.getBytes());
        xch.close();
    }

    private void handlePost(HttpExchange xch) throws IOException {
        String url = RequestUtils.responseBody(xch);
        if (!RequestUtils.isValidUrl(url)) {
            xch.sendResponseHeaders(422, 0);
            xch.close();
            return;
        }
        String id = RequestUtils.generateId();
        while (service.isUrlRegistered(id)) {
            id = RequestUtils.generateId();
        }
        service.upsertUrl(id, url);
        xch.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        xch.sendResponseHeaders(201, 0);

        String shortLink = service.host() + "/" + id;
        xch.getResponseBody().write(shortLink.getBytes());
        xch.close();
    }

    private void handlePut(HttpExchange xch) throws IOException {
        String url = RequestUtils.responseBody(xch);
        String[] separatedPath = xch.getRequestURI().getPath().split("/");
        if (separatedPath.length < 4) {
            xch.sendResponseHeaders(405, 0);
            xch.close();
            return;
        }
        String id = separatedPath[3];
        if (!RequestUtils.isValidId(id)) {
            xch.sendResponseHeaders(422, 0);
            xch.close();
            return;
        }
        if (!RequestUtils.isValidUrl(url)) {
            xch.sendResponseHeaders(422, 0);
            xch.close();
            return;
        }
        if (!service.isUrlRegistered(id)) {
            xch.sendResponseHeaders(404, 0);
            xch.close();
            return;
        }
        service.upsertUrl(id, url);
        xch.sendResponseHeaders(200, 0);
        xch.close();
    }

    private void handleDelete(HttpExchange xch) throws IOException {
        String[] separatedPath = xch.getRequestURI().getPath().split("/");
        if (separatedPath.length < 4) {
            xch.sendResponseHeaders(405, 0);
            xch.close();
            return;
        }
        String id = separatedPath[3];
        if (!RequestUtils.isValidId(id)) {
            xch.sendResponseHeaders(422, 0);
            xch.close();
            return;
        }
        service.deleteUrl(id);
        xch.sendResponseHeaders(202, 0);
        xch.close();
    }
}
