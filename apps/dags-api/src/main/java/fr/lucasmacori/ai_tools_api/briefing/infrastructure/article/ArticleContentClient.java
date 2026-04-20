package fr.lucasmacori.ai_tools_api.briefing.infrastructure.article;

import fr.lucasmacori.ai_tools_api.briefing.domain.model.ArticleContent;

public interface ArticleContentClient {
	ArticleContent readArticle(String url);
}
