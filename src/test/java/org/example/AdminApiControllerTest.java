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
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
public class AdminApiControllerTest {

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
    void listApisWithEmptyFolderShouldReturnEmptyList() throws Exception {
        this.mockMvc.perform(get("/apis"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void listApisWithTwoVersionsShouldReturnTwoItems() throws Exception {
        final API api1 = API.builder()
                .id("id1")
                .version("1.0.0")
                .url("whatever")
                .status(API.StatusEnum.ACTIVE)
                .build();

        final API api2 = API.builder()
                .id("id1")
                .version("1.1.0")
                .url("whatever")
                .status(API.StatusEnum.ACTIVE)
                .build();
        registerApi(api1);
        registerApi(api2);

        final String expectedJson = objectMapper.writeValueAsString(List.of(api1, api2));
        this.mockMvc.perform(get("/apis"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson, JsonCompareMode.LENIENT));
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

    @Test
    void listApisWithTwoAPIsShouldReturnTwoItems() throws Exception {
        final API api1 = API.builder()
                .id("id1")
                .version("1.0.0")
                .url("whatever")
                .status(API.StatusEnum.ACTIVE)
                .build();

        final API api2 = API.builder()
                .id("id2")
                .version("1.1.0")
                .url("whatever")
                .status(API.StatusEnum.ACTIVE)
                .build();
        registerApi(api1);
        registerApi(api2);

        final String expectedJson = objectMapper.writeValueAsString(List.of(api1, api2));
        this.mockMvc.perform(get("/apis"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testGetApi() throws Exception {
        final String apiId = "id1";
        final String version = "1.0.0";
        final API api1 = API.builder()
                .id(apiId)
                .version(version)
                .url("whatever")
                .status(API.StatusEnum.ACTIVE)
                .build();

        registerApi(api1);

        final String expectedJson = objectMapper.writeValueAsString(api1);
        this.mockMvc.perform(get("/api/"+apiId+"/"+version))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testPutApi() throws Exception {
        final String apiId = "id1";
        final String version = "1.0.0";
        final API api1 = API.builder()
                .id(apiId)
                .version(version)
                .url("whatever")
                .status(API.StatusEnum.ACTIVE)
                .build();

        registerApi(api1);

        final API updatedApi = API.builder()
                .id(apiId)
                .version(version)
                .url("new url")
                .status(API.StatusEnum.ACTIVE)
                .build();

        final String updatedJson = objectMapper.writeValueAsString(updatedApi);
        this.mockMvc.perform(put("/api/"+apiId+"/"+version)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson)
                )
                .andExpect(status().isOk())
                .andExpect(content().json(updatedJson, JsonCompareMode.LENIENT));

        this.mockMvc.perform(get("/api/"+apiId+"/"+version))
                .andExpect(status().isOk())
                .andExpect(content().json(updatedJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testDeleteApi() throws Exception {
        final String apiId = "id1";
        final String version = "1.0.0";
        final API api1 = API.builder()
                .id(apiId)
                .version(version)
                .url("whatever")
                .status(API.StatusEnum.ACTIVE)
                .build();

        registerApi(api1);

        this.mockMvc.perform(delete("/api/"+apiId+"/"+version))
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/api/"+apiId+"/"+version))
                .andExpect(status().isNotFound());
    }

}
