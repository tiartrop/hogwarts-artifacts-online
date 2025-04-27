package edu.tcu.cs.hogwarts_artifacts_online.wizard;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tcu.cs.hogwarts_artifacts_online.system.StatusCode;
import edu.tcu.cs.hogwarts_artifacts_online.wizard.dto.WizardDto;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Integration tests for Wizard API endpoints")
@Tag("intergration")
public class WizardControllerIntergrationTest {

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
  void testFindWizardByIdSuccess() throws Exception {
    this.mockMvc.perform(get(this.baseUrl + "/wizards/1").header("Authorization", this.token).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Find One Success"))
        .andExpect(jsonPath("$.data.id").value("1"))
        .andExpect(jsonPath("$.data.name").value("Albus Dumbledore"));
  }

  @Test
  void testFindWizardByIdNotFound() throws Exception {
    this.mockMvc.perform(get(this.baseUrl + "/wizards/9").header("Authorization", this.token).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find wizard with Id 9 :("))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testFindAllWizardsSuccess() throws Exception {
    this.mockMvc.perform(get(this.baseUrl + "/wizards").header("Authorization", this.token).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Find All Success"))
        .andExpect(jsonPath("$.data", Matchers.hasSize(3)));
  }

  @Test
  void testAddWizardSuccess() throws Exception {
    // Given
    WizardDto wizardDto = new WizardDto(null, "Hermione Granger", 0);
    String json = this.objectMapper.writeValueAsString(wizardDto);

    this.mockMvc.perform(post(this.baseUrl + "/wizards").header("Authorization", this.token).contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Add Success"))
        .andExpect(jsonPath("$.data.id").value("4"))
        .andExpect(jsonPath("$.data.name").value("Hermione Granger"))
        .andExpect(jsonPath("$.data.numberOfArtifacts").value("0"));

    this.mockMvc.perform(get(this.baseUrl + "/wizards").header("Authorization", this.token).contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Find All Success"))
        .andExpect(jsonPath("$.data", Matchers.hasSize(4)));
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testUpdateWizardSuccess() throws Exception {
    WizardDto wizardDto = new WizardDto(2, "Harry Potter-update", 0);
    String json = this.objectMapper.writeValueAsString(wizardDto);

    this.mockMvc.perform(put(this.baseUrl + "/wizards/2").header("Authorization", this.token).contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Update Success"))
        .andExpect(jsonPath("$.data.id").value("2"))
        .andExpect(jsonPath("$.data.name").value("Harry Potter-update"));
  }

  @Test
  void testUpdateWizardNotFound() throws Exception {
    WizardDto wizardDto = new WizardDto(9, "Tom Riddle", 0);
    String json = this.objectMapper.writeValueAsString(wizardDto);

    this.mockMvc.perform(put(this.baseUrl + "/wizards/9").header("Authorization", this.token).contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find wizard with Id 9 :("))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testDeleteWizardSuccess() throws Exception {
    this.mockMvc.perform(delete(this.baseUrl + "/wizards/3").header("Authorization", this.token).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Delete Success"))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  void testDeleteWizardNotFound() throws Exception {
    this.mockMvc.perform(delete(this.baseUrl + "/wizards/9").header("Authorization", this.token).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find wizard with Id 9 :("))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  void testAssignArtifactSuccess() throws Exception {
    this.mockMvc.perform(put(this.baseUrl + "/wizards/2/artifacts/1250808601744904196").header("Authorization", this.token).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Artifact Assignment Success"))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  void testAssignArtifactErrorWithNonExistentWizardId() throws Exception {
    this.mockMvc.perform(put(this.baseUrl + "/wizards/5/artifacts/1250808601744904191").header("Authorization", this.token).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find wizard with Id 5 :("))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  void testAssignArtifactErrorWithNonExistentArtifactId() throws Exception {
    this.mockMvc.perform(put(this.baseUrl + "/wizards/2/artifacts/1250808601744904199").header("Authorization", this.token).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find artifact with Id 1250808601744904199 :("))
        .andExpect(jsonPath("$.data").isEmpty());
  }

}
