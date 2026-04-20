package fr.lucasmacori.ai_tools_api.briefing.domain.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fr.lucasmacori.ai_tools_api.briefing.domain.model.RssFeedArticle;
import fr.lucasmacori.ai_tools_api.briefing.domain.model.Source;
import fr.lucasmacori.ai_tools_api.briefing.domain.model.SourceType;
import fr.lucasmacori.ai_tools_api.briefing.domain.repository.IRssSourceItemRepository;
import fr.lucasmacori.ai_tools_api.briefing.domain.repository.ISourceRepository;
import fr.lucasmacori.ai_tools_api.briefing.infrastructure.rss.RssFeedClient;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RssFeedReadService {

	private static final Logger LOGGER = LoggerFactory.getLogger(RssFeedReadService.class);

	private static final String DEFAULT_USER_ID = "hardcoded-user-id";

	private final ISourceRepository sourceRepository;
	private final IRssSourceItemRepository rssSourceItemRepository;
	private final RssFeedClient rssFeedClient;

	public void readAllFeeds() {
		List<Source> rssSources = sourceRepository.findAll().stream()
				.filter(source -> source.type() == SourceType.RSS_FEED)
				.toList();

		for (Source source : rssSources) {
			readFeed(source);
		}
	}

	private void readFeed(Source source) {
		try {
			List<RssFeedArticle> articles = rssFeedClient.readArticles(source.content());
			for (RssFeedArticle article : articles) {
				if (rssSourceItemRepository.hasArticleBeenRead(DEFAULT_USER_ID, source.sourceId(), article.externalId())) {
					continue;
				}

				logUnreadArticle(source, article);
				rssSourceItemRepository.markArticleAsRead(DEFAULT_USER_ID, source, article);
			}
		}
		catch (RuntimeException exception) {
			LOGGER.warn("Failed to read RSS source {}", source.sourceId(), exception);
		}
	}

	private void logUnreadArticle(Source source, RssFeedArticle article) {
		LOGGER.info(
				"[RSS][NEW] userId={} sourceId={} sourceTitle=\"{}\" articleTitle=\"{}\" link={} publishedAt={}",
				DEFAULT_USER_ID,
				source.sourceId(),
				source.title(),
				article.title(),
				article.link(),
				article.publishedAt());
	}
}
