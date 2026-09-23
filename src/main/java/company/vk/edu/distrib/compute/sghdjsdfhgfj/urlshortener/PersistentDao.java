package company.vk.edu.distrib.compute.sghdjsdfhgfj.urlshortener;

import company.vk.edu.distrib.compute.Dao;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class PersistentDao implements Dao<String> {
    private final Map<String, String> data;
    private final String filename;

    PersistentDao(String filename) throws IOException {
        this.filename = filename;
        data = new HashMap<>();
        read();
    }

    @Override
    public String get(String key) throws NoSuchElementException, IllegalArgumentException, IOException {
        if (!data.containsKey(key)) {
            throw new NoSuchElementException();
        }
        return data.get(key);
    }

    public boolean containsKey(String key) {
        return data.containsKey(key);
    }

    @Override
    public void upsert(String key, String value) throws IllegalArgumentException, IOException {
        data.put(key, value);
        append(key, value);
    }

    @Override
    public void delete(String key) throws IllegalArgumentException, IOException {
        data.remove(key);
        append(key, "");
    }

    @Override
    public void close() throws IOException {
        //
    }

    private void read() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(filename, "rw")) {
            try {
                while (true) {
                    String key = file.readUTF();
                    String value = file.readUTF();
                    if (value.isEmpty()) {
                        data.remove(key);
                    } else {
                        data.put(key, value);
                    }
                }
            } catch (EOFException e) {
                //
            }
        }
    }

    private void append(String key, String value) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(filename, "rw")) {
            file.seek(file.length());
            file.writeUTF(key);
            file.writeUTF(value);
        }
    }
}
