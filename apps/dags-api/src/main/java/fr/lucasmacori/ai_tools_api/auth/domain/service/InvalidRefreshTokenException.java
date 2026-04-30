package fr.lucasmacori.ai_tools_api.auth.domain.service;

public class InvalidRefreshTokenException extends RuntimeException {
	public InvalidRefreshTokenException() {
		super("Invalid refresh token");
	}
}
