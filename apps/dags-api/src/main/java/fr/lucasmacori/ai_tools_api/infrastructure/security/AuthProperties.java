package fr.lucasmacori.ai_tools_api.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.auth")
public record AuthProperties(
		boolean registrationEnabled,
		long accessTokenTtlSeconds,
		long refreshTokenTtlSeconds,
		String tokenSecret) {
}
