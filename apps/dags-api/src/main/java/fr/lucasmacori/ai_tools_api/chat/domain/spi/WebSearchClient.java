package fr.lucasmacori.ai_tools_api.chat.domain.spi;

import java.util.List;

import fr.lucasmacori.ai_tools_api.chat.domain.model.WebSearchResult;

public interface WebSearchClient {

	List<WebSearchResult> search(String query);
}
