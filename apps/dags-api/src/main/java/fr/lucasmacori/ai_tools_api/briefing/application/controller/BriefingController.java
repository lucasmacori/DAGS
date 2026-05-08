package fr.lucasmacori.ai_tools_api.briefing.application.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import fr.lucasmacori.ai_tools_api.briefing.application.dto.BriefingResponse;
import fr.lucasmacori.ai_tools_api.briefing.application.service.BriefingApplicationService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/v1/briefing")
@RequiredArgsConstructor
class BriefingController {

	private final BriefingApplicationService applicationService;

	@PostMapping(path = "/generate", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	Mono<BriefingResponse> generateBriefing(@AuthenticationPrincipal Jwt jwt) {
		String userId = jwt.getSubject();
		return Mono.fromCallable(() -> BriefingResponse.from(applicationService.generateBriefing(userId)))
				.subscribeOn(Schedulers.boundedElastic());
	}
}
