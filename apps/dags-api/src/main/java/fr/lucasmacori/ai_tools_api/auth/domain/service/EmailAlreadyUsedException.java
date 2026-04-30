package fr.lucasmacori.ai_tools_api.auth.domain.service;

public class EmailAlreadyUsedException extends RuntimeException {
	public EmailAlreadyUsedException() {
		super("An account already exists for this email");
	}
}
