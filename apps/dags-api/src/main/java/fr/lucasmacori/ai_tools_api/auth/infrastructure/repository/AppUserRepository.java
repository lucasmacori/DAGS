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

import fr.lucasmacori.ai_tools_api.auth.domain.model.AppUser;
import fr.lucasmacori.ai_tools_api.auth.domain.repository.IAppUserRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AppUserRepository implements IAppUserRepository {

	private static final RowMapper<AppUser> ROW_MAPPER = new AppUserRowMapper();

	private final NamedParameterJdbcTemplate jdbcTemplate;

	@Override
	public Optional<AppUser> findByEmail(String email) {
		return jdbcTemplate.query(
				"SELECT user_id, email, password_hash, enabled, created_at FROM app_user WHERE email = :email",
				Map.of("email", email),
				ROW_MAPPER)
				.stream()
				.findFirst();
	}

	@Override
	public Optional<AppUser> findById(String userId) {
		return jdbcTemplate.query(
				"SELECT user_id, email, password_hash, enabled, created_at FROM app_user WHERE user_id = :user_id",
				Map.of("user_id", UUID.fromString(userId)),
				ROW_MAPPER)
				.stream()
				.findFirst();
	}

	@Override
	public AppUser save(AppUser user) {
		MapSqlParameterSource parameters = new MapSqlParameterSource()
				.addValue("user_id", UUID.fromString(user.userId()))
				.addValue("email", user.email())
				.addValue("password_hash", user.passwordHash())
				.addValue("enabled", user.enabled())
				.addValue("created_at", user.createdAt());

		jdbcTemplate.update(
				"INSERT INTO app_user (user_id, email, password_hash, enabled, created_at) VALUES (:user_id, :email, :password_hash, :enabled, :created_at)",
				parameters);

		return user;
	}

	private static final class AppUserRowMapper implements RowMapper<AppUser> {
		@Override
		public AppUser mapRow(ResultSet rs, int rowNum) throws SQLException {
			Timestamp createdAt = rs.getTimestamp("created_at");
			return new AppUser(
					rs.getObject("user_id", UUID.class).toString(),
					rs.getString("email"),
					rs.getString("password_hash"),
					rs.getBoolean("enabled"),
					createdAt == null ? null : createdAt.toLocalDateTime());
		}
	}
}
