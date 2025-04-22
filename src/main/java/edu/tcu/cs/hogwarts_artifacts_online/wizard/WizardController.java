package edu.tcu.cs.hogwarts_artifacts_online.wizard;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.tcu.cs.hogwarts_artifacts_online.system.Result;
import edu.tcu.cs.hogwarts_artifacts_online.system.StatusCode;
import edu.tcu.cs.hogwarts_artifacts_online.wizard.converter.WizardDtoToWizardConverter;
import edu.tcu.cs.hogwarts_artifacts_online.wizard.converter.WizardToWizardDtoConverter;
import edu.tcu.cs.hogwarts_artifacts_online.wizard.dto.WizardDto;
import jakarta.validation.Valid;

@RestController
@RequestMapping("${api.endpoint.base-url}/wizards")
public class WizardController {

  private final WizardService wizardService;

  private final WizardToWizardDtoConverter wizardToWizardDtoConverter;
  private final WizardDtoToWizardConverter wizardDtoToWizardConverter;

  public WizardController(WizardService wizardService, WizardToWizardDtoConverter wizardToWizardDtoConverter, WizardDtoToWizardConverter wizardDtoToWizardConverter) {
    this.wizardService = wizardService;
    this.wizardToWizardDtoConverter = wizardToWizardDtoConverter;
    this.wizardDtoToWizardConverter = wizardDtoToWizardConverter;
  }

  @GetMapping("/{wizardId}")
  public Result findWizardById(@PathVariable Integer wizardId) {
    Wizard foundWizard = this.wizardService.findById(wizardId);
    WizardDto wizardDto = this.wizardToWizardDtoConverter.convert(foundWizard);
    return new Result(true, StatusCode.SUCCESS, "Find One Success", wizardDto);
  }

  @GetMapping
  public Result findAllWizards() {
    List<Wizard> foundWizards = this.wizardService.findAll();
    List<WizardDto> wizardDtos = foundWizards.stream().map(this.wizardToWizardDtoConverter::convert).collect(Collectors.toList());
    return new Result(true, StatusCode.SUCCESS, "Find All Success", wizardDtos);
  }

  @PostMapping
  public Result addWizard(@Valid @RequestBody WizardDto wizardDto) {
    Wizard savedWizard = this.wizardService.save(this.wizardDtoToWizardConverter.convert(wizardDto));
    WizardDto savedWizardDto = this.wizardToWizardDtoConverter.convert(savedWizard);
    return new Result(true, StatusCode.SUCCESS, "Add Success", savedWizardDto);
  }

  @PutMapping("/{wizardId}")
  public Result updateWizard(@PathVariable Integer wizardId, @Valid @RequestBody WizardDto wizardDto) {
    Wizard updatedWizard = this.wizardService.update(wizardId, this.wizardDtoToWizardConverter.convert(wizardDto));
    WizardDto updatedWizardDto = this.wizardToWizardDtoConverter.convert(updatedWizard);
    return new Result(true, StatusCode.SUCCESS, "Update Success", updatedWizardDto);
  }

  @DeleteMapping("/{wizardId}")
  public Result deleteWizard(@PathVariable Integer wizardId) {
    this.wizardService.delete(wizardId);
    return new Result(true, StatusCode.SUCCESS, "Delete Success");
  }

  @PutMapping("/{wizardId}/artifacts/{artifactId}")
  public Result assignArtifact(@PathVariable Integer wizardId, @PathVariable String artifactId) {
    this.wizardService.assignArtifact(wizardId, artifactId);
    return new Result(true, StatusCode.SUCCESS, "Artifact Assignment Success");
  }

}
