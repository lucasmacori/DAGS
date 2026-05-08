package fr.lucasmacori.ai_tools_api.briefing.infrastructure.repository.jdbc;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fr.lucasmacori.ai_tools_api.briefing.infrastructure.entity.SourceEntity;

@Repository
public interface SourceJDBCRepository extends ListCrudRepository<SourceEntity, UUID> {
	@Override
	List<SourceEntity> findAll();

	List<SourceEntity> findByUserId(UUID userId);

	List<SourceEntity> findByType(String type);

	@Query("SELECT * FROM source WHERE type = :type AND article_read_at IS NULL AND user_id = :userId")
	List<SourceEntity> findByTypeAndArticleReadAtIsNullAndUserId(@Param("type") String type, @Param("userId") UUID userId);

	List<SourceEntity> findByTypeAndArticleReadAtIsNull(String type);
}
