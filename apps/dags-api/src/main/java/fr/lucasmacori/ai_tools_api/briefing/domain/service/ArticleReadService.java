package fr.lucasmacori.ai_tools_api.briefing.domain.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fr.lucasmacori.ai_tools_api.briefing.domain.model.ArticleContent;
import fr.lucasmacori.ai_tools_api.briefing.domain.model.RssSourceItemLink;
import fr.lucasmacori.ai_tools_api.briefing.domain.model.Source;
import fr.lucasmacori.ai_tools_api.briefing.domain.repository.IRssSourceItemRepository;
import fr.lucasmacori.ai_tools_api.briefing.infrastructure.article.ArticleContentClient;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArticleReadService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ArticleReadService.class);

	private final SourceService sourceService;
	private final IRssSourceItemRepository rssSourceItemRepository;
	private final ArticleContentClient articleContentClient;

	public void readAllArticles(String userId) {
		Map<String, ReadTargets> targetsByUrl = new LinkedHashMap<>();

		List<Source> articleSources = sourceService.getUnreadArticleSources(userId);
		for (Source source : articleSources) {
			addSourceTarget(targetsByUrl, source);
		}

		for (RssSourceItemLink link : rssSourceItemRepository.findUnreadArticleLinks(userId)) {
			addRssTarget(targetsByUrl, link);
		}

		for (Map.Entry<String, ReadTargets> entry : targetsByUrl.entrySet()) {
			readAndMark(userId, entry.getKey(), entry.getValue());
		}
	}

	private void addSourceTarget(Map<String, ReadTargets> targetsByUrl, Source source) {
		String url = source.content();
		if (url == null || url.isBlank()) {
			return;
		}

		targetsByUrl.computeIfAbsent(url.trim(), ignored -> new ReadTargets()).sourceIds().add(source.sourceId());
	}

	private void addRssTarget(Map<String, ReadTargets> targetsByUrl, RssSourceItemLink item) {
		String url = item.link();
		if (url == null || url.isBlank()) {
			return;
		}

		targetsByUrl.computeIfAbsent(url.trim(), ignored -> new ReadTargets()).rssItemIds().add(item.rssSourceItemId());
	}

	private void readAndMark(String userId, String url, ReadTargets targets) {

		try {
			ArticleContent article = articleContentClient.readArticle(url);
			logArticle(userId, article);
			for (String sourceId : targets.sourceIds()) {
				sourceService.markArticleAsRead(sourceId);
				sourceService.storeArticleContent(sourceId, article.content());
			}
			for (String rssSourceItemId : targets.rssItemIds()) {
				rssSourceItemRepository.markArticleLinkAsRead(userId, rssSourceItemId);
				rssSourceItemRepository.storeArticleContent(rssSourceItemId, article.content());
			}
		}
		catch (RuntimeException exception) {
			LOGGER.warn("[ARTICLE][FAILED] url={} reason={}", url, exception.getMessage());
		}
	}

	private void logArticle(String userId, ArticleContent article) {
		LOGGER.info("[ARTICLE][READ] userId={} url={} title=\"{}\" contentLength={}",
				userId,
				article.url(),
				article.title(),
				article.content().length());
	}

	private static final class ReadTargets {
		private final List<String> sourceIds = new ArrayList<>();
		private final List<String> rssItemIds = new ArrayList<>();

		private List<String> sourceIds() {
			return sourceIds;
		}

		private List<String> rssItemIds() {
			return rssItemIds;
		}
	}
}
