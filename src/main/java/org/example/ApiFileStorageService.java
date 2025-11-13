package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.API;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ApiFileStorageService implements ApiStorageService{

    @Autowired
    private Main.AppConfig appConfig;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void insertApi(API api) throws StorageServiceException {
        try{
            final File file = getApiFile(api);
            if (file.isFile()) {
                log.error("Api {} with version {} already exists", api.getId(), api.getVersion());
                throw StorageServiceException.builder()
                        .statusCode(HttpStatus.CONFLICT)
                        .build();
            }

            objectMapper.writeValue(file, api);
        } catch (final Exception e) {
            log.error("Exception encountered while saving api {} with version {}", api.getId(), api.getVersion(), e);
            throw StorageServiceException.builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    private File getApiFile(final API api) throws StorageServiceException {
        return getApiFile(api.getId(), api.getVersion());
    }

    private File getApiFile(final String apiId, final String version) throws StorageServiceException {
        try {
            final Path directoryPath = Paths.get(appConfig.getAppFolderPath(), apiId);
            Files.createDirectories(directoryPath);

            final Path filePath = directoryPath.resolve(version);
            return new File(filePath.toUri());
        } catch (final Exception e) {
            throw StorageServiceException.builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    @Override
    public void updateApi(API api) throws StorageServiceException {
        try{
            final File file = getApiFile(api);
            objectMapper.writeValue(file, api);
        } catch (final Exception e) {
            log.error("Exception encountered while saving api {} with version {}", api.getId(), api.getVersion(), e);
            throw StorageServiceException.builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    @Override
    public void deleteApi(String apiId, String version) throws StorageServiceException {
        final File file = getApiFile(apiId, version);
        if (!file.delete()) {
            throw StorageServiceException.builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
                    .errorMessage(String.format("Error while deleting api %s with version %s", apiId, version))
                    .build();
        }
    }

    @Override
    public API getApi(String apiId, String version) throws StorageServiceException {
        final File file = getApiFile(apiId, version);
        try {
            if (file.isFile()) {
                return objectMapper.readValue(file, API.class);
            }
        } catch (final Exception e) {
            log.error("Failed to get api {} with version {}", apiId, version, e);
            throw StorageServiceException.builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
                    .errorMessage(String.format("Issue retrieving API with id %s and version %s", apiId, version))
                    .build();
        }

        throw StorageServiceException.builder()
                .statusCode(HttpStatus.NOT_FOUND)
                .errorMessage(String.format("API with id %s and version %s not found", apiId, version))
                .build();
    }

    @Override
    public List<API> listApis() throws StorageServiceException {
        final File topLevelDirectory = new File(appConfig.getAppFolderPath());

        final File[] subDirectories = topLevelDirectory.listFiles();
        if (subDirectories == null) {
            throw StorageServiceException.builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
                    .errorMessage("Error listing APIs in top level directory")
                    .build();
        }

        if (subDirectories.length == 0) {
            return List.of();
        }

        final List<API> result = new ArrayList<>();
        for (final File subDirectory: subDirectories) {
            final List<API> versions = getAllApiVersions(subDirectory.getName());
            result.addAll(versions);
        }

        return result;
    }

    private List<API> getAllApiVersions(final String apiId) throws StorageServiceException {
        final Path apiPath = Paths.get(appConfig.getAppFolderPath(), apiId);
        final File apiDirectory = new File(apiPath.toUri());
        if (!apiDirectory.isDirectory()) {
            log.warn("File {} not a directory", apiDirectory.getName());
            return List.of();
        }

        final File[] versions = apiDirectory.listFiles();
        if (versions == null) {
            throw StorageServiceException.builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
                    .errorMessage(String.format("Error listing API versions for api %s", apiId))
                    .build();
        }

        if (versions.length == 0) {
            return List.of();
        }

        final List<API> result = new ArrayList<>();
        for (final File version : versions) {
            final API api = getApi(apiId, version.getName());
            result.add(api);
        }

        return result;
    }
}
