package org.example;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.API;
import org.openapitools.model.Error;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RestController
public class GatewayController {

    @Autowired
    private ApiCache apiCache;

    @Autowired
    private ApiStorageService apiStorageService;

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
            forward(req, resp, url.get()+urlSuffix);
            return;
        }

        try {
            final API api = apiStorageService.getApi(apiId, version);
            if (api == null) {
                resp.sendError(HttpStatus.NOT_FOUND.value());
            } else {
                forward(req, resp, api.getUrl()+urlSuffix);
            }
        } catch (Exception e) {
            resp.sendError(HttpStatus.NOT_FOUND.value());
        }
    }

    private static void forward(HttpServletRequest req, HttpServletResponse resp, String urlValue) throws IOException {
        log.info("Sending request to {}", urlValue);

        final URL url = new URL(urlValue);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod(req.getMethod());
        conn.setDoOutput(true);

        if (Objects.equals(req.getMethod(), HttpMethod.POST.name())) {
            final String postParams = getParametersMap(req.getParameterMap());

            DataOutputStream paramsWriter = new DataOutputStream(conn.getOutputStream());
            paramsWriter.writeBytes(postParams);
            paramsWriter.flush();
            paramsWriter.close();
        }

        InputStream remoteResponse = conn.getInputStream();
        OutputStream localResponder = resp.getOutputStream();
        int c;
        while((c = remoteResponse.read()) != -1) {
            localResponder.write(c);
        }
        remoteResponse.close();
        localResponder.close();

        conn.disconnect();
    }

    private static String getParametersMap(final Map<String, String[]> parameterMap) {
        if (parameterMap.isEmpty()) {
            return "";
        }

        final StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            sb.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue()[0]);
            sb.append("&");
        }

        final String result = sb.toString();
        return result.substring(0, result.length() - 1); // cut last "&"
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
