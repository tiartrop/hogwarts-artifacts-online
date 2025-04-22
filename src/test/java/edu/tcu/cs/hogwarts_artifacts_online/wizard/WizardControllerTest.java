package edu.tcu.cs.hogwarts_artifacts_online.wizard;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tcu.cs.hogwarts_artifacts_online.system.StatusCode;
import edu.tcu.cs.hogwarts_artifacts_online.wizard.dto.WizardDto;

@SpringBootTest
@AutoConfigureMockMvc
public class WizardControllerTest {

  @Autowired
  MockMvc mockmvc;

  @MockitoBean
  WizardService wizardService;

  @Autowired
  ObjectMapper objectMapper;

  List<Wizard> wizards;

  @BeforeEach
  void setUp() {
    this.wizards = new ArrayList<>();

    Wizard w1 = new Wizard();
    w1.setId(1);
    w1.setName("Albus Dumbledore");
    this.wizards.add(w1);

    Wizard w2 = new Wizard();
    w2.setId(2);
    w2.setName("Harry Potter");
    this.wizards.add(w2);

    Wizard w3 = new Wizard();
    w3.setId(3);
    w3.setName("Neville Longbottom");
    this.wizards.add(w3);
  }

  @AfterEach
  void tearDown() {

  }

  @Test
  void testFindWizardByIdSuccess() throws Exception {
    // Given
    given(this.wizardService.findById(1)).willReturn(this.wizards.get(0));

    // When and then
    this.mockmvc.perform(get("/api/v1/wizards/1").accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Find One Success"))
        .andExpect(jsonPath("$.data.id").value("1"))
        .andExpect(jsonPath("$.data.name").value("Albus Dumbledore"));
  }

  @Test
  void testFindWizardByIdNotFound() throws Exception {
    // Given
    given(this.wizardService.findById(1)).willThrow(new WizardNotFoundException(1));

    // When and then
    this.mockmvc.perform(get("/api/v1/wizards/1").accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find wizard with Id 1 :("))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  void testFindAllWizardsSuccess() throws Exception {
    // Given
    given(this.wizardService.findAll()).willReturn(this.wizards);

    // When and then
    this.mockmvc.perform(get("/api/v1/wizards").accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Find All Success"))
        .andExpect(jsonPath("$.data", Matchers.hasSize(this.wizards.size())))
        .andExpect(jsonPath("$.data[0].id").value("1"))
        .andExpect(jsonPath("$.data[0].name").value("Albus Dumbledore"))
        .andExpect(jsonPath("$.data[1].id").value("2"))
        .andExpect(jsonPath("$.data[1].name").value("Harry Potter"));
  }

  @Test
  void testAddWizardSuccess() throws Exception {
    // Given
    WizardDto wizardDto = new WizardDto(null, "Hermione Granger", 0);
    String json = this.objectMapper.writeValueAsString(wizardDto);

    Wizard savedWizard = new Wizard();
    savedWizard.setId(4);
    savedWizard.setName("Hermione Granger");

    given(this.wizardService.save(Mockito.any(Wizard.class))).willReturn(savedWizard);

    // When and then
    this.mockmvc.perform(post("/api/v1/wizards").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Add Success"))
        .andExpect(jsonPath("$.data.id").value(savedWizard.getId()))
        .andExpect(jsonPath("$.data.name").value(savedWizard.getName()))
        .andExpect(jsonPath("$.data.numberOfArtifacts").value(savedWizard.getNumberOfArtifacts()));
  }

  @Test
  void testUpdateWizardSuccess() throws Exception {
    // Given
    WizardDto wizardDto = new WizardDto(2, "Harry Potter", 0);
    String json = this.objectMapper.writeValueAsString(wizardDto);

    Wizard updateWizard = new Wizard();
    updateWizard.setId(2);
    updateWizard.setName("Harry Potter-update");

    given(this.wizardService.update(eq(2), Mockito.any(Wizard.class))).willReturn(updateWizard);

    // When and then
    this.mockmvc.perform(put("/api/v1/wizards/2").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Update Success"))
        .andExpect(jsonPath("$.data.id").value("2"))
        .andExpect(jsonPath("$.data.name").value(updateWizard.getName()));
  }

  @Test
  void testUpdateWizardNotFound() throws Exception {
    // Given
    WizardDto wizardDto = new WizardDto(2, "Harry Potter", 0);
    String json = this.objectMapper.writeValueAsString(wizardDto);

    given(this.wizardService.update(eq(2), Mockito.any(Wizard.class))).willThrow(new WizardNotFoundException(2));

    // When and then
    this.mockmvc.perform(put("/api/v1/wizards/2").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find wizard with Id 2 :("))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  void testDeleteWizardSuccess() throws Exception {
    // Given
    doNothing().when(this.wizardService).delete(1);

    // When and then
    this.mockmvc.perform(delete("/api/v1/wizards/1").accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Delete Success"))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  void testDeleteWizardNotFound() throws Exception {
    // Given
    doThrow(new WizardNotFoundException(1)).when(this.wizardService).delete(1);

    // When and then
    this.mockmvc.perform(delete("/api/v1/wizards/1").accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find wizard with Id 1 :("))
        .andExpect(jsonPath("$.data").isEmpty());
  }

}
