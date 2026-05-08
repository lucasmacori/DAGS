package fr.lucasmacori.ai_tools_api.briefing.infrastructure.repository.jdbc;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import fr.lucasmacori.ai_tools_api.briefing.infrastructure.entity.UserBriefingSettingsEntity;

@Repository
public interface UserBriefingSettingsJDBCRepository extends ListCrudRepository<UserBriefingSettingsEntity, UUID> {
	Optional<UserBriefingSettingsEntity> findByUserId(UUID userId);
}
