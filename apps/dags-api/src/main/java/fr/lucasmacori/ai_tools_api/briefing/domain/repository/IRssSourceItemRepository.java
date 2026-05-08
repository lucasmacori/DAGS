package fr.lucasmacori.ai_tools_api.briefing.domain.repository;

import java.util.List;

import fr.lucasmacori.ai_tools_api.briefing.domain.model.RssFeedArticle;
import fr.lucasmacori.ai_tools_api.briefing.domain.model.RssSourceItemLink;
import fr.lucasmacori.ai_tools_api.briefing.domain.model.Source;

public interface IRssSourceItemRepository {
	boolean hasArticleBeenRead(String userId, String sourceId, String externalId);

	List<RssSourceItemLink> findUnreadArticleLinks(String userId);

	List<RssSourceItemLink> findUnsummarizedArticleLinks(String userId);

	void markArticleAsRead(String userId, Source source, RssFeedArticle article);

	void markArticleLinkAsRead(String userId, String rssSourceItemId);

	void storeArticleContent(String rssSourceItemId, String content);

	void markAsSummarized(String rssSourceItemId);
}
