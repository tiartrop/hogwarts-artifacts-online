package edu.tcu.cs.hogwarts_artifacts_online.client.ai.chat;

import edu.tcu.cs.hogwarts_artifacts_online.client.ai.chat.dto.ChatRequest;
import edu.tcu.cs.hogwarts_artifacts_online.client.ai.chat.dto.ChatResponse;

public interface ChatClient {

  ChatResponse generate(ChatRequest chatRequest);

}
