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

	private final ISourceRepository sourceRepository;
	private final IRssSourceItemRepository rssSourceItemRepository;
	private final RssFeedClient rssFeedClient;

	public void readAllFeeds(String userId) {
		List<Source> rssSources = sourceRepository.findAllByUserId(userId).stream()
				.filter(source -> source.type() == SourceType.RSS_FEED)
				.toList();

		for (Source source : rssSources) {
			readFeed(userId, source);
		}
	}

	private void readFeed(String userId, Source source) {
		try {
			List<RssFeedArticle> articles = rssFeedClient.readArticles(source.content());
			for (RssFeedArticle article : articles) {
				if (rssSourceItemRepository.hasArticleBeenRead(userId, source.sourceId(), article.externalId())) {
					continue;
				}

				logUnreadArticle(userId, source, article);
				rssSourceItemRepository.markArticleAsRead(userId, source, article);
			}
		}
		catch (RuntimeException exception) {
			LOGGER.warn("Failed to read RSS source {}", source.sourceId(), exception);
		}
	}

	private void logUnreadArticle(String userId, Source source, RssFeedArticle article) {
		LOGGER.info(
				"[RSS][NEW] userId={} sourceId={} sourceTitle=\"{}\" articleTitle=\"{}\" link={} publishedAt={}",
				userId,
				source.sourceId(),
				source.title(),
				article.title(),
				article.link(),
				article.publishedAt());
	}
}
