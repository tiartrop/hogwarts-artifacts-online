package edu.tcu.cs.hogwarts_artifacts_online.wizard;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class WizardService {

  private final WizardRepository wizardRepository;

  public WizardService(WizardRepository wizardRepository) {
    this.wizardRepository = wizardRepository;
  }

  public Wizard findById(Integer wizardId) {
    return this.wizardRepository.findById(wizardId).orElseThrow(() -> new WizardNotFoundException(wizardId));
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
                                .orElseThrow(() -> new WizardNotFoundException(wizardId));
  }

  public void delete(Integer wizardId) {
    this.wizardRepository.findById(wizardId).orElseThrow(() -> new WizardNotFoundException(wizardId));
    this.wizardRepository.deleteById(wizardId);
  }

}
