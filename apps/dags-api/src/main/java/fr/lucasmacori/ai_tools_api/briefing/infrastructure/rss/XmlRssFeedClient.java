package fr.lucasmacori.ai_tools_api.briefing.infrastructure.rss;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import fr.lucasmacori.ai_tools_api.briefing.domain.model.RssFeedArticle;

@Component
public class XmlRssFeedClient implements RssFeedClient {

	private final HttpClient httpClient = HttpClient.newBuilder()
			.followRedirects(Redirect.NORMAL)
			.build();

	@Override
	public List<RssFeedArticle> readArticles(String feedUrl) {
		try {
			HttpRequest request = HttpRequest.newBuilder(URI.create(feedUrl)).GET().build();
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() < 200 || response.statusCode() >= 300) {
				throw new IllegalStateException("Failed to fetch RSS feed: HTTP " + response.statusCode());
			}

			return parseArticles(response.body());
		}
		catch (IOException exception) {
			throw new IllegalStateException("Failed to fetch RSS feed", exception);
		}
		catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("RSS feed reading was interrupted", exception);
		}
	}

	private List<RssFeedArticle> parseArticles(String xml) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			factory.setExpandEntityReferences(false);
			factory.setXIncludeAware(false);
			Document document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes()));

			NodeList items = document.getElementsByTagName("item");
			List<RssFeedArticle> articles = new ArrayList<>();

			for (int index = 0; index < items.getLength(); index++) {
				Element item = (Element) items.item(index);
				String title = textOf(item, "title");
				String link = textOf(item, "link");
				String guid = textOf(item, "guid");
				String externalId = firstNonBlank(guid, link, buildFallbackExternalId(title, textOf(item, "pubDate")));
				if (externalId == null) {
					continue;
				}

				articles.add(new RssFeedArticle(
						externalId,
						title,
						link,
						parsePublishedAt(textOf(item, "pubDate"))));
			}

			return articles;
		}
		catch (Exception exception) {
			throw new IllegalStateException("Failed to parse RSS feed", exception);
		}
	}

	private String textOf(Element parent, String tagName) {
		NodeList nodes = parent.getElementsByTagName(tagName);
		if (nodes.getLength() == 0 || nodes.item(0) == null) {
			return null;
		}

		String text = nodes.item(0).getTextContent();
		if (text == null) {
			return null;
		}

		String normalized = text.trim();
		return normalized.isEmpty() ? null : normalized;
	}

	private String buildFallbackExternalId(String title, String publishedAt) {
		String fallback = firstNonBlank(title, publishedAt);
		return fallback == null ? null : Integer.toHexString(fallback.hashCode());
	}

	private String firstNonBlank(String... values) {
		for (String value : values) {
			if (value != null && !value.isBlank()) {
				return value;
			}
		}

		return null;
	}

	private LocalDateTime parsePublishedAt(String pubDate) {
		if (pubDate == null || pubDate.isBlank()) {
			return null;
		}

		try {
			return OffsetDateTime.parse(pubDate, DateTimeFormatter.RFC_1123_DATE_TIME).toLocalDateTime();
		}
		catch (DateTimeParseException exception) {
			return null;
		}
	}
}
