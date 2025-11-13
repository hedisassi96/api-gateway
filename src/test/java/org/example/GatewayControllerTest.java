package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.API;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
public class GatewayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Main.AppConfig appConfig;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() throws IOException {
        Files.createDirectories(Path.of(appConfig.getAppFolderPath()));
    }

    @AfterEach
    public void tearDown() throws IOException {
        final File directory = new File(appConfig.getAppFolderPath());
        FileUtils.deleteDirectory(directory);
    }

    @Test
    void testForward() throws Exception {
        final API api1 = API.builder()
                .id("ai-cats")
                .version("v1")
                .url("https://api.ai-cats.net/v1")
                .status(API.StatusEnum.ACTIVE)
                .build();

        registerApi(api1);

        this.mockMvc.perform(get("/ai-cats/v1/cat"))
                .andExpect(status().isOk());
    }

    private void registerApi(final API api) throws Exception {
        final String stringValue = objectMapper.writeValueAsString(api);
        this.mockMvc.perform(
                        post("/apis")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(stringValue))
                .andExpect(status().isOk())
                .andExpect(content().json(stringValue));
    }
}
