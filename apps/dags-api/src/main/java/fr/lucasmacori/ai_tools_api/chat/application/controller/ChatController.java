package fr.lucasmacori.ai_tools_api.chat.application.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import fr.lucasmacori.ai_tools_api.chat.application.dto.ChatRequestBody;
import fr.lucasmacori.ai_tools_api.chat.application.dto.GenerateChatResponse;
import fr.lucasmacori.ai_tools_api.chat.application.service.ChatApplicationService;
import jakarta.validation.Valid;
import reactor.core.publisher.Flux;

@RestController
class ChatController {

	private final ChatApplicationService applicationService;

	ChatController(ChatApplicationService applicationService) {
		this.applicationService = applicationService;
	}

	@PostMapping(path = "/chat", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	Flux<String> chat(@Valid @RequestBody ChatRequestBody request) {
		return applicationService.chat(request);
	}

	@PostMapping(path = "/generate-chat", produces = MediaType.APPLICATION_JSON_VALUE)
	GenerateChatResponse generateChat() {
		return new GenerateChatResponse(applicationService.generateChatId().toString());
	}
}
