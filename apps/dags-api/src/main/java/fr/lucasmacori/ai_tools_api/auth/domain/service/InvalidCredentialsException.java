package fr.lucasmacori.ai_tools_api.auth.domain.service;

public class InvalidCredentialsException extends RuntimeException {
	public InvalidCredentialsException() {
		super("Invalid email or password");
	}
}
