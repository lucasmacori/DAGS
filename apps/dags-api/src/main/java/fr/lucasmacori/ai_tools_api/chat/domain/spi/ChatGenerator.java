package fr.lucasmacori.ai_tools_api.chat.domain.spi;

import reactor.core.publisher.Flux;

public interface ChatGenerator {

	Flux<String> stream(String chatId, String systemPrompt, String userMessage, String model);
}
