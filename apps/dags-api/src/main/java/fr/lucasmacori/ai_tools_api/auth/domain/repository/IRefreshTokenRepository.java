package fr.lucasmacori.ai_tools_api.auth.domain.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import fr.lucasmacori.ai_tools_api.auth.domain.model.RefreshTokenRecord;

public interface IRefreshTokenRepository {
	RefreshTokenRecord save(RefreshTokenRecord refreshTokenRecord);

	Optional<RefreshTokenRecord> findByTokenHash(String tokenHash);

	void revoke(String tokenId, LocalDateTime revokedAt);

	void revokeAllForUser(String userId, LocalDateTime revokedAt);
}
