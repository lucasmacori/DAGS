package fr.lucasmacori.ai_tools_api.auth.domain.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.lucasmacori.ai_tools_api.auth.domain.model.AppUser;
import fr.lucasmacori.ai_tools_api.auth.domain.model.AuthTokenPair;
import fr.lucasmacori.ai_tools_api.auth.domain.model.AuthUserView;
import fr.lucasmacori.ai_tools_api.auth.domain.model.RefreshTokenRecord;
import fr.lucasmacori.ai_tools_api.auth.domain.repository.IAppUserRepository;
import fr.lucasmacori.ai_tools_api.auth.domain.repository.IRefreshTokenRepository;
import fr.lucasmacori.ai_tools_api.infrastructure.security.AuthProperties;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private static final JwsHeader JWT_HEADER = JwsHeader.with(MacAlgorithm.HS256).build();

	private final IAppUserRepository appUserRepository;
	private final IRefreshTokenRepository refreshTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtEncoder jwtEncoder;
	private final SecureRandom secureRandom;
	private final Clock clock;
	private final AuthProperties authProperties;

	@Transactional
	public AuthTokenPair login(String email, String password) {
		AppUser user = appUserRepository.findByEmail(normalizeEmail(email))
				.filter(AppUser::enabled)
				.orElseThrow(InvalidCredentialsException::new);

		if (!passwordEncoder.matches(normalizeRequiredPassword(password), user.passwordHash())) {
			throw new InvalidCredentialsException();
		}

		return issueTokenPair(user);
	}

	@Transactional
	public AuthTokenPair register(String email, String password) {
		if (!authProperties.registrationEnabled()) {
			throw new RegistrationDisabledException();
		}

		String normalizedEmail = normalizeEmail(email);
		if (appUserRepository.findByEmail(normalizedEmail).isPresent()) {
			throw new EmailAlreadyUsedException();
		}

		AppUser user = appUserRepository.save(new AppUser(
				UUID.randomUUID().toString(),
				normalizedEmail,
				passwordEncoder.encode(normalizeRequiredPassword(password)),
				true,
				LocalDateTime.now(clock)));

		return issueTokenPair(user);
	}

	@Transactional
	public AuthTokenPair refresh(String refreshToken) {
		String normalizedRefreshToken = normalizeRequiredRefreshToken(refreshToken);
		LocalDateTime now = LocalDateTime.now(clock);

		RefreshTokenRecord tokenRecord = refreshTokenRepository.findByTokenHash(hashToken(normalizedRefreshToken))
				.filter(record -> record.isActiveAt(now))
				.orElseThrow(InvalidRefreshTokenException::new);

		AppUser user = appUserRepository.findById(tokenRecord.userId())
				.filter(AppUser::enabled)
				.orElseThrow(InvalidRefreshTokenException::new);

		refreshTokenRepository.revoke(tokenRecord.tokenId(), now);

		return issueTokenPair(user);
	}

	@Transactional
	public void logout(String refreshToken) {
		String normalizedRefreshToken = normalizeRequiredRefreshToken(refreshToken);
		refreshTokenRepository.findByTokenHash(hashToken(normalizedRefreshToken))
				.ifPresent(record -> refreshTokenRepository.revoke(record.tokenId(), LocalDateTime.now(clock)));
	}

	public AuthUserView getCurrentUser(Jwt jwt) {
		return new AuthUserView(jwt.getSubject(), jwt.getClaimAsString("email"));
	}

	@Transactional
	public AuthTokenPair updateEmail(String userId, String newEmail, String currentPassword) {
		AppUser user = appUserRepository.findById(userId)
				.filter(AppUser::enabled)
				.orElseThrow(() -> new InvalidCredentialsException());

		if (!passwordEncoder.matches(normalizeRequiredPassword(currentPassword), user.passwordHash())) {
			throw new InvalidCurrentPasswordException();
		}

		String normalizedNewEmail = normalizeEmail(newEmail);
		if (!normalizedNewEmail.equals(user.email()) && appUserRepository.findByEmail(normalizedNewEmail).isPresent()) {
			throw new EmailAlreadyUsedException();
		}

		appUserRepository.updateEmail(userId, normalizedNewEmail);

		AppUser updatedUser = new AppUser(
				user.userId(),
				normalizedNewEmail,
				user.passwordHash(),
				user.enabled(),
				user.createdAt());

		return issueTokenPair(updatedUser);
	}

	@Transactional
	public AuthTokenPair changePassword(String userId, String currentPassword, String newPassword) {
		AppUser user = appUserRepository.findById(userId)
				.filter(AppUser::enabled)
				.orElseThrow(() -> new InvalidCredentialsException());

		if (!passwordEncoder.matches(normalizeRequiredPassword(currentPassword), user.passwordHash())) {
			throw new InvalidCurrentPasswordException();
		}

		String newPasswordHash = passwordEncoder.encode(normalizeRequiredPassword(newPassword));
		appUserRepository.updatePasswordHash(userId, newPasswordHash);

		LocalDateTime now = LocalDateTime.now(clock);
		refreshTokenRepository.revokeAllForUser(userId, now);

		AppUser updatedUser = new AppUser(
				user.userId(),
				user.email(),
				newPasswordHash,
				user.enabled(),
				user.createdAt());

		return issueTokenPair(updatedUser);
	}

	private AuthTokenPair issueTokenPair(AppUser user) {
		Instant now = clock.instant();
		Instant accessExpiresAt = now.plusSeconds(authProperties.accessTokenTtlSeconds());
		String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(
				JWT_HEADER,
				JwtClaimsSet.builder()
						.subject(user.userId())
						.claim("email", user.email())
						.claim("scope", "ROLE_USER")
						.issuedAt(now)
						.expiresAt(accessExpiresAt)
						.build()))
				.getTokenValue();

		String rawRefreshToken = generateRefreshToken();
		refreshTokenRepository.save(new RefreshTokenRecord(
				UUID.randomUUID().toString(),
				user.userId(),
				hashToken(rawRefreshToken),
				LocalDateTime.ofInstant(now.plusSeconds(authProperties.refreshTokenTtlSeconds()), ZoneOffset.UTC),
				null,
				LocalDateTime.ofInstant(now, ZoneOffset.UTC)));

		return new AuthTokenPair(
				accessToken,
				authProperties.accessTokenTtlSeconds(),
				rawRefreshToken,
				authProperties.refreshTokenTtlSeconds(),
				AuthUserView.from(user));
	}

	private String generateRefreshToken() {
		byte[] bytes = new byte[48];
		secureRandom.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private String hashToken(String token) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(messageDigest.digest(token.getBytes(StandardCharsets.UTF_8)));
		}
		catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 digest is not available", exception);
		}
	}

	private String normalizeEmail(String email) {
		if (email == null || email.isBlank()) {
			throw new IllegalArgumentException("email is required");
		}

		return email.trim().toLowerCase();
	}

	private String normalizeRequiredPassword(String password) {
		if (password == null || password.isBlank()) {
			throw new IllegalArgumentException("password is required");
		}

		String normalizedPassword = password.trim();
		if (normalizedPassword.length() < 8) {
			throw new IllegalArgumentException("password must contain at least 8 characters");
		}

		return normalizedPassword;
	}

	private String normalizeRequiredRefreshToken(String refreshToken) {
		if (refreshToken == null || refreshToken.isBlank()) {
			throw new IllegalArgumentException("refreshToken is required");
		}

		return refreshToken.trim();
	}
}
