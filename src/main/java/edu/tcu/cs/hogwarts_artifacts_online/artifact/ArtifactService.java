package edu.tcu.cs.hogwarts_artifacts_online.artifact;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tcu.cs.hogwarts_artifacts_online.artifact.dto.ArtifactDto;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.utils.IdWorker;
import edu.tcu.cs.hogwarts_artifacts_online.client.ai.chat.ChatClient;
import edu.tcu.cs.hogwarts_artifacts_online.client.ai.chat.dto.ChatRequest;
import edu.tcu.cs.hogwarts_artifacts_online.client.ai.chat.dto.ChatResponse;
import edu.tcu.cs.hogwarts_artifacts_online.client.ai.chat.dto.Message;
import edu.tcu.cs.hogwarts_artifacts_online.system.exception.ObjectNotFoundException;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class ArtifactService {

  private final ArtifactRepository artifactRepository;

  private final IdWorker idWorker;

  private final ChatClient chatClient;

  public ArtifactService(ArtifactRepository artifactRepository, IdWorker idWorker, ChatClient chatClient) {
    this.artifactRepository = artifactRepository;
    this.idWorker = idWorker;
    this.chatClient = chatClient;
  }

  @Observed(name = "artifact", contextualName = "findByIdService")
  public Artifact findById(String artifactId) {
    return this.artifactRepository.findById(artifactId).orElseThrow(() -> new ObjectNotFoundException("artifact", artifactId));
  }

  @Timed("findAllArtifactsService.time")
  public List<Artifact> findAll() {
    return this.artifactRepository.findAll();
  }

  public Artifact save(Artifact newArtifact) {
    newArtifact.setId(idWorker.nextId() + "");
    return this.artifactRepository.save(newArtifact);
  }

  public Artifact update(String artifactId, Artifact update) {
    return this.artifactRepository.findById(artifactId)
                                  .map(oldArtifact -> {
                                    oldArtifact.setName(update.getName());
                                    oldArtifact.setDescription(update.getDescription());
                                    oldArtifact.setImageUrl(update.getImageUrl());
                                    return this.artifactRepository.save(oldArtifact);
                                  })
                                  .orElseThrow(() -> new ObjectNotFoundException("artifact", artifactId));
  }

  public void delete(String artifactId) {
    this.artifactRepository.findById(artifactId).orElseThrow(() -> new ObjectNotFoundException("artifact", artifactId));
    this.artifactRepository.deleteById(artifactId);
  }

  // Prepare the messages for summarizing.
  public String summarize(List<ArtifactDto> artifactDtos) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    String jsonArray = objectMapper.writeValueAsString(artifactDtos);

    ChatRequest chatRequest = new ChatRequest("gemini-2.0-flash", List.of(
      new Message("system", "Your task is to generate a short summary of a given JSON array in at most 100 words. The summary must include the number of artifacts, each artifact's description, and the ownership information. Don't mention that the summary is from a given JSON array."),
      new Message("user", jsonArray)
    ));

    ChatResponse chatResponse = this.chatClient.generate(chatRequest);

    return chatResponse.choices().get(0).message().content();
  }

}
