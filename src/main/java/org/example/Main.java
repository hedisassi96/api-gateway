package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class Main {

    private static final String APP_FOLDER_PATH = "./app_data";

    public static void main(String[] args) throws IOException {
        Files.createDirectories(Paths.get(APP_FOLDER_PATH));
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public AppConfig createAppConfig() {
        return AppConfig.builder()
                .appFolderPath(APP_FOLDER_PATH)
                .build();
    }

    @Bean
    public ObjectMapper createObjectMapper() {
        return new ObjectMapper();
    }

    @Builder
    @Getter
    public static class AppConfig {
        public String appFolderPath;
    }
}