package fr.lucasmacori.ai_tools_api.chat.domain.model;

import java.util.List;

public record ChatRequest(String chatId, String message, String model, List<String> documentIds) {
}
