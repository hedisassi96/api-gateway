package org.example;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.API;
import org.openapitools.model.Error;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@RestController
public class GatewayController {

    @Autowired
    private ApiCache apiCache;

    @Autowired
    private ApiStorageService apiStorageService;

    @Autowired
    private ProxyService proxyService;

    @RequestMapping("/**")
    void gateway(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final String requestPath = req.getRequestURI();
        final String[] splitPath = requestPath.split("/");

        if(splitPath.length < 3) {
            resp.sendError(HttpStatus.NOT_FOUND.value());
            return;
        }

        final String apiId = splitPath[1];
        final String version = splitPath[2];
        final StringBuilder sb = new StringBuilder();
        for (int i = 3; i < splitPath.length; i++) {
            sb.append("/");
            sb.append(splitPath[i]);
        }
        final String urlSuffix = sb.toString();

        final Optional<String> url = apiCache.get(apiId, version);
        if (url.isPresent()) {
            proxyService.forward(req, resp, url.get()+urlSuffix);
            return;
        }

        try {
            final API api = apiStorageService.getApi(apiId, version);
            if (api == null) {
                resp.sendError(HttpStatus.NOT_FOUND.value());
            } else {
                apiCache.put(api.getId(), api.getVersion(), api.getUrl());
                proxyService.forward(req, resp, api.getUrl()+urlSuffix);
            }
        } catch (Exception e) {
            resp.sendError(HttpStatus.NOT_FOUND.value());
        }
    }

    @ControllerAdvice
    public static class ErrorHandler {
        @ExceptionHandler
        public ResponseEntity<org.openapitools.model.Error> handleGlobalException(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                        Error.builder()
                        .code(HttpStatus.INTERNAL_SERVER_ERROR.toString())
                        .message(e.getMessage())
                        .build()
                    );
        }
    }
}
