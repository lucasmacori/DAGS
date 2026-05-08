package fr.lucasmacori.ai_tools_api.briefing.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import fr.lucasmacori.ai_tools_api.briefing.domain.model.RssFeedArticle;
import fr.lucasmacori.ai_tools_api.briefing.domain.model.RssSourceItemLink;
import fr.lucasmacori.ai_tools_api.briefing.domain.model.Source;
import fr.lucasmacori.ai_tools_api.briefing.domain.repository.IRssSourceItemRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RssSourceItemRepository implements IRssSourceItemRepository {

	private final NamedParameterJdbcTemplate jdbcTemplate;

	@Override
	public boolean hasArticleBeenRead(String userId, String sourceId, String externalId) {
		Integer count = jdbcTemplate.queryForObject(
				"""
				SELECT COUNT(*)
				FROM rss_source_item
				WHERE user_id = :user_id
				  AND source_id = :source_id
				  AND external_id = :external_id
				""",
				Map.of(
						"user_id", userId,
						"source_id", UUID.fromString(sourceId),
						"external_id", externalId),
				Integer.class);

		return count != null && count > 0;
	}

	@Override
	public List<RssSourceItemLink> findUnreadArticleLinks(String userId) {
		return jdbcTemplate.query(
				"""
				SELECT rss_source_item_id, link
				FROM rss_source_item
				WHERE user_id = :user_id
				  AND link IS NOT NULL
				  AND btrim(link) <> ''
				  AND article_read_at IS NULL
				ORDER BY discovered_at DESC
				""",
				Map.of("user_id", userId),
				(rs, rowNum) -> new RssSourceItemLink(rs.getObject("rss_source_item_id", UUID.class).toString(), rs.getString("link")));
	}

	@Override
	public List<RssSourceItemLink> findUnsummarizedArticleLinks(String userId) {
		return jdbcTemplate.query(
				"""
				SELECT rss_source_item_id, link, content, title
				FROM rss_source_item
				WHERE user_id = :user_id
				  AND link IS NOT NULL
				  AND btrim(link) <> ''
				  AND summarized_at IS NULL
				ORDER BY discovered_at DESC
				""",
				Map.of("user_id", userId),
				(rs, rowNum) -> new RssSourceItemLink(
						rs.getObject("rss_source_item_id", UUID.class).toString(),
						rs.getString("link"),
						rs.getString("content"),
						rs.getString("title")));
	}

	@Override
	public void markArticleAsRead(String userId, Source source, RssFeedArticle article) {
		MapSqlParameterSource parameters = new MapSqlParameterSource()
				.addValue("rss_source_item_id", UUID.randomUUID())
				.addValue("source_id", UUID.fromString(source.sourceId()))
				.addValue("user_id", userId)
				.addValue("external_id", article.externalId())
				.addValue("title", article.title())
				.addValue("link", article.link())
				.addValue("published_at", article.publishedAt())
				.addValue("discovered_at", LocalDateTime.now());

		jdbcTemplate.update(
				"""
				INSERT INTO rss_source_item (
				    rss_source_item_id,
				    source_id,
				    user_id,
				    external_id,
				    title,
				    link,
				    published_at,
				    discovered_at
				)
				VALUES (
				    :rss_source_item_id,
				    :source_id,
				    :user_id,
				    :external_id,
				    :title,
				    :link,
				    :published_at,
				    :discovered_at
				)
				""",
				parameters);
	}

	@Override
	public void markArticleLinkAsRead(String userId, String rssSourceItemId) {
		jdbcTemplate.update(
				"""
				UPDATE rss_source_item
				SET article_read_at = :article_read_at
				WHERE user_id = :user_id
				  AND rss_source_item_id = :rss_source_item_id
				""",
				Map.of(
						"user_id", userId,
						"rss_source_item_id", UUID.fromString(rssSourceItemId),
						"article_read_at", LocalDateTime.now()));
	}

	@Override
	public void storeArticleContent(String rssSourceItemId, String content) {
		jdbcTemplate.update(
				"""
				UPDATE rss_source_item
				SET content = :content
				WHERE rss_source_item_id = :rss_source_item_id
				""",
				Map.of(
						"rss_source_item_id", UUID.fromString(rssSourceItemId),
						"content", content));
	}

	@Override
	public void markAsSummarized(String rssSourceItemId) {
		jdbcTemplate.update(
				"""
				UPDATE rss_source_item
				SET summarized_at = :summarized_at
				WHERE rss_source_item_id = :rss_source_item_id
				""",
				Map.of(
						"rss_source_item_id", UUID.fromString(rssSourceItemId),
						"summarized_at", LocalDateTime.now()));
	}
}
