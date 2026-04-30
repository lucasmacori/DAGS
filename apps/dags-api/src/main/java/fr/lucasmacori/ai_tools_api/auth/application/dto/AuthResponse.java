package fr.lucasmacori.ai_tools_api.auth.application.dto;

import fr.lucasmacori.ai_tools_api.auth.domain.model.AuthTokenPair;
import fr.lucasmacori.ai_tools_api.auth.domain.model.AuthUserView;

public record AuthResponse(
		String accessToken,
		long accessTokenExpiresInSeconds,
		String refreshToken,
		long refreshTokenExpiresInSeconds,
		AuthUserView user) {
	public static AuthResponse from(AuthTokenPair tokenPair) {
		return new AuthResponse(
				tokenPair.accessToken(),
				tokenPair.accessTokenExpiresInSeconds(),
				tokenPair.refreshToken(),
				tokenPair.refreshTokenExpiresInSeconds(),
				tokenPair.user());
	}
}
