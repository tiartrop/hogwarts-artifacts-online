package edu.tcu.cs.hogwarts_artifacts_online.wizard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import edu.tcu.cs.hogwarts_artifacts_online.artifact.Artifact;
import edu.tcu.cs.hogwarts_artifacts_online.system.exception.ObjectNotFoundException;

@SpringBootTest
@AutoConfigureMockMvc
public class WizardServiceTest {

  @Mock
  WizardRepository wizardRepository;

  @InjectMocks
  WizardService wizardService;

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
  void testFindByIdSuccess() {
    // Given
    Artifact a1 = new Artifact();
    a1.setId("1250808601744904192");
    a1.setName("Invisibility Cloak");
    a1.setDescription("An invisibility cloak is used to make the wearer invisible.");
    a1.setImageUrl("ImageUrl");

    Artifact a2 = new Artifact();
    a2.setId("1250808601744904193");
    a2.setName("Elder Wand");
    a2.setDescription("The Elder Wand, known throughout history as the Deathstick or the Wand of Destiny, is an extremely powerful wand made of elder wood with a core of Thestral tail hair.");
    a2.setImageUrl("ImageUrl");

    Wizard w = new Wizard();
    w.setId(1);
    w.setName("Albus Dumbledore");
    w.addArtifact(a1);
    w.addArtifact(a2);

    given(this.wizardRepository.findById(1)).willReturn(Optional.of(w));

    // When
    Wizard returnedWizard = this.wizardService.findById(1);

    // Then
    assertThat(returnedWizard.getId()).isEqualTo(w.getId());
    assertThat(returnedWizard.getName()).isEqualTo(w.getName());
    assertThat(returnedWizard.getNumberOfArtifacts()).isEqualTo(2);
    assertThat(returnedWizard.getArtifacts().get(0)).isEqualTo(a1);
    verify(this.wizardRepository, times(1)).findById(1);
  }

  @Test
  void testFindByIdNotFound() {
    // Given
    given(this.wizardRepository.findById(Mockito.any(Integer.class))).willReturn(Optional.empty());

    // When
    assertThrows(ObjectNotFoundException.class, () -> {
      this.wizardService.findById(1);
    });

    // Then
    verify(this.wizardRepository, times(1)).findById(1);
  }

  @Test
  void testFindAllSuccess() {
    // Given
    given(this.wizardRepository.findAll()).willReturn(this.wizards);

    // When
    List<Wizard> actualWizards = this.wizardService.findAll();

    // Then
    assertThat(actualWizards.size()).isEqualTo(this.wizards.size());
    verify(this.wizardRepository, times(1)).findAll();
  }

  @Test
  void testSaveSuccess() {
    // Given
    Wizard newWizard = new Wizard();
    newWizard.setName("Hermione Granger");

    given(this.wizardRepository.save(newWizard)).willReturn(newWizard);

    // When
    Wizard savedWizard = this.wizardService.save(newWizard);

    // Then
    assertThat(savedWizard.getName()).isEqualTo(newWizard.getName());
    verify(this.wizardRepository, times(1)).save(newWizard);
  }

  @Test
  void testUpdateSuccess() {
    // Given
    Wizard oldWizard = new Wizard();
    oldWizard.setId(2);
    oldWizard.setName("Harry Potter");

    Wizard update = new Wizard();
    update.setName("Harry Potter-update");

    given(this.wizardRepository.findById(2)).willReturn(Optional.of(oldWizard));
    given(this.wizardRepository.save(oldWizard)).willReturn(oldWizard);

    // When
    Wizard updatedWizard = this.wizardService.update(2, update);

    // Then
    assertThat(updatedWizard.getId()).isEqualTo(2);
    assertThat(updatedWizard.getName()).isEqualTo(update.getName());
    verify(this.wizardRepository, times(1)).findById(2);
    verify(this.wizardRepository, times(1)).save(oldWizard);
  }

  @Test
  void testUpdateNotFound() {
    // Given
    Wizard update = new Wizard();
    update.setName("Harry Potter-update");

    given(this.wizardRepository.findById(2)).willReturn(Optional.empty());

    // When
    assertThrows(ObjectNotFoundException.class, () -> {
      this.wizardService.update(2, update);
    });

    // Then
    verify(this.wizardRepository, times(1)).findById(2);
  }

  @Test
  void testDeleteSuccess() {
    Wizard wizard = new Wizard();
    wizard.setId(1);
    wizard.setName("Albus Dumbledore");

    given(this.wizardRepository.findById(1)).willReturn(Optional.of(wizard));
    doNothing().when(this.wizardRepository).deleteById(1);

    // When
    this.wizardService.delete(1);

    // Then
    verify(this.wizardRepository, times(1)).deleteById(1);
  }

  @Test
  void testDeleteNotFound() {
    given(this.wizardRepository.findById(1)).willReturn(Optional.empty());

    // When
    assertThrows(ObjectNotFoundException.class, () -> {
      this.wizardService.delete(1);
    });

    // Then
    verify(this.wizardRepository, times(1)).findById(1);
  }

}
