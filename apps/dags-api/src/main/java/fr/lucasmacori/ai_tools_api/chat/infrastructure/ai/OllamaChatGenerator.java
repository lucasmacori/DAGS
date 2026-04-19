package fr.lucasmacori.ai_tools_api.chat.infrastructure.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Component;

import fr.lucasmacori.ai_tools_api.chat.domain.spi.ChatGenerator;
import reactor.core.publisher.Flux;

@Component
public class OllamaChatGenerator implements ChatGenerator {

	private final ChatClient chatClient;

	public OllamaChatGenerator(ChatModel chatModel) {
		this.chatClient = ChatClient.create(chatModel);
	}

	@Override
	public Flux<String> stream(String systemPrompt, String userPrompt, String model) {
		return chatClient.prompt()
				.options(OllamaChatOptions.builder().model(model).build())
				.system(systemPrompt)
				.user(userPrompt)
				.stream()
				.content();
	}
}
