package org.example;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface ApiCache {
    Optional<String> get(String apiId, String version);
    void put(String apiId, String version, String url);
    void delete(String apiId, String version);
}
