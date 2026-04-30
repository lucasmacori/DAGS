package fr.lucasmacori.ai_tools_api.auth.domain.service;

public class RegistrationDisabledException extends RuntimeException {
	public RegistrationDisabledException() {
		super("Account creation is disabled");
	}
}
