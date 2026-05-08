package fr.lucasmacori.ai_tools_api.briefing.domain.model;

public record RssSourceItemLink(String rssSourceItemId, String link, String content, String title) {

	public RssSourceItemLink(String rssSourceItemId, String link) {
		this(rssSourceItemId, link, null, null);
	}
}
