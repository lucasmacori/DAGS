package fr.lucasmacori.ai_tools_api.briefing.infrastructure.repository.jdbc;

import java.util.UUID;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import fr.lucasmacori.ai_tools_api.briefing.infrastructure.entity.BriefingEntity;

@Repository
public interface BriefingJDBCRepository extends ListCrudRepository<BriefingEntity, UUID> {
}
