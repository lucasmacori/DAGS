package fr.lucasmacori.ai_tools_api.briefing.domain.service;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import fr.lucasmacori.ai_tools_api.briefing.domain.model.Source;
import fr.lucasmacori.ai_tools_api.briefing.domain.model.SourceType;
import fr.lucasmacori.ai_tools_api.briefing.domain.repository.ISourceRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SourceService {

	private final ISourceRepository sourceRepository;

	public List<Source> getSources() {
		return sourceRepository.findAll();
	}

	public List<Source> getUnreadArticleSources() {
		return sourceRepository.findUnreadArticleSources();
	}

	public void markArticleAsRead(String sourceId) {
		sourceRepository.markArticleAsRead(sourceId);
	}

	public Source create(SourceType type, String title, String content) {
		String normalizedTitle = normalizeTitle(title);
		String normalizedContent = normalizeRequiredText(content, "content");
		validateContent(type, normalizedContent);
		LocalDateTime now = LocalDateTime.now();

		Source source = new Source(
				UUID.randomUUID().toString(),
				type,
				normalizedTitle,
				normalizedContent,
				now,
				now,
				null);
		return sourceRepository.create(source);
	}

	public Optional<Source> update(String sourceId, SourceType type, String title, String content) {
		return sourceRepository.findById(sourceId)
				.map(existing -> {
					SourceType nextType = type != null ? type : existing.type();
					String nextTitle = title != null ? normalizeTitle(title) : existing.title();
					String nextContent = content != null ? normalizeRequiredText(content, "content") : existing.content();
					validateContent(nextType, nextContent);
					Source updated = new Source(
							existing.sourceId(),
							nextType,
							nextTitle,
							nextContent,
							existing.createdAt(),
							LocalDateTime.now(),
							existing.articleReadAt());
					return sourceRepository.update(updated);
				});
	}

	public boolean delete(String sourceId) {
		Optional<Source> existing = sourceRepository.findById(sourceId);
		if (existing.isEmpty()) {
			return false;
		}

		sourceRepository.deleteById(sourceId);
		return true;
	}

	private void validateContent(SourceType type, String content) {
		if (type == null) {
			throw new IllegalArgumentException("type is required");
		}

		if (type == SourceType.PLAIN_TEXT) {
			return;
		}

		validateUrl(content);
	}

	private void validateUrl(String content) {
		try {
			URI uri = URI.create(content);
			if (uri.getScheme() == null || uri.getHost() == null) {
				throw new IllegalArgumentException("content must be a valid URL");
			}
		}
		catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("content must be a valid URL", exception);
		}
	}

	private String normalizeRequiredText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " is required");
		}

		return value.trim();
	}

	private String normalizeTitle(String title) {
		if (title == null) {
			return null;
		}

		String normalized = title.trim();
		return normalized.isEmpty() ? null : normalized;
	}
}
