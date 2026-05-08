package fr.lucasmacori.ai_tools_api.briefing.application.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import fr.lucasmacori.ai_tools_api.briefing.application.dto.UpdateBriefingSettingsRequestBody;
import fr.lucasmacori.ai_tools_api.briefing.domain.model.UserBriefingSettings;
import fr.lucasmacori.ai_tools_api.briefing.domain.service.BriefingSettingsService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BriefingSettingsApplicationService {

	private final BriefingSettingsService briefingSettingsService;

	public UserBriefingSettings getSettings(String userId) {
		return briefingSettingsService.getSettings(userId);
	}

	public UserBriefingSettings updateSettings(String userId, UpdateBriefingSettingsRequestBody request) {
		if (!request.hasUpdates()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one field must be provided");
		}

		try {
			return briefingSettingsService.updateSettings(
					userId,
					request.enabled(),
					request.frequency(),
					request.generationTime(),
					request.systemPrompt());
		}
		catch (IllegalArgumentException exception) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
		}
	}
}
