package fr.lucasmacori.ai_tools_api.briefing.domain.repository;

import java.util.List;
import java.util.Optional;

import fr.lucasmacori.ai_tools_api.briefing.domain.model.Source;

public interface ISourceRepository {
	List<Source> findAll();

	List<Source> findByType(fr.lucasmacori.ai_tools_api.briefing.domain.model.SourceType type);

	List<Source> findUnreadArticleSources();

	Source create(Source source);

	Optional<Source> findById(String sourceId);

	Source update(Source source);

	void markArticleAsRead(String sourceId);

	void deleteById(String sourceId);
}
