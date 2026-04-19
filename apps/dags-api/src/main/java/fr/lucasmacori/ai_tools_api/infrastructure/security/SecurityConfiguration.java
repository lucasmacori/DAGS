package fr.lucasmacori.ai_tools_api.infrastructure.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties(BasicAuthProperties.class)
public class SecurityConfiguration {

	@Bean
	SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		return http
				.csrf(ServerHttpSecurity.CsrfSpec::disable)
				.formLogin(ServerHttpSecurity.FormLoginSpec::disable)
				.httpBasic(Customizer.withDefaults())
				.authorizeExchange(exchanges -> exchanges.anyExchange().authenticated())
				.build();
	}

	@Bean
	MapReactiveUserDetailsService userDetailsService(BasicAuthProperties properties, PasswordEncoder passwordEncoder) {
		UserDetails user = User.withUsername(properties.username())
				.password(passwordEncoder.encode(properties.password()))
				.roles("USER")
				.build();

		return new MapReactiveUserDetailsService(user);
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}
}
