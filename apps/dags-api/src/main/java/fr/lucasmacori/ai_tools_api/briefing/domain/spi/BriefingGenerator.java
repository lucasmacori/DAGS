package fr.lucasmacori.ai_tools_api.briefing.domain.spi;

public interface BriefingGenerator {
	String generate(String systemPrompt, String userMessage, String model);
}
