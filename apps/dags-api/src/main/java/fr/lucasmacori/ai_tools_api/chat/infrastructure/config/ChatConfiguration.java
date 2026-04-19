package fr.lucasmacori.ai_tools_api.chat.infrastructure.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fr.lucasmacori.ai_tools_api.chat.domain.repository.IConversationHistoryRepository;
import fr.lucasmacori.ai_tools_api.chat.domain.repository.IConversationRepository;
import fr.lucasmacori.ai_tools_api.chat.domain.service.ChatService;
import fr.lucasmacori.ai_tools_api.chat.domain.spi.ChatGenerator;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableConfigurationProperties(ChatPromptProperties.class)
@RequiredArgsConstructor
class ChatConfiguration {

	private final IConversationRepository conversationRepository;
	private final IConversationHistoryRepository conversationHistoryRepository;

	@Bean
	ChatService chatService(ChatGenerator chatGenerator, ChatPromptProperties chatPromptProperties) {
		return new ChatService(
				chatGenerator,
				chatPromptProperties.defaultModel(),
				chatPromptProperties.system(),
				conversationRepository,
				conversationHistoryRepository);
	}

	@Bean
	ChatMemoryRepository chatMemoryRepository(ChatPromptProperties chatPromptProperties) {
		String provider = chatPromptProperties.memoryProvider().trim().toLowerCase();

		if (!"in-memory".equals(provider)) {
			throw new IllegalStateException("Unsupported chat memory provider: " + chatPromptProperties.memoryProvider());
		}

		return new InMemoryChatMemoryRepository();
	}

	@Bean
	ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository, ChatPromptProperties chatPromptProperties) {
		return MessageWindowChatMemory.builder()
				.chatMemoryRepository(chatMemoryRepository)
				.maxMessages(chatPromptProperties.maxMemoryMessages())
				.build();
	}
}
