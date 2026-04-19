package fr.lucasmacori.ai_tools_api.chat.application.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.lucasmacori.ai_tools_api.chat.application.dto.ChatRequestBody;
import fr.lucasmacori.ai_tools_api.chat.application.service.ChatApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
class ChatController {
	private final ChatApplicationService applicationService;

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	Flux<String> chat(@Valid @RequestBody ChatRequestBody request) {
		return applicationService.chat(request);
	}
}
