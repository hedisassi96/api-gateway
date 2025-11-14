package org.example;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;

@Slf4j
@Service
public class ProxyService {

    public void forward(HttpServletRequest req, HttpServletResponse resp, String urlValue) throws IOException {
        log.info("Sending request to {}", urlValue);

        final URL url = new URL(urlValue);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod(req.getMethod());
        conn.setDoOutput(true);

        // set headers
        final Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String headerName = headerNames.nextElement();
            conn.setRequestProperty(headerName, req.getHeader(headerName));
        }
        //Remove header and add new header
        conn.setRequestProperty("host", "");
        conn.addRequestProperty("via", "api-gateway");

        // set query parameters
        if (!req.getParameterMap().isEmpty()) {
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

}
