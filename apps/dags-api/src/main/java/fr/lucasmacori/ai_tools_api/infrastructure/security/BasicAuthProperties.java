package fr.lucasmacori.ai_tools_api.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.basic-auth")
public record BasicAuthProperties(String username, String password) {
}
