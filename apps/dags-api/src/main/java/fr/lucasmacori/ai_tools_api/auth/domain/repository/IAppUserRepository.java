package fr.lucasmacori.ai_tools_api.auth.domain.repository;

import java.util.Optional;

import fr.lucasmacori.ai_tools_api.auth.domain.model.AppUser;

public interface IAppUserRepository {
	Optional<AppUser> findByEmail(String email);

	Optional<AppUser> findById(String userId);

	AppUser save(AppUser user);

	void updateEmail(String userId, String newEmail);

	void updatePasswordHash(String userId, String newPasswordHash);
}
