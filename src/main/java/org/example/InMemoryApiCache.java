package org.example;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Optional;

@Service
public class InMemoryApiCache implements ApiCache{

    private final HashMap<Pair<String, String>, String> map;

    public InMemoryApiCache() {
        this.map = new HashMap<>();
    }

    @Override
    public Optional<String> get(String apiId, String version) {
        final Pair<String, String> key = Pair.of(apiId, version);
        final String url = map.get(key);
        return Optional.ofNullable(url);
    }

    @Override
    public void put(String apiId, String version, String url) {
        final Pair<String, String> key = Pair.of(apiId, version);
        map.put(key, url);
    }

    @Override
    public void delete(String apiId, String version) {
        final Pair<String, String> key = Pair.of(apiId, version);
        map.remove(key);
    }
}
