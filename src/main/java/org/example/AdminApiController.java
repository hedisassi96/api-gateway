package org.example;

import org.openapitools.model.API;

import org.openapitools.model.Error;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class AdminApiController {

    @Autowired
    private ApiStorageService apiStorageService;

    @GetMapping("/apis")
    ResponseEntity<?> getAllAPIs() throws StorageServiceException  {
        final List<API> apis = apiStorageService.listApis();
        return ResponseEntity.of(Optional.of(apis));
    }

    @PostMapping("/apis")
    ResponseEntity<?> registerAPI(@RequestBody API api) throws StorageServiceException {
        apiStorageService.insertApi(api);
        return ResponseEntity.of(Optional.of(api));
    }

    @GetMapping("/api/{apiId}/{version}")
    ResponseEntity<?> getAPI(@PathVariable String apiId, @PathVariable String version) throws StorageServiceException {
        final API api = apiStorageService.getApi(apiId, version);
        return ResponseEntity.of(Optional.of(api));
    }

    @DeleteMapping("/api/{apiId}/{version}")
    ResponseEntity<?> deleteAPI(@PathVariable String apiId, @PathVariable String version) throws StorageServiceException {
        apiStorageService.deleteApi(apiId, version);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/api/{apiId}/{version}")
    ResponseEntity<?> updateAPI(@RequestBody API api, @PathVariable String apiId, @PathVariable String version) throws ApiException, StorageServiceException {
        if (!api.getId().equals(apiId)
        || !api.getVersion().equals(version)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "API ID and version cannot be changed once created");
        }

        apiStorageService.updateApi(api);
        return ResponseEntity.of(Optional.of(api));
    }

    @ControllerAdvice
    public static class ErrorHandler {
        @ExceptionHandler
        public ResponseEntity<Error> handleGlobalException(ApiException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new Error(e.getStatusCode().toString(), e.getMessage()));
        }

        @ExceptionHandler
        public ResponseEntity<Error> handleGlobalException(StorageServiceException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new Error(e.getStatusCode().toString(), e.getMessage()));
        }
    }
}
