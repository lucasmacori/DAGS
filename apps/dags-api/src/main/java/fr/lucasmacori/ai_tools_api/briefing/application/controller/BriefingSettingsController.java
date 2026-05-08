package fr.lucasmacori.ai_tools_api.briefing.application.controller;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.lucasmacori.ai_tools_api.briefing.application.dto.BriefingSettingsResponse;
import fr.lucasmacori.ai_tools_api.briefing.application.dto.UpdateBriefingSettingsRequestBody;
import fr.lucasmacori.ai_tools_api.briefing.application.service.BriefingSettingsApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/briefing/settings")
@RequiredArgsConstructor
class BriefingSettingsController {

	private final BriefingSettingsApplicationService applicationService;

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	BriefingSettingsResponse getSettings(@AuthenticationPrincipal Jwt jwt) {
		String userId = jwt.getSubject();
		return BriefingSettingsResponse.from(applicationService.getSettings(userId));
	}

	@PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	BriefingSettingsResponse updateSettings(@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody UpdateBriefingSettingsRequestBody request) {
		String userId = jwt.getSubject();
		return BriefingSettingsResponse.from(applicationService.updateSettings(userId, request));
	}
}
