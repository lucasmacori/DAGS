package fr.lucasmacori.ai_tools_api.chat.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fr.lucasmacori.ai_tools_api.chat.domain.service.ChatService;
import fr.lucasmacori.ai_tools_api.chat.domain.spi.ChatGenerator;

@Configuration
@EnableConfigurationProperties(ChatPromptProperties.class)
class ChatConfiguration {

	@Bean
	ChatService chatService(ChatGenerator chatGenerator, ChatPromptProperties chatPromptProperties) {
		return new ChatService(chatGenerator, chatPromptProperties.defaultModel(), chatPromptProperties.system());
	}
}
