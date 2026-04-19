package fr.lucasmacori.ai_tools_api.chat.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GenerateChatResponse(@JsonProperty("chat_id") String chatId) {
}
