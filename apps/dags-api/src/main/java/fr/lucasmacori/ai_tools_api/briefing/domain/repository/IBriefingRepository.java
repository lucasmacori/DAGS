package fr.lucasmacori.ai_tools_api.briefing.domain.repository;

import fr.lucasmacori.ai_tools_api.briefing.domain.model.Briefing;

public interface IBriefingRepository {
	Briefing save(Briefing briefing);
}
