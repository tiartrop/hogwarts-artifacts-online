package edu.tcu.cs.hogwarts_artifacts_online.artifact;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.tcu.cs.hogwarts_artifacts_online.artifact.converter.ArtifactDtoToArtifactConverter;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.converter.ArtifactToArtifactDtoConverter;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.dto.ArtifactDto;
import edu.tcu.cs.hogwarts_artifacts_online.client.imagestorage.ImageStorageClient;
import edu.tcu.cs.hogwarts_artifacts_online.system.Result;
import edu.tcu.cs.hogwarts_artifacts_online.system.StatusCode;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Valid;

@RestController
@RequestMapping("${api.endpoint.base-url}/artifacts")
public class ArtifactController {

  private final ArtifactService artifactService;

  private final ArtifactToArtifactDtoConverter artifactToArtifactDtoConverter;
  private final ArtifactDtoToArtifactConverter artifactDtoToArtifactConverter;

  private final MeterRegistry meterRegistry;

  private final ImageStorageClient imageStorageClient;

  public ArtifactController(ArtifactService artifactService,
                            ArtifactToArtifactDtoConverter artifactToArtifactDtoConverter,
                            ArtifactDtoToArtifactConverter artifactDtoToArtifactConverter,
                            MeterRegistry meterRegistry,
                            ImageStorageClient imageStorageClient) {
    this.artifactService = artifactService;
    this.artifactToArtifactDtoConverter = artifactToArtifactDtoConverter;
    this.artifactDtoToArtifactConverter = artifactDtoToArtifactConverter;
    this.meterRegistry = meterRegistry;
    this.imageStorageClient = imageStorageClient;
  }

  @GetMapping("/{artifactId}")
  public Result findArtifactById(@PathVariable String artifactId) {
    Artifact foundArtifact = this.artifactService.findById(artifactId);
    meterRegistry.counter("artifact.id." + artifactId).increment();
    ArtifactDto artifactDto = this.artifactToArtifactDtoConverter.convert(foundArtifact);
    return new Result(true, StatusCode.SUCCESS, "Find One Success", artifactDto);
  }

  @GetMapping
  public Result findAllArtifacts(Pageable pageable) {
    Page<Artifact> artifactPage = this.artifactService.findAll(pageable);
    Page<ArtifactDto> artifactDtoPage = artifactPage.map(this.artifactToArtifactDtoConverter::convert);
    return new Result(true, StatusCode.SUCCESS, "Find All Success", artifactDtoPage);
  }

  @PostMapping
  public Result addArtifact(@Valid @RequestBody ArtifactDto artifactDto) {
    Artifact savedArtifact = this.artifactService.save(this.artifactDtoToArtifactConverter.convert(artifactDto));
    ArtifactDto savedArtifactDto = this.artifactToArtifactDtoConverter.convert(savedArtifact);
    return new Result(true, StatusCode.SUCCESS, "Add Success", savedArtifactDto);
  }

  @PutMapping("/{artifactId}")
  public Result updateArtifact(@PathVariable String artifactId, @Valid @RequestBody ArtifactDto artifactDto) {
    Artifact updatedArtifact = this.artifactService.update(artifactId, this.artifactDtoToArtifactConverter.convert(artifactDto));
    ArtifactDto updatedArtifactDto = this.artifactToArtifactDtoConverter.convert(updatedArtifact);
    return new Result(true, StatusCode.SUCCESS, "Update Success", updatedArtifactDto);
  }

  @DeleteMapping("/{artifactId}")
  public Result deleteArtifact(@PathVariable String artifactId) {
    this.artifactService.delete(artifactId);
    return new Result(true, StatusCode.SUCCESS, "Delete Success");
  }

  @GetMapping("/summary")
  public Result summarizeArtifacts() throws JsonProcessingException {
    List<Artifact> foundArtifacts = this.artifactService.findAll();
    List<ArtifactDto> artifactDtos = foundArtifacts.stream().map(this.artifactToArtifactDtoConverter::convert).collect(Collectors.toList());
    String summarize = this.artifactService.summarize(artifactDtos);
    return new Result(true, StatusCode.SUCCESS, "Summarize Success", summarize);
  }

  @PostMapping("/search")
  public Result findArtifactsByCriteria(@RequestBody Map<String, String> searchCriteria, Pageable pageable) throws JsonProcessingException {
    Page<Artifact> artifactPage = this.artifactService.findByCriteria(searchCriteria, pageable);
    Page<ArtifactDto> artifactDtoPage = artifactPage.map(this.artifactToArtifactDtoConverter::convert);

    return new Result(true, StatusCode.SUCCESS, "Search Success", artifactDtoPage);
  }

  @PostMapping("/images")
  public Result uploadImage(@RequestParam String containerName,@RequestParam MultipartFile file) throws IOException {
    try (InputStream inputStream = file.getInputStream()) {
      String imageUrl = this.imageStorageClient.uploadImage(containerName, file.getOriginalFilename(), inputStream, file.getSize());
      return new Result(true, StatusCode.SUCCESS, "Upload Image Success", imageUrl);
    }
  }

}
