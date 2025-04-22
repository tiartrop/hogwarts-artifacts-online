package edu.tcu.cs.hogwarts_artifacts_online.wizard;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.tcu.cs.hogwarts_artifacts_online.artifact.Artifact;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.ArtifactRepository;
import edu.tcu.cs.hogwarts_artifacts_online.system.exception.ObjectNotFoundException;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class WizardService {

  private final WizardRepository wizardRepository;

  private final ArtifactRepository artifactRepository;

  public WizardService(WizardRepository wizardRepository, ArtifactRepository artifactRepository) {
    this.wizardRepository = wizardRepository;
    this.artifactRepository = artifactRepository;
  }

  public Wizard findById(Integer wizardId) {
    return this.wizardRepository.findById(wizardId).orElseThrow(() -> new ObjectNotFoundException("wizard", wizardId));
  }

  public List<Wizard> findAll() {
    return this.wizardRepository.findAll();
  }

  public Wizard save(Wizard newWizard) {
    return this.wizardRepository.save(newWizard);
  }

  public Wizard update(Integer wizardId, Wizard update) {
    return this.wizardRepository.findById(wizardId)
                                .map(oldWizard -> {
                                  oldWizard.setName(update.getName());
                                  return this.wizardRepository.save(oldWizard);
                                })
                                .orElseThrow(() -> new ObjectNotFoundException("wizard", wizardId));
  }

  public void delete(Integer wizardId) {
    Wizard wizardToBeDeleted = this.wizardRepository.findById(wizardId).orElseThrow(() -> new ObjectNotFoundException("wizard", wizardId));
    // Before deletion, we will unassign ths wizard's owned artifacts.
    wizardToBeDeleted.removeAllArtifacts();
    this.wizardRepository.deleteById(wizardId);
  }

  public void assignArtifact(Integer wizardId, String artifactId) {
    // Find this artifact by Id from DB.
    Artifact artifactToBeAssigned = this.artifactRepository.findById(artifactId).orElseThrow(() -> new ObjectNotFoundException("artifact", artifactId));

    // Find this wizard by Id from DB.
    Wizard wizard = this.wizardRepository.findById(wizardId).orElseThrow(() -> new ObjectNotFoundException("wizard", wizardId));

    // Artifact assignment
    if(artifactToBeAssigned.getOwner() != null) {
      artifactToBeAssigned.getOwner().removeArtifact(artifactToBeAssigned);
    }
    wizard.addArtifact(artifactToBeAssigned);
  }

}
