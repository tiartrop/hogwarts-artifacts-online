package edu.tcu.cs.hogwarts_artifacts_online.artifact.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import edu.tcu.cs.hogwarts_artifacts_online.artifact.Artifact;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.dto.ArtifactDto;

@Component
public class ArtifactDtoToArtifactConverter implements Converter<ArtifactDto, Artifact> {

  @Override
  public Artifact convert(ArtifactDto source) {
    Artifact artifact = new Artifact();
    artifact.setId(source.id());
    artifact.setName(source.name());
    artifact.setDescription(source.description());
    artifact.setImageUrl(source.imageUrl());
    return artifact;
  }

}
