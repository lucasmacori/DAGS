package fr.lucasmacori.ai_tools_api.briefing.infrastructure.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Component;

import fr.lucasmacori.ai_tools_api.briefing.domain.spi.BriefingGenerator;

@Component
public class OllamaBriefingGenerator implements BriefingGenerator {

	private final ChatModel chatModel;

	public OllamaBriefingGenerator(ChatModel chatModel) {
		this.chatModel = chatModel;
	}

	@Override
	public String generate(String systemPrompt, String userMessage, String model) {
		return ChatClient.builder(chatModel)
				.build()
				.prompt()
				.options(OllamaChatOptions.builder().model(model).build())
				.system(systemPrompt)
				.user(userMessage)
				.call()
				.content();
	}
}
