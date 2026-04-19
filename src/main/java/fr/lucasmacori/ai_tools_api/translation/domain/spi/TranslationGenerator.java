package fr.lucasmacori.ai_tools_api.translation.domain.spi;

import reactor.core.publisher.Flux;

public interface TranslationGenerator {

	Flux<String> stream(String userPrompt);
}
