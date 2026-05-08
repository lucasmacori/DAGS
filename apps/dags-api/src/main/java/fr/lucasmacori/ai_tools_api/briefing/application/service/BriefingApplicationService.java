package fr.lucasmacori.ai_tools_api.briefing.application.service;

import org.springframework.stereotype.Service;

import fr.lucasmacori.ai_tools_api.briefing.domain.model.Briefing;
import fr.lucasmacori.ai_tools_api.briefing.domain.service.BriefingGenerationService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BriefingApplicationService {

	private final BriefingGenerationService briefingGenerationService;

	public Briefing generateBriefing(String userId) {
		return briefingGenerationService.generateBriefing(userId);
	}
}
