package fr.lucasmacori.ai_tools_api.translation.infrastructure.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import fr.lucasmacori.ai_tools_api.translation.domain.spi.TranslationGenerator;
import fr.lucasmacori.ai_tools_api.translation.infrastructure.config.TranslationPromptProperties;
import reactor.core.publisher.Flux;

@Component
public class OllamaTranslationGenerator implements TranslationGenerator {

	private final ChatClient chatClient;
	private final TranslationPromptProperties promptProperties;

	public OllamaTranslationGenerator(ChatModel chatModel, TranslationPromptProperties promptProperties) {
		this.chatClient = ChatClient.create(chatModel);
		this.promptProperties = promptProperties;
	}

	@Override
	public Flux<String> stream(String userPrompt) {
		return chatClient.prompt()
				.system(promptProperties.system())
				.user(userPrompt)
				.stream()
				.content();
	}
}
