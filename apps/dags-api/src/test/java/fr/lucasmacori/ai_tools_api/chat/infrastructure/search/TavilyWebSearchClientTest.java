package fr.lucasmacori.ai_tools_api.chat.infrastructure.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import fr.lucasmacori.ai_tools_api.chat.domain.model.WebSearchResult;
import reactor.core.publisher.Mono;

class TavilyWebSearchClientTest {

	@Test
	void searchMapsTavilyResultsToDomainResults() {
		WebClient webClient = WebClient.builder()
				.exchangeFunction(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
						.header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
						.body("""
								{
								  "results": [
								    {
								      "title": "First result",
								      "url": "https://example.com/first",
								      "content": "First result content",
								      "score": 0.94
								    }
								  ]
								}
								""")
						.build()))
				.build();
		TavilyWebSearchClient client = new TavilyWebSearchClient(webClient, Duration.ofSeconds(1), 5, "basic");

		List<WebSearchResult> results = client.search("current news");

		assertEquals(1, results.size());
		assertEquals("First result", results.getFirst().title());
		assertEquals("https://example.com/first", results.getFirst().url());
		assertEquals("First result content", results.getFirst().content());
	}

	@Test
	void searchReturnsEmptyResultsWhenTavilyFails() {
		WebClient webClient = WebClient.builder()
				.exchangeFunction(request -> Mono.just(ClientResponse.create(HttpStatus.TOO_MANY_REQUESTS).build()))
				.build();
		TavilyWebSearchClient client = new TavilyWebSearchClient(webClient, Duration.ofSeconds(1), 5, "basic");

		List<WebSearchResult> results = client.search("current news");

		assertTrue(results.isEmpty());
	}
}
