package org.example;

import org.openapitools.model.API;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ApiStorageService {
    void insertApi(API api) throws StorageServiceException;
    void updateApi(API api) throws StorageServiceException;
    void deleteApi(String apiId, String version) throws StorageServiceException;
    API getApi(String apiId, String version) throws StorageServiceException;
    List<API> listApis() throws StorageServiceException;
}
