package edu.tcu.cs.hogwarts_artifacts_online.client.ai.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServiceUnavailable;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withTooManyRequests;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tcu.cs.hogwarts_artifacts_online.client.ai.chat.dto.ChatRequest;
import edu.tcu.cs.hogwarts_artifacts_online.client.ai.chat.dto.ChatResponse;
import edu.tcu.cs.hogwarts_artifacts_online.client.ai.chat.dto.Choice;
import edu.tcu.cs.hogwarts_artifacts_online.client.ai.chat.dto.Message;

@RestClientTest(GeminiChatClient.class)
public class GeminiChatClientTest {

  @Autowired
  private GeminiChatClient geminiChatClient;

  @Autowired
  private MockRestServiceServer mockServer;

  @Autowired
  private ObjectMapper objectMapper;

  @Value("${ai.gemini.endpoint}")
  private String url;

  private ChatRequest chatRequest;

  @BeforeEach
  void setUp() {

    this.chatRequest = new ChatRequest("gemini-2.0-flash", List.of(
      new Message("system", "Your task is to generate a short summary of a given JSON array in at most 100 words. The summary must include the number of artifacts, each artifact's description, and the ownership information. Don't mention that the summary is from a given JSON array."),
      new Message("user", "A json array.")
    ));
  }

  @Test
  void testGenerateSuccess() throws JsonProcessingException {
    // Given
    ChatResponse chatResponse = new ChatResponse(List.of(
      new Choice(0, new Message("assistant", "The summary includes six artifacts, owned by three different wizards."))
    ));
    this.mockServer.expect(requestTo(this.url))
        .andExpect(method(HttpMethod.POST)) // Check whether this is a POST request.
        .andExpect(content().json(this.objectMapper.writeValueAsString(this.chatRequest))) // Check whether the chatRequest is sent.
        .andRespond(withSuccess(this.objectMapper.writeValueAsString(chatResponse), MediaType.APPLICATION_JSON)); // The mock OpenAI server should respond this json message.

    // When
    ChatResponse generatedChatResponse = this.geminiChatClient.generate(this.chatRequest);

    // Then:
    this.mockServer.verify(); // Verify that all expected requests set up vid expect and andExpect were indeed performed.
    assertThat(generatedChatResponse.choices().get(0).message().content()).isEqualTo("The summary includes six artifacts, owned by three different wizards.");
  }

  @Test
  void testGenerateUnauthorizedRequest() throws JsonProcessingException {
    // Given
    this.mockServer.expect(requestTo(this.url))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Authorization", startsWith("Bearer "))) // Check whether the header is set correctly.
        .andExpect(content().json(this.objectMapper.writeValueAsString(this.chatRequest)))
        .andRespond(withUnauthorizedRequest());

    // When
    Throwable thrown = catchThrowable(() -> {
      this.geminiChatClient.generate(this.chatRequest);
    });

    // Then:
    this.mockServer.verify(); // Verify that all expected requests set up vid expect and andExpect were indeed performed.
    assertThat(thrown).isInstanceOf(HttpClientErrorException.Unauthorized.class);
  }

  @Test
    void testGenerateQuotaExceeded() {
        // Given
        this.mockServer.expect(requestTo(this.url))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withTooManyRequests());

        // When
        Throwable thrown = catchThrowable(() -> {
          this.geminiChatClient.generate(chatRequest);
        });

        // Then
        this.mockServer.verify(); // Verify that all expected requests set up via expect(RequestMatcher) were indeed performed.
        assertThat(thrown).isInstanceOf(HttpClientErrorException.TooManyRequests.class);
    }

    @Test
    void testGenerateServerError() {
        // Given
        this.mockServer.expect(requestTo(this.url))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withServerError());

        // When
        Throwable thrown = catchThrowable(() -> {
          this.geminiChatClient.generate(chatRequest);
        });

        // Then
        this.mockServer.verify(); // Verify that all expected requests set up via expect(RequestMatcher) were indeed performed.
        assertThat(thrown).isInstanceOf(HttpServerErrorException.InternalServerError.class);
    }

    @Test
    void testGenerateServerOverloaded() {
        // Given
        this.mockServer.expect(requestTo(this.url))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withServiceUnavailable());

        // When
        Throwable thrown = catchThrowable(() -> {
          this.geminiChatClient.generate(chatRequest);
        });

        // Then
        this.mockServer.verify(); // Verify that all expected requests set up via expect(RequestMatcher) were indeed performed.
        assertThat(thrown).isInstanceOf(HttpServerErrorException.ServiceUnavailable.class);
    }

}
