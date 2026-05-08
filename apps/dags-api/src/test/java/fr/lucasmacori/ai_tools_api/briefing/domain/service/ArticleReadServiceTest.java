package fr.lucasmacori.ai_tools_api.briefing.domain.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import fr.lucasmacori.ai_tools_api.briefing.domain.model.ArticleContent;
import fr.lucasmacori.ai_tools_api.briefing.domain.model.RssSourceItemLink;
import fr.lucasmacori.ai_tools_api.briefing.domain.model.Source;
import fr.lucasmacori.ai_tools_api.briefing.domain.model.SourceType;
import fr.lucasmacori.ai_tools_api.briefing.domain.repository.IRssSourceItemRepository;
import fr.lucasmacori.ai_tools_api.briefing.infrastructure.article.ArticleContentClient;

class ArticleReadServiceTest {

	@Test
	void readAllArticlesReadsArticleSourcesAndRssLinksOncePerUniqueUrl() {
		SourceService sourceService = mock(SourceService.class);
		IRssSourceItemRepository rssSourceItemRepository = mock(IRssSourceItemRepository.class);
		ArticleContentClient articleContentClient = mock(ArticleContentClient.class);
		Source articleSource = new Source(
				"source-1",
				SourceType.ARTICLE_URL,
				"Article",
				"https://example.com/article",
				LocalDateTime.now(),
				LocalDateTime.now(),
				null);

		when(sourceService.getUnreadArticleSources("user-1")).thenReturn(List.of(articleSource));
		when(rssSourceItemRepository.findUnreadArticleLinks("user-1"))
				.thenReturn(List.of(
						new RssSourceItemLink("rss-1", "https://example.com/article"),
						new RssSourceItemLink("rss-2", "https://example.com/another")));
		when(articleContentClient.readArticle("https://example.com/article"))
				.thenReturn(new ArticleContent("https://example.com/article", "Article", "Content"));
		when(articleContentClient.readArticle("https://example.com/another"))
				.thenReturn(new ArticleContent("https://example.com/another", "Another", "More"));

		ArticleReadService service = new ArticleReadService(sourceService, rssSourceItemRepository, articleContentClient);

		service.readAllArticles("user-1");

		verify(articleContentClient, times(1)).readArticle("https://example.com/article");
		verify(articleContentClient, times(1)).readArticle("https://example.com/another");
		verify(sourceService).markArticleAsRead("source-1");
		verify(rssSourceItemRepository).markArticleLinkAsRead("user-1", "rss-1");
		verify(rssSourceItemRepository).markArticleLinkAsRead("user-1", "rss-2");
	}

	@Test
	void readAllArticlesSkipsRssItemsAlreadyMarkedReadByOnlyLoadingUnreadLinks() {
		SourceService sourceService = mock(SourceService.class);
		IRssSourceItemRepository rssSourceItemRepository = mock(IRssSourceItemRepository.class);
		ArticleContentClient articleContentClient = mock(ArticleContentClient.class);

		when(sourceService.getUnreadArticleSources("user-1")).thenReturn(List.of());
		when(rssSourceItemRepository.findUnreadArticleLinks("user-1")).thenReturn(List.of());

		ArticleReadService service = new ArticleReadService(sourceService, rssSourceItemRepository, articleContentClient);

		service.readAllArticles("user-1");

		verify(articleContentClient, never()).readArticle("https://example.com/article");
	}
}
