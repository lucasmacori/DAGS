package fr.lucasmacori.ai_tools_api.briefing.domain.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.lucasmacori.ai_tools_api.briefing.domain.model.Briefing;
import fr.lucasmacori.ai_tools_api.briefing.domain.model.RssSourceItemLink;
import fr.lucasmacori.ai_tools_api.briefing.domain.model.Source;
import fr.lucasmacori.ai_tools_api.briefing.domain.model.SourceType;
import fr.lucasmacori.ai_tools_api.briefing.domain.model.UserBriefingSettings;
import fr.lucasmacori.ai_tools_api.briefing.domain.repository.IBriefingRepository;
import fr.lucasmacori.ai_tools_api.briefing.domain.repository.IRssSourceItemRepository;
import fr.lucasmacori.ai_tools_api.briefing.domain.spi.BriefingGenerator;
import fr.lucasmacori.ai_tools_api.briefing.infrastructure.article.ArticleContentClient;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BriefingGenerationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(BriefingGenerationService.class);

	@Value("${briefing.default-model:gemma:e4b}")
	private String defaultBriefingModel;

	private final SourceService sourceService;
	private final IRssSourceItemRepository rssSourceItemRepository;
	private final BriefingSettingsService briefingSettingsService;
	private final BriefingGenerator briefingGenerator;
	private final ArticleContentClient articleContentClient;
	private final IBriefingRepository briefingRepository;

	public Briefing generateBriefing(String userId) {
		List<ArticleEntry> articles = collectArticles(userId);

		if (articles.isEmpty()) {
			LOGGER.info("[BRIEFING] userId={} no new articles to summarize", userId);
			Briefing emptyBriefing = new Briefing(
					UUID.randomUUID().toString(),
					userId,
					"No new articles to summarize.",
					0,
					LocalDateTime.now());
			return briefingRepository.save(emptyBriefing);
		}

		UserBriefingSettings settings = briefingSettingsService.getSettings(userId);
		String systemPrompt = settings.systemPrompt();

		String userMessage = buildPrompt(articles);

		LOGGER.info("[BRIEFING] userId={} articles={} generating summary", userId, articles.size());

		String generatedContent;
		try {
			generatedContent = briefingGenerator.generate(systemPrompt, userMessage, defaultBriefingModel);
		}
		catch (RuntimeException exception) {
			LOGGER.error("[BRIEFING] userId={} generation failed", userId, exception);
			throw new RuntimeException("Failed to generate briefing", exception);
		}

		markArticlesAsSummarized(articles);

		Briefing briefing = new Briefing(
				UUID.randomUUID().toString(),
				userId,
				generatedContent,
				articles.size(),
				LocalDateTime.now());

		return briefingRepository.save(briefing);
	}

	private List<ArticleEntry> collectArticles(String userId) {
		List<ArticleEntry> articles = new ArrayList<>();

		List<Source> unsummarizedSources = collectUnsummarizedSources(userId);
		for (Source source : unsummarizedSources) {
			String content = resolveSourceContent(source);
			if (content != null && !content.isBlank()) {
				articles.add(new ArticleEntry(source.sourceId(), source.title(), content, true, null));
			}
		}

		List<RssSourceItemLink> unsummarizedRss = rssSourceItemRepository.findUnsummarizedArticleLinks(userId);
		for (RssSourceItemLink link : unsummarizedRss) {
			String content = resolveRssContent(link);
			if (content != null && !content.isBlank()) {
				articles.add(new ArticleEntry(link.rssSourceItemId(), link.title() != null ? link.title() : "Untitled", content, false, link.rssSourceItemId()));
			}
		}

		return articles;
	}

	private List<Source> collectUnsummarizedSources(String userId) {
		return sourceService.getSources(userId).stream()
				.filter(s -> s.type() == SourceType.ARTICLE_URL
						|| s.type() == SourceType.PLAIN_TEXT)
				.filter(s -> s.summarizedAt() == null)
				.toList();
	}

	private String resolveSourceContent(Source source) {
		if (source.type() == SourceType.PLAIN_TEXT) {
			return source.content();
		}

		if (source.articleContent() != null && !source.articleContent().isBlank()) {
			return source.articleContent();
		}

		String url = source.content();
		if (url == null || url.isBlank()) {
			return null;
		}

		try {
			var article = articleContentClient.readArticle(url);
			sourceService.storeArticleContent(source.sourceId(), article.content());
			return article.content();
		}
		catch (RuntimeException exception) {
			LOGGER.warn("[BRIEFING] failed to fetch source content url={} reason={}", url, exception.getMessage());
			return null;
		}
	}

	private String resolveRssContent(RssSourceItemLink link) {
		if (link.content() != null && !link.content().isBlank()) {
			return link.content();
		}

		if (link.link() == null || link.link().isBlank()) {
			return null;
		}

		try {
			var article = articleContentClient.readArticle(link.link());
			rssSourceItemRepository.storeArticleContent(link.rssSourceItemId(), article.content());
			return article.content();
		}
		catch (RuntimeException exception) {
			LOGGER.warn("[BRIEFING] failed to fetch RSS content url={} reason={}", link.link(), exception.getMessage());
			return null;
		}
	}

	private String buildPrompt(List<ArticleEntry> articles) {
		StringBuilder builder = new StringBuilder();
		builder.append("Please summarize the following articles:\n\n");

		for (int i = 0; i < articles.size(); i++) {
			ArticleEntry article = articles.get(i);
			builder.append("--- Article ").append(i + 1).append(" ---\n");
			if (article.title() != null && !article.title().isBlank()) {
				builder.append("Title: ").append(article.title()).append("\n");
			}
			builder.append("Content:\n");
			builder.append(article.content()).append("\n\n");
		}

		builder.append("Provide a concise yet comprehensive summary of all articles. ");
		builder.append("Highlight connections between topics. ");
		builder.append("Use bullet points for key takeaways.");

		return builder.toString();
	}

	private void markArticlesAsSummarized(List<ArticleEntry> articles) {
		for (ArticleEntry article : articles) {
			try {
				if (article.isSource()) {
					sourceService.markAsSummarized(article.id());
				}
				else if (article.rssItemId() != null) {
					rssSourceItemRepository.markAsSummarized(article.rssItemId());
				}
			}
			catch (RuntimeException exception) {
				LOGGER.warn("[BRIEFING] failed to mark article as summarized id={}", article.id(), exception);
			}
		}
	}

	private record ArticleEntry(String id, String title, String content, boolean isSource, String rssItemId) {
	}
}
