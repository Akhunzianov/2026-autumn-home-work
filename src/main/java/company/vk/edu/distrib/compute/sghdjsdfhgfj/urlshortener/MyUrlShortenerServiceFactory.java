package company.vk.edu.distrib.compute.sghdjsdfhgfj.urlshortener;

import company.vk.edu.distrib.compute.AbstractHttpServiceFactory;
import company.vk.edu.distrib.compute.urlshortener.UrlShortenerService;
import org.jspecify.annotations.NonNull;

import java.io.IOException;

public class MyUrlShortenerServiceFactory extends AbstractHttpServiceFactory<UrlShortenerService> {
    @Override
    @NonNull
    protected UrlShortenerService doCreate(int port) throws IOException {
        return new MyUrlShortenerService(port);
    }
}
