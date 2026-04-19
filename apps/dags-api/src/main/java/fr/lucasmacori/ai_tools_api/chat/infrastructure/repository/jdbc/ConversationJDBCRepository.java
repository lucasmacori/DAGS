package fr.lucasmacori.ai_tools_api.chat.infrastructure.repository.jdbc;

import java.util.List;
import java.util.UUID;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import fr.lucasmacori.ai_tools_api.chat.infrastructure.entity.ConversationEntity;

@Repository
public interface ConversationJDBCRepository extends ListCrudRepository<ConversationEntity, UUID> {
	@Override
	List<ConversationEntity> findAll();

	@Override
	<S extends ConversationEntity> S save(S entity);
}
