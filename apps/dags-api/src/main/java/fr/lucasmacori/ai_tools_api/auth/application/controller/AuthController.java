package fr.lucasmacori.ai_tools_api.auth.application.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import fr.lucasmacori.ai_tools_api.auth.application.dto.AuthResponse;
import fr.lucasmacori.ai_tools_api.auth.application.dto.ChangePasswordRequestBody;
import fr.lucasmacori.ai_tools_api.auth.application.dto.LoginRequestBody;
import fr.lucasmacori.ai_tools_api.auth.application.dto.LogoutRequestBody;
import fr.lucasmacori.ai_tools_api.auth.application.dto.RefreshTokenRequestBody;
import fr.lucasmacori.ai_tools_api.auth.application.dto.RegisterRequestBody;
import fr.lucasmacori.ai_tools_api.auth.application.dto.UpdateEmailRequestBody;
import fr.lucasmacori.ai_tools_api.auth.application.service.AuthApplicationService;
import fr.lucasmacori.ai_tools_api.auth.domain.model.AuthUserView;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthApplicationService authApplicationService;

	@PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	AuthResponse login(@Valid @RequestBody LoginRequestBody request) {
		return authApplicationService.login(request);
	}

	@PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	AuthResponse register(@Valid @RequestBody RegisterRequestBody request) {
		return authApplicationService.register(request);
	}

	@PostMapping(path = "/refresh", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	AuthResponse refresh(@Valid @RequestBody RefreshTokenRequestBody request) {
		return authApplicationService.refresh(request);
	}

	@PostMapping(path = "/logout", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void logout(@Valid @RequestBody LogoutRequestBody request) {
		authApplicationService.logout(request);
	}

	@GetMapping(path = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
	AuthUserView me(@AuthenticationPrincipal Jwt jwt) {
		return authApplicationService.getCurrentUser(jwt);
	}

	@PatchMapping(path = "/me/email", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	AuthResponse updateEmail(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateEmailRequestBody request) {
		return authApplicationService.updateEmail(jwt, request);
	}

	@PatchMapping(path = "/me/password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	AuthResponse changePassword(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ChangePasswordRequestBody request) {
		return authApplicationService.changePassword(jwt, request);
	}
}
