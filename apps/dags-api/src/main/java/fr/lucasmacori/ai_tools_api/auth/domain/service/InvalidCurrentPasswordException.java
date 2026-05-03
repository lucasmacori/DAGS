package fr.lucasmacori.ai_tools_api.auth.domain.service;

public class InvalidCurrentPasswordException extends RuntimeException {
	public InvalidCurrentPasswordException() {
		super("Current password is incorrect.");
	}
}
