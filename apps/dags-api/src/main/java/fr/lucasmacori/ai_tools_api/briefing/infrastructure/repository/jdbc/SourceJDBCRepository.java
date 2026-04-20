package fr.lucasmacori.ai_tools_api.briefing.infrastructure.repository.jdbc;

import java.util.List;
import java.util.UUID;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import fr.lucasmacori.ai_tools_api.briefing.infrastructure.entity.SourceEntity;

@Repository
public interface SourceJDBCRepository extends ListCrudRepository<SourceEntity, UUID> {
	@Override
	List<SourceEntity> findAll();

	List<SourceEntity> findByType(String type);

	List<SourceEntity> findByTypeAndArticleReadAtIsNull(String type);
}
