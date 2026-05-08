package fr.lucasmacori.ai_tools_api.briefing.infrastructure.repository;

import org.springframework.stereotype.Repository;

import fr.lucasmacori.ai_tools_api.briefing.domain.model.Briefing;
import fr.lucasmacori.ai_tools_api.briefing.domain.repository.IBriefingRepository;
import fr.lucasmacori.ai_tools_api.briefing.infrastructure.entity.BriefingEntity;
import fr.lucasmacori.ai_tools_api.briefing.infrastructure.repository.jdbc.BriefingJDBCRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BriefingRepository implements IBriefingRepository {

	private final BriefingJDBCRepository jdbcRepository;

	@Override
	public Briefing save(Briefing briefing) {
		return jdbcRepository.save(BriefingEntity.fromBriefing(briefing)).toBriefing();
	}
}
