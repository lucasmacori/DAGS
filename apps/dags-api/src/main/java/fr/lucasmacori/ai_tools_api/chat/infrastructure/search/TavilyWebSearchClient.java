package fr.lucasmacori.ai_tools_api.chat.infrastructure.search;

import java.time.Duration;
import java.util.List;

import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.annotation.JsonProperty;

import fr.lucasmacori.ai_tools_api.chat.domain.model.WebSearchResult;
import fr.lucasmacori.ai_tools_api.chat.domain.spi.WebSearchClient;

public class TavilyWebSearchClient implements WebSearchClient {

	private final WebClient webClient;
	private final Duration timeout;
	private final int maxResults;
	private final String searchDepth;

	public TavilyWebSearchClient(WebClient webClient, Duration timeout, int maxResults, String searchDepth) {
		this.webClient = webClient;
		this.timeout = timeout;
		this.maxResults = maxResults;
		this.searchDepth = searchDepth;
	}

	@Override
	public List<WebSearchResult> search(String query) {
		TavilyResponse response = webClient.post()
				.uri("/search")
				.bodyValue(new TavilySearchRequest(query, searchDepth, maxResults, false, false))
				.retrieve()
				.bodyToMono(TavilyResponse.class)
				.timeout(timeout)
				.onErrorReturn(new TavilyResponse(List.of()))
				.block();

		if (response == null || response.results() == null) {
			return List.of();
		}

		return response.results().stream()
				.filter(result -> result.title() != null && result.url() != null && result.content() != null)
				.map(result -> new WebSearchResult(result.title(), result.url(), result.content()))
				.toList();
	}

	private record TavilySearchRequest(
			String query,
			@JsonProperty("search_depth") String searchDepth,
			@JsonProperty("max_results") int maxResults,
			@JsonProperty("include_answer") boolean includeAnswer,
			@JsonProperty("include_raw_content") boolean includeRawContent) {
	}

	private record TavilyResponse(List<TavilyResult> results) {
	}

	private record TavilyResult(String title, String url, String content) {
	}
}
