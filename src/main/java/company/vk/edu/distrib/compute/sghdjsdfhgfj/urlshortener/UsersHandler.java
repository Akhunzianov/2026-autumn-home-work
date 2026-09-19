package company.vk.edu.distrib.compute.sghdjsdfhgfj.urlshortener;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class UsersHandler implements HttpHandler {
    private final MyUrlShortenerService service;

    public UsersHandler(MyUrlShortenerService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange xch) throws IOException {
        String method = xch.getRequestMethod();
        if ("POST".equalsIgnoreCase(method)) {
            String[] credentials = RequestUtils.responseBody(xch).split(":");
            if (credentials.length == 2) {
                service.upsertUser(credentials[0], credentials[1]);
                xch.sendResponseHeaders(200, 0);
            } else {
                xch.sendResponseHeaders(422, 0);
            }
        } else {
            xch.sendResponseHeaders(405, 0);
        }
        xch.close();
    }
}
