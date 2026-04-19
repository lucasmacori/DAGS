package fr.lucasmacori.ai_tools_api.chat.domain.model;

import java.util.List;

public record ConversationHistoryPage(int page, int size, List<ConversationMessage> messages) {
}
