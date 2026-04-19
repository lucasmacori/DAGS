package fr.lucasmacori.ai_tools_api.chat.domain.model;

import java.time.LocalDateTime;

public record Conversation(String conversationId, String conversationName, LocalDateTime createdAt) {}
