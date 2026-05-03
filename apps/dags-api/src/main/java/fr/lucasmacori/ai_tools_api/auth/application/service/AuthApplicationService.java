package fr.lucasmacori.ai_tools_api.auth.application.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import fr.lucasmacori.ai_tools_api.auth.application.dto.AuthResponse;
import fr.lucasmacori.ai_tools_api.auth.application.dto.ChangePasswordRequestBody;
import fr.lucasmacori.ai_tools_api.auth.application.dto.LoginRequestBody;
import fr.lucasmacori.ai_tools_api.auth.application.dto.LogoutRequestBody;
import fr.lucasmacori.ai_tools_api.auth.application.dto.RefreshTokenRequestBody;
import fr.lucasmacori.ai_tools_api.auth.application.dto.RegisterRequestBody;
import fr.lucasmacori.ai_tools_api.auth.application.dto.UpdateEmailRequestBody;
import fr.lucasmacori.ai_tools_api.auth.domain.model.AuthUserView;
import fr.lucasmacori.ai_tools_api.auth.domain.service.AuthService;
import fr.lucasmacori.ai_tools_api.auth.domain.service.EmailAlreadyUsedException;
import fr.lucasmacori.ai_tools_api.auth.domain.service.InvalidCredentialsException;
import fr.lucasmacori.ai_tools_api.auth.domain.service.InvalidCurrentPasswordException;
import fr.lucasmacori.ai_tools_api.auth.domain.service.InvalidRefreshTokenException;
import fr.lucasmacori.ai_tools_api.auth.domain.service.RegistrationDisabledException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthApplicationService {

	private final AuthService authService;

	public AuthResponse login(LoginRequestBody request) {
		try {
			return AuthResponse.from(authService.login(request.email(), request.password()));
		}
		catch (InvalidCredentialsException exception) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, exception.getMessage(), exception);
		}
		catch (IllegalArgumentException exception) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
		}
	}

	public AuthResponse register(RegisterRequestBody request) {
		try {
			return AuthResponse.from(authService.register(request.email(), request.password()));
		}
		catch (RegistrationDisabledException exception) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage(), exception);
		}
		catch (EmailAlreadyUsedException exception) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
		}
		catch (IllegalArgumentException exception) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
		}
	}

	public AuthResponse refresh(RefreshTokenRequestBody request) {
		try {
			return AuthResponse.from(authService.refresh(request.refreshToken()));
		}
		catch (InvalidRefreshTokenException | IllegalArgumentException exception) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, exception.getMessage(), exception);
		}
	}

	public void logout(LogoutRequestBody request) {
		try {
			authService.logout(request.refreshToken());
		}
		catch (IllegalArgumentException exception) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
		}
	}

	public AuthUserView getCurrentUser(Jwt jwt) {
		return authService.getCurrentUser(jwt);
	}

	public AuthResponse updateEmail(Jwt jwt, UpdateEmailRequestBody request) {
		try {
			return AuthResponse.from(authService.updateEmail(jwt.getSubject(), request.email(), request.password()));
		}
		catch (InvalidCredentialsException exception) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, exception.getMessage(), exception);
		}
		catch (InvalidCurrentPasswordException exception) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, exception.getMessage(), exception);
		}
		catch (EmailAlreadyUsedException exception) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
		}
		catch (IllegalArgumentException exception) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
		}
	}

	public AuthResponse changePassword(Jwt jwt, ChangePasswordRequestBody request) {
		try {
			return AuthResponse.from(authService.changePassword(jwt.getSubject(), request.currentPassword(), request.newPassword()));
		}
		catch (InvalidCredentialsException exception) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, exception.getMessage(), exception);
		}
		catch (InvalidCurrentPasswordException exception) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, exception.getMessage(), exception);
		}
		catch (IllegalArgumentException exception) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
		}
	}
}
