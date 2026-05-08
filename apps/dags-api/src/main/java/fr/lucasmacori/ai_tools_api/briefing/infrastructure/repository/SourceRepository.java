package fr.lucasmacori.ai_tools_api.briefing.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import fr.lucasmacori.ai_tools_api.briefing.domain.model.Source;
import fr.lucasmacori.ai_tools_api.briefing.domain.model.SourceType;
import fr.lucasmacori.ai_tools_api.briefing.domain.repository.ISourceRepository;
import fr.lucasmacori.ai_tools_api.briefing.infrastructure.entity.SourceEntity;
import fr.lucasmacori.ai_tools_api.briefing.infrastructure.repository.jdbc.SourceJDBCRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SourceRepository implements ISourceRepository {

	private final SourceJDBCRepository sourceJDBCRepository;

	@Override
	public List<Source> findAllByUserId(String userId) {
		return sourceJDBCRepository.findByUserId(UUID.fromString(userId)).stream().map(SourceEntity::toSource).toList();
	}

	@Override
	public List<Source> findByTypeAndUserId(SourceType type, String userId) {
		return sourceJDBCRepository.findByType(type.name()).stream()
				.filter(entity -> userId.equals(entity.toSource().userId()))
				.map(SourceEntity::toSource).toList();
	}

	@Override
	public List<Source> findUnreadArticleSourcesByUserId(String userId) {
		return sourceJDBCRepository.findByTypeAndArticleReadAtIsNullAndUserId("ARTICLE_URL", UUID.fromString(userId))
				.stream().map(SourceEntity::toSource).toList();
	}

	@Override
	public Source create(Source source) {
		return sourceJDBCRepository.save(SourceEntity.fromNewSource(source)).toSource();
	}

	@Override
	public Optional<Source> findById(String sourceId) {
		return sourceJDBCRepository.findById(UUID.fromString(sourceId)).map(SourceEntity::toSource);
	}

	@Override
	public Source update(Source source) {
		return sourceJDBCRepository.save(SourceEntity.fromExistingSource(source)).toSource();
	}

	@Override
	public void markArticleAsRead(String sourceId) {
		sourceJDBCRepository.findById(UUID.fromString(sourceId))
				.map(SourceEntity::toSource)
				.map(source -> new Source(
						source.sourceId(),
						source.type(),
						source.title(),
						source.content(),
						source.createdAt(),
						LocalDateTime.now(),
						LocalDateTime.now(),
						source.userId(),
						source.articleContent(),
						source.summarizedAt()))
				.ifPresent(updated -> sourceJDBCRepository.save(SourceEntity.fromExistingSource(updated)));
	}

	@Override
	public void storeArticleContent(String sourceId, String articleContent) {
		sourceJDBCRepository.findById(UUID.fromString(sourceId))
				.map(entity -> {
					var source = entity.toSource();
					return SourceEntity.fromExistingSource(new Source(
							source.sourceId(),
							source.type(),
							source.title(),
							source.content(),
							source.createdAt(),
							source.updatedAt(),
							source.articleReadAt(),
							source.userId(),
							articleContent,
							source.summarizedAt()));
				})
				.ifPresent(sourceJDBCRepository::save);
	}

	@Override
	public void markAsSummarized(String sourceId) {
		sourceJDBCRepository.findById(UUID.fromString(sourceId))
				.map(SourceEntity::toSource)
				.map(source -> new Source(
						source.sourceId(),
						source.type(),
						source.title(),
						source.content(),
						source.createdAt(),
						LocalDateTime.now(),
						source.articleReadAt(),
						source.userId(),
						source.articleContent(),
						LocalDateTime.now()))
				.ifPresent(updated -> sourceJDBCRepository.save(SourceEntity.fromExistingSource(updated)));
	}

	@Override
	public void deleteById(String sourceId) {
		sourceJDBCRepository.deleteById(UUID.fromString(sourceId));
	}
}
