package fr.lucasmacori.ai_tools_api.chat.infrastructure.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Component;

import fr.lucasmacori.ai_tools_api.chat.domain.spi.ChatGenerator;
import reactor.core.publisher.Flux;

@Component
public class OllamaChatGenerator implements ChatGenerator {

	private final ChatClient chatClient;

	public OllamaChatGenerator(ChatModel chatModel, ChatMemory chatMemory) {
		this.chatClient = ChatClient.builder(chatModel)
				.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
				.build();
	}

	@Override
	public Flux<String> stream(String chatId, String systemPrompt, String userMessage, String model) {
		return chatClient.prompt()
				.advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, chatId))
				.options(OllamaChatOptions.builder().model(model).build())
				.system(systemPrompt)
				.user(userMessage)
				.stream()
				.content();
	}
}
