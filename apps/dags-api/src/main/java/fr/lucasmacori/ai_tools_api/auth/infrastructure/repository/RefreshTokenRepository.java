package fr.lucasmacori.ai_tools_api.auth.infrastructure.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import fr.lucasmacori.ai_tools_api.auth.domain.model.RefreshTokenRecord;
import fr.lucasmacori.ai_tools_api.auth.domain.repository.IRefreshTokenRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository implements IRefreshTokenRepository {

	private static final RowMapper<RefreshTokenRecord> ROW_MAPPER = new RefreshTokenRowMapper();

	private final NamedParameterJdbcTemplate jdbcTemplate;

	@Override
	public RefreshTokenRecord save(RefreshTokenRecord refreshTokenRecord) {
		MapSqlParameterSource parameters = new MapSqlParameterSource()
				.addValue("token_id", UUID.fromString(refreshTokenRecord.tokenId()))
				.addValue("user_id", UUID.fromString(refreshTokenRecord.userId()))
				.addValue("token_hash", refreshTokenRecord.tokenHash())
				.addValue("expires_at", refreshTokenRecord.expiresAt())
				.addValue("revoked_at", refreshTokenRecord.revokedAt())
				.addValue("created_at", refreshTokenRecord.createdAt());

		jdbcTemplate.update(
				"INSERT INTO auth_refresh_token (token_id, user_id, token_hash, expires_at, revoked_at, created_at) VALUES (:token_id, :user_id, :token_hash, :expires_at, :revoked_at, :created_at)",
				parameters);
		return refreshTokenRecord;
	}

	@Override
	public Optional<RefreshTokenRecord> findByTokenHash(String tokenHash) {
		return jdbcTemplate.query(
				"SELECT token_id, user_id, token_hash, expires_at, revoked_at, created_at FROM auth_refresh_token WHERE token_hash = :token_hash",
				Map.of("token_hash", tokenHash),
				ROW_MAPPER)
				.stream()
				.findFirst();
	}

	@Override
	public void revoke(String tokenId, LocalDateTime revokedAt) {
		jdbcTemplate.update(
				"UPDATE auth_refresh_token SET revoked_at = :revoked_at WHERE token_id = :token_id AND revoked_at IS NULL",
				Map.of("token_id", UUID.fromString(tokenId), "revoked_at", revokedAt));
	}

	@Override
	public void revokeAllForUser(String userId, LocalDateTime revokedAt) {
		jdbcTemplate.update(
				"UPDATE auth_refresh_token SET revoked_at = :revoked_at WHERE user_id = :user_id AND revoked_at IS NULL",
				Map.of("user_id", UUID.fromString(userId), "revoked_at", revokedAt));
	}

	private static final class RefreshTokenRowMapper implements RowMapper<RefreshTokenRecord> {
		@Override
		public RefreshTokenRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
			Timestamp expiresAt = rs.getTimestamp("expires_at");
			Timestamp revokedAt = rs.getTimestamp("revoked_at");
			Timestamp createdAt = rs.getTimestamp("created_at");
			return new RefreshTokenRecord(
					rs.getObject("token_id", UUID.class).toString(),
					rs.getObject("user_id", UUID.class).toString(),
					rs.getString("token_hash"),
					expiresAt == null ? null : expiresAt.toLocalDateTime(),
					revokedAt == null ? null : revokedAt.toLocalDateTime(),
					createdAt == null ? null : createdAt.toLocalDateTime());
		}
	}
}
