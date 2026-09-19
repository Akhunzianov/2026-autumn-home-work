package company.vk.edu.distrib.compute.sghdjsdfhgfj.urlshortener.handlers;

import com.sun.net.httpserver.HttpExchange;
import company.vk.edu.distrib.compute.sghdjsdfhgfj.urlshortener.MyUrlShortenerService;
import company.vk.edu.distrib.compute.sghdjsdfhgfj.urlshortener.RequestUtils;

import java.io.IOException;

public class UsersHandler implements CustomHttpHandler {
    private final MyUrlShortenerService service;

    public UsersHandler(MyUrlShortenerService service) {
        this.service = service;
    }

    @Override
    public void handlePost(HttpExchange xch) throws IOException {
        String[] credentials = RequestUtils.responseBody(xch).split(":");
        if (credentials.length == 2) {
            service.upsertUser(credentials[0], credentials[1]);
            xch.sendResponseHeaders(200, 0);
        } else {
            xch.sendResponseHeaders(422, 0);
        }
    }
}
