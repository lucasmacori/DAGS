package fr.lucasmacori.ai_tools_api.auth.domain.model;

public record AuthUserView(String userId, String email) {
	public static AuthUserView from(AppUser user) {
		return new AuthUserView(user.userId(), user.email());
	}
}
