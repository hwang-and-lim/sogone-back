package org.example.sogone.user.service;

import org.example.sogone.exception.BusinessException;
import org.example.sogone.exception.ErrorCode;
import org.example.sogone.user.entity.User;
import org.example.sogone.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;

	public User save(String nickname) {
		if (userRepository.existsByNickname(nickname)) {
			throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
		}

		User newUser = User.builder().nickname(nickname).build();
		userRepository.save(newUser);

		return newUser;
	}
}
