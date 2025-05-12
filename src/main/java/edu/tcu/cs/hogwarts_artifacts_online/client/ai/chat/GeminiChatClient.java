package edu.tcu.cs.hogwarts_artifacts_online.client.ai.chat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import edu.tcu.cs.hogwarts_artifacts_online.client.ai.chat.dto.ChatRequest;
import edu.tcu.cs.hogwarts_artifacts_online.client.ai.chat.dto.ChatResponse;

@Component
public class GeminiChatClient implements ChatClient {

  private final RestClient restClient;

  public GeminiChatClient(@Value("${ai.gemini.endpoint}") String endpoint,
                          @Value("${ai.gemini.api-key}") String apiKey,
                          RestClient.Builder restClientBuilder) {
    this.restClient = restClientBuilder.baseUrl(endpoint).defaultHeader("Authorization", "Bearer " + apiKey).build();
  }

  @Override
  public ChatResponse generate(ChatRequest chatRequest) {

    return this.restClient
               .post()
               .contentType(MediaType.APPLICATION_JSON)
               .body(chatRequest)
               .retrieve()
               .body(ChatResponse.class);
  }

}
