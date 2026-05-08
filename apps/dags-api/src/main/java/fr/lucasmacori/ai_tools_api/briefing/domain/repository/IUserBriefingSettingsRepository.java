package fr.lucasmacori.ai_tools_api.briefing.domain.repository;

import java.util.Optional;

import fr.lucasmacori.ai_tools_api.briefing.domain.model.UserBriefingSettings;

public interface IUserBriefingSettingsRepository {
	Optional<UserBriefingSettings> findByUserId(String userId);

	UserBriefingSettings save(UserBriefingSettings settings);
}
