package company.vk.edu.distrib.compute.sghdjsdfhgfj.urlshortener;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ExceptionHandler implements HttpHandler {
    private final HttpHandler handler;
    private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandler.class);

    public ExceptionHandler(HttpHandler handler) {
        this.handler = handler;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try {
            handler.handle(httpExchange);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            httpExchange.sendResponseHeaders(500, 0);
            httpExchange.getResponseBody().write(e.toString().getBytes());
            httpExchange.close();
        }
    }
}
