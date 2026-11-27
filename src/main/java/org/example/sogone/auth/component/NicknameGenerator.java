package org.example.sogone.auth.component;

import java.security.SecureRandom;
import java.util.HexFormat;

import org.example.sogone.user.repository.UserRepository;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NicknameGenerator {
	private static final int MAX_RETRY = 5;
	private final UserRepository userRepository;
	private final SecureRandom random = new SecureRandom();

	public String resolve(String socialNickname) {
		if (isUsable(socialNickname)) {
			return socialNickname.trim();
		}

		for (int i = 0; i < MAX_RETRY; i++) {
			String candidate = "사용자_" + randomSuffix();
			if (!userRepository.existsByNickname(candidate)) {
				return candidate;
			}
		}
		throw new IllegalStateException("임시 닉네임 생성 실페");
	}

	private boolean isUsable(String nickname) {
		if (nickname == null)
			return false;

		String n = nickname.trim();
		return n.length() >= 2 && n.length() <= 30 &&
			!userRepository.existsByNickname(n);
	}

	private String randomSuffix() {
		byte[] bytes = new byte[4];
		random.nextBytes(bytes);
		return HexFormat.of().formatHex(bytes);
	}
}
