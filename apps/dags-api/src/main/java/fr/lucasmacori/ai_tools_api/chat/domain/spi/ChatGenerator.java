package fr.lucasmacori.ai_tools_api.chat.domain.spi;

import reactor.core.publisher.Flux;

public interface ChatGenerator {

	Flux<String> stream(String systemPrompt, String userPrompt, String model);
}
