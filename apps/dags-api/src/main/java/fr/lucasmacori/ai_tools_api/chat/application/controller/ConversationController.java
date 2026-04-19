package fr.lucasmacori.ai_tools_api.chat.application.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.lucasmacori.ai_tools_api.chat.application.dto.CreateConversationRequestBody;
import fr.lucasmacori.ai_tools_api.chat.application.dto.GenerateChatResponse;
import fr.lucasmacori.ai_tools_api.chat.application.service.ChatApplicationService;
import fr.lucasmacori.ai_tools_api.chat.domain.model.Conversation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/conversation")
@RequiredArgsConstructor
public class ConversationController {
	private final ChatApplicationService applicationService;

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	List<Conversation> getConversations() {
		return applicationService.getConversations();
	}

	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	GenerateChatResponse createConversation(@Valid @RequestBody CreateConversationRequestBody request) {
		return new GenerateChatResponse(applicationService.createConversation(request.name()).conversationId());
	}
}
