package company.vk.edu.distrib.compute.sghdjsdfhgfj.urlshortener;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class RedirectHandler implements HttpHandler {
    private final MyUrlShortenerService service;

    public RedirectHandler(MyUrlShortenerService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange xch) throws IOException {
        String method = xch.getRequestMethod();
        if ("GET".equalsIgnoreCase(method)) {
            String urlId = xch.getRequestURI().getPath().replaceFirst("/", "");
            if (service.isValidId(urlId)) {
                if (service.isUrlRegistered(urlId)) {
                    xch.getResponseHeaders().add("Location", service.getLongUrl(urlId));
                    xch.sendResponseHeaders(301, 0);
                } else {
                    xch.sendResponseHeaders(404, 0);
                }
            } else {
                xch.sendResponseHeaders(422, 0);
            }
        } else {
            xch.sendResponseHeaders(405, 0);
        }
        xch.close();
    }
}
