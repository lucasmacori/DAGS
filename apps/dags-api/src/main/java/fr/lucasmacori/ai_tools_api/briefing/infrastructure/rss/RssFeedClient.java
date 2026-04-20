package fr.lucasmacori.ai_tools_api.briefing.infrastructure.rss;

import java.util.List;

import fr.lucasmacori.ai_tools_api.briefing.domain.model.RssFeedArticle;

public interface RssFeedClient {
	List<RssFeedArticle> readArticles(String feedUrl);
}
