package fr.lucasmacori.ai_tools_api.translation.application.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.lucasmacori.ai_tools_api.translation.application.dto.TranslateTextRequest;
import fr.lucasmacori.ai_tools_api.translation.application.service.TranslateTextApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/translate")
@RequiredArgsConstructor
class TranslationController {
	private final TranslateTextApplicationService applicationService;

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	Flux<String> translate(@Valid @RequestBody TranslateTextRequest request) {
		return applicationService.translate(request);
	}
}
