package fr.lucasmacori.ai_tools_api.briefing.domain.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import fr.lucasmacori.ai_tools_api.briefing.domain.model.RssFeedArticle;
import fr.lucasmacori.ai_tools_api.briefing.domain.model.Source;
import fr.lucasmacori.ai_tools_api.briefing.domain.model.SourceType;
import fr.lucasmacori.ai_tools_api.briefing.domain.repository.IRssSourceItemRepository;
import fr.lucasmacori.ai_tools_api.briefing.domain.repository.ISourceRepository;
import fr.lucasmacori.ai_tools_api.briefing.infrastructure.rss.RssFeedClient;

class RssFeedReadServiceTest {

	@Test
	void readAllFeedsStoresOnlyUnreadArticlesFromRssSources() {
		ISourceRepository sourceRepository = mock(ISourceRepository.class);
		IRssSourceItemRepository rssSourceItemRepository = mock(IRssSourceItemRepository.class);
		RssFeedClient rssFeedClient = mock(RssFeedClient.class);
		Source rssSource = new Source(
				"source-1",
				SourceType.RSS_FEED,
				"Feed",
				"https://example.com/rss",
				LocalDateTime.now(),
				LocalDateTime.now(),
				null);
		Source plainTextSource = new Source(
				"source-2",
				SourceType.PLAIN_TEXT,
				"Notes",
				"hello",
				LocalDateTime.now(),
				LocalDateTime.now(),
				null);
		RssFeedArticle unreadArticle = new RssFeedArticle("article-1", "Unread", "https://example.com/a", LocalDateTime.now());
		RssFeedArticle readArticle = new RssFeedArticle("article-2", "Read", "https://example.com/b", LocalDateTime.now());

		when(sourceRepository.findAll()).thenReturn(List.of(rssSource, plainTextSource));
		when(rssFeedClient.readArticles("https://example.com/rss")).thenReturn(List.of(unreadArticle, readArticle));
		when(rssSourceItemRepository.hasArticleBeenRead("hardcoded-user-id", "source-1", "article-1")).thenReturn(false);
		when(rssSourceItemRepository.hasArticleBeenRead("hardcoded-user-id", "source-1", "article-2")).thenReturn(true);

		RssFeedReadService service = new RssFeedReadService(sourceRepository, rssSourceItemRepository, rssFeedClient);

		service.readAllFeeds();

		verify(rssFeedClient).readArticles("https://example.com/rss");
		verify(rssFeedClient, never()).readArticles("hello");
		verify(rssSourceItemRepository).markArticleAsRead("hardcoded-user-id", rssSource, unreadArticle);
		verify(rssSourceItemRepository, never()).markArticleAsRead("hardcoded-user-id", rssSource, readArticle);
	}
}
