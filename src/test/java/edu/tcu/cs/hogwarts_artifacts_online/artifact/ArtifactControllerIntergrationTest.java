package edu.tcu.cs.hogwarts_artifacts_online.artifact;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tcu.cs.hogwarts_artifacts_online.artifact.dto.ArtifactDto;
import edu.tcu.cs.hogwarts_artifacts_online.system.StatusCode;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Integration tests for Artifact API endpoints")
@Tag("intergration")
@ActiveProfiles(value = "dev")
public class ArtifactControllerIntergrationTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @Value("${api.endpoint.base-url}")
  String baseUrl;

  String token;

  @BeforeEach
  void setUp() throws Exception {
    ResultActions resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("john", "123456")));
    MvcResult mvcResult = resultActions.andDo(print()).andReturn();
    String contentAsString = mvcResult.getResponse().getContentAsString();
    JSONObject json = new JSONObject(contentAsString);
    this.token = "Bearer " + json.getJSONObject("data").getString("token"); // Don't foget to add "Bearer " as a prefix.
  }

  @Test
  void testFindArtifactByIdSuccess() throws Exception {
    this.mockMvc.perform(get(this.baseUrl + "/artifacts/1250808601744904191").accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Find One Success"))
        .andExpect(jsonPath("$.data.id").value("1250808601744904191"))
        .andExpect(jsonPath("$.data.name").value("Deluminator"));
  }

  @Test
  void testFindArtifactByIdNotFound() throws Exception {
    this.mockMvc.perform(get(this.baseUrl + "/artifacts/1250808601744904199").accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find artifact with Id 1250808601744904199 :("))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testFindAllArtifactsSuccess() throws Exception {
    this.mockMvc.perform(get(this.baseUrl + "/artifacts").contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Find All Success"))
        .andExpect(jsonPath("$.data.content", Matchers.hasSize(6)));
  }

  @Test
  void testAddArtifactSuccess() throws Exception {
    ArtifactDto artifactDto = new ArtifactDto(null,
                                              "Remembrall",
                                              "A Remembrall was a magical large marble-sized glass ball that contained smoke which turned red when its owner or user had forgotten something. It turned clear once whatever was forgotten was remembered.",
                                              "ImageUrl",
                                              null);
    String json = this.objectMapper.writeValueAsString(artifactDto);

    this.mockMvc.perform(post(this.baseUrl + "/artifacts").contentType(MediaType.APPLICATION_JSON).header("Authorization", this.token).content(json).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Add Success"))
        .andExpect(jsonPath("$.data.id").isNotEmpty())
        .andExpect(jsonPath("$.data.name").value("Remembrall"))
        .andExpect(jsonPath("$.data.description").value("A Remembrall was a magical large marble-sized glass ball that contained smoke which turned red when its owner or user had forgotten something. It turned clear once whatever was forgotten was remembered."))
        .andExpect(jsonPath("$.data.imageUrl").value("ImageUrl"));

    this.mockMvc.perform(get(this.baseUrl + "/artifacts").contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Find All Success"))
        .andExpect(jsonPath("$.data.content", Matchers.hasSize(7)));
  }

  @Test
  void testUpdateArtifactSuccess() throws Exception {
    ArtifactDto artifactDto = new ArtifactDto("1250808601744904192", "Invisibility Cloak", "A new description.", "ImageUrl", null);
    String json = this.objectMapper.writeValueAsString(artifactDto);

    this.mockMvc.perform(put(this.baseUrl + "/artifacts/1250808601744904192").header("Authorization", this.token).contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Update Success"))
        .andExpect(jsonPath("$.data.id").value("1250808601744904192"))
        .andExpect(jsonPath("$.data.name").value("Invisibility Cloak"))
        .andExpect(jsonPath("$.data.description").value("A new description."))
        .andExpect(jsonPath("$.data.imageUrl").value("ImageUrl"));
  }

  @Test
  void testUpdateArtifactNotFound() throws Exception {
    ArtifactDto artifactDto = new ArtifactDto("1250808601744904199", "Invisibility Cloak", "A new description.", "ImageUrl", null);
    String json = this.objectMapper.writeValueAsString(artifactDto);

    this.mockMvc.perform(put(this.baseUrl + "/artifacts/1250808601744904199").header("Authorization", this.token).contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find artifact with Id 1250808601744904199 :("))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  void testDeleteArtifactSuccess() throws Exception {
    this.mockMvc.perform(delete(this.baseUrl + "/artifacts/1250808601744904194").header("Authorization", this.token).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Delete Success"))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  void testDeleteArtifactNotFound() throws Exception {
    this.mockMvc.perform(delete(this.baseUrl + "/artifacts/1250808601744904199").header("Authorization", this.token).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find artifact with Id 1250808601744904199 :("))
        .andExpect(jsonPath("$.data").isEmpty());
  }

}
