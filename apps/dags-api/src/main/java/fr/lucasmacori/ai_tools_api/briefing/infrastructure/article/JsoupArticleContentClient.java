package fr.lucasmacori.ai_tools_api.briefing.infrastructure.article;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import fr.lucasmacori.ai_tools_api.briefing.domain.model.ArticleContent;

@Component
public class JsoupArticleContentClient implements ArticleContentClient {

	@Override
	public ArticleContent readArticle(String url) {
		try {
			Document document = Jsoup.connect(url)
					.userAgent("Mozilla/5.0 (compatible; DAGS Briefing Bot/1.0)")
					.timeout(15000)
					.followRedirects(true)
					.get();

			removeNoise(document);
			String title = document.title();
			String content = document.body() == null ? "" : normalize(document.body().text());
			return new ArticleContent(url, title, content);
		}
		catch (Exception exception) {
			throw new IllegalStateException("Failed to read article content", exception);
		}
	}

	private void removeNoise(Document document) {
		for (Element element : document.select("script, style, noscript, header, footer, nav, aside")) {
			element.remove();
		}
	}

	private String normalize(String text) {
		return text == null ? "" : text.replaceAll("\\s+", " ").trim();
	}
}
