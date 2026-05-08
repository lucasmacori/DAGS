package fr.lucasmacori.ai_tools_api.briefing.application.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import fr.lucasmacori.ai_tools_api.briefing.application.dto.CreateSourceRequestBody;
import fr.lucasmacori.ai_tools_api.briefing.application.dto.SourceResponse;
import fr.lucasmacori.ai_tools_api.briefing.application.dto.UpdateSourceRequestBody;
import fr.lucasmacori.ai_tools_api.briefing.application.service.ArticleReadApplicationService;
import fr.lucasmacori.ai_tools_api.briefing.application.service.RssFeedReadApplicationService;
import fr.lucasmacori.ai_tools_api.briefing.application.service.SourceApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/source")
@RequiredArgsConstructor
class SourceController {

	private final SourceApplicationService applicationService;
	private final RssFeedReadApplicationService rssFeedReadApplicationService;
	private final ArticleReadApplicationService articleReadApplicationService;

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	List<SourceResponse> getSources(@AuthenticationPrincipal Jwt jwt) {
		String userId = jwt.getSubject();
		return applicationService.getSources(userId).stream().map(SourceResponse::from).toList();
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	SourceResponse createSource(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateSourceRequestBody request) {
		String userId = jwt.getSubject();
		return SourceResponse.from(applicationService.createSource(request, userId));
	}

	@PostMapping(path = "/rss/read")
	@ResponseStatus(HttpStatus.ACCEPTED)
	void readRssFeeds(@AuthenticationPrincipal Jwt jwt) {
		String userId = jwt.getSubject();
		rssFeedReadApplicationService.triggerReadAllFeeds(userId);
	}

	@PostMapping(path = "/articles/read")
	@ResponseStatus(HttpStatus.ACCEPTED)
	void readArticles(@AuthenticationPrincipal Jwt jwt) {
		String userId = jwt.getSubject();
		articleReadApplicationService.triggerReadAllArticles(userId);
	}

	@PatchMapping(path = "/{sourceId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	SourceResponse updateSource(@PathVariable String sourceId, @RequestBody UpdateSourceRequestBody request) {
		return SourceResponse.from(applicationService.updateSource(sourceId, request));
	}

	@DeleteMapping(path = "/{sourceId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void deleteSource(@PathVariable String sourceId) {
		applicationService.deleteSource(sourceId);
	}
}
