package company.vk.edu.distrib.compute.sghdjsdfhgfj.urlshortener;

import company.vk.edu.distrib.compute.Dao;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class PersistentDao<T> implements Dao<T> {
    private final Map<String, T> data;
    private final String filename;

    PersistentDao(String filename) {
        this.filename = filename;
        data = new HashMap<>();
    }

    @Override
    @NonNull
    public T get(@NonNull String key) throws NoSuchElementException, IllegalArgumentException, IOException {
        if (!data.containsKey(key)) {
            throw new NoSuchElementException();
        }
        return data.get(key);
    }

    public boolean containsKey(@NonNull String key) {
        return data.containsKey(key);
    }

    @Override
    public void upsert(@NonNull String key, @NonNull T value) throws IllegalArgumentException, IOException {
        data.put(key, value);
        flush();
    }

    @Override
    public void delete(@NonNull String key) throws IllegalArgumentException, IOException {
        data.remove(key);
        flush();
    }

    @Override
    public void close() throws IOException {
        flush();
    }

    private void flush() throws IOException {
        try (OutputStream os = Files.newOutputStream(Paths.get(filename))) {
            ObjectOutputStream outputStream = new ObjectOutputStream(os);
            outputStream.writeObject(data);
        }
    }
}
