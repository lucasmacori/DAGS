package fr.lucasmacori.ai_tools_api.briefing.domain.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import fr.lucasmacori.ai_tools_api.briefing.domain.model.UserBriefingSettings;
import fr.lucasmacori.ai_tools_api.briefing.domain.repository.IUserBriefingSettingsRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BriefingSettingsService {

	private static final String DEFAULT_SYSTEM_PROMPT = """
			You are a briefing assistant. Summarize the following articles concisely.
			Focus on key points, main findings, and actionable insights.
			Organize the summary in a clear, scannable format.
			""";

	private final IUserBriefingSettingsRepository settingsRepository;

	public UserBriefingSettings getSettings(String userId) {
		return settingsRepository.findByUserId(userId)
				.orElseGet(() -> createDefaultSettings(userId));
	}

	public UserBriefingSettings updateSettings(String userId, Boolean enabled, String frequency,
			String generationTime, String systemPrompt) {
		UserBriefingSettings existing = getSettings(userId);

		boolean nextEnabled = enabled != null ? enabled : existing.enabled();
		String nextFrequency = frequency != null ? frequency : existing.frequency();
		String nextGenerationTime = generationTime != null ? generationTime : existing.generationTime();
		String nextSystemPrompt = systemPrompt != null ? systemPrompt : existing.systemPrompt();

		UserBriefingSettings updated = new UserBriefingSettings(
				userId,
				nextEnabled,
				nextFrequency,
				nextGenerationTime,
				nextSystemPrompt,
				existing.createdAt(),
				LocalDateTime.now());

		return settingsRepository.save(updated);
	}

	private UserBriefingSettings createDefaultSettings(String userId) {
		LocalDateTime now = LocalDateTime.now();
		UserBriefingSettings defaults = new UserBriefingSettings(
				userId,
				false,
				"DAILY",
				"08:00",
				DEFAULT_SYSTEM_PROMPT,
				now,
				now);
		return settingsRepository.save(defaults);
	}
}
