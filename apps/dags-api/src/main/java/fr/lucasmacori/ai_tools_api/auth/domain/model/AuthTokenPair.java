package fr.lucasmacori.ai_tools_api.auth.domain.model;

public record AuthTokenPair(
		String accessToken,
		long accessTokenExpiresInSeconds,
		String refreshToken,
		long refreshTokenExpiresInSeconds,
		AuthUserView user) {
}
