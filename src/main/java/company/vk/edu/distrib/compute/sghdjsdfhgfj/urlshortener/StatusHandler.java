package company.vk.edu.distrib.compute.sghdjsdfhgfj.urlshortener;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class StatusHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange xch) throws IOException {
        String method = xch.getRequestMethod();
        if ("GET".equalsIgnoreCase(method)) {
            xch.sendResponseHeaders(200, 0);
        } else {
            xch.sendResponseHeaders(405, 0);
        }
        xch.close();
    }
}
