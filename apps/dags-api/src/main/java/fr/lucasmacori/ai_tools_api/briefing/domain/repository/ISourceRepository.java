package fr.lucasmacori.ai_tools_api.briefing.domain.repository;

import java.util.List;
import java.util.Optional;

import fr.lucasmacori.ai_tools_api.briefing.domain.model.Source;
import fr.lucasmacori.ai_tools_api.briefing.domain.model.SourceType;

public interface ISourceRepository {
	List<Source> findAllByUserId(String userId);

	List<Source> findByTypeAndUserId(SourceType type, String userId);

	List<Source> findUnreadArticleSourcesByUserId(String userId);

	Source create(Source source);

	Optional<Source> findById(String sourceId);

	Source update(Source source);

	void markArticleAsRead(String sourceId);

	void storeArticleContent(String sourceId, String articleContent);

	void markAsSummarized(String sourceId);

	void deleteById(String sourceId);
}
