package org.example.sogone.auth.service;

import java.time.Instant;

import org.example.sogone.auth.dto.SignupRequestDTO;
import org.example.sogone.auth.dto.SignupResponseDTO;
import org.example.sogone.auth.entity.UserAuthProvider;
import org.example.sogone.auth.entity.type.Provider;
import org.example.sogone.auth.repository.UserAuthProviderRepository;
import org.example.sogone.exception.CustomException;
import org.example.sogone.exception.ErrorCode;
import org.example.sogone.user.entity.User;
import org.example.sogone.user.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final UserService userService;
	private final UserAuthProviderRepository authProviderRepository;

	@Transactional
	public SignupResponseDTO signup(SignupRequestDTO request) {
		String email = UserAuthProvider.normalizeEmail(request.getEmail());
		if (authProviderRepository.existsByProviderAndProviderUserId(Provider.LOCAL, email)) {
			throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
		}

		User newUser = userService.save(request.getNickname());

		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

		UserAuthProvider newAuthProvider = UserAuthProvider.ofLocal(
			newUser,
			email,
			encoder.encode(request.getPassword()),
			Instant.now()
		);

		authProviderRepository.save(newAuthProvider);

		return SignupResponseDTO.of(newUser, newAuthProvider);
	}
}
