package org.example.sogone.auth.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;

import org.apache.commons.validator.routines.EmailValidator;
import org.example.sogone.auth.dto.EmailSendRequestDTO;
import org.example.sogone.auth.dto.EmailVerifyCodeRequestDTO;
import org.example.sogone.exception.BusinessException;
import org.example.sogone.exception.ErrorCode;
import org.example.sogone.redis.RedisSingleDataService;
import org.example.sogone.util.EmailNormalizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerifyService {
	private final JavaMailSender javaMailSender;
	private final RedisSingleDataService redisSingleDataService;

	private final static Duration TTL_1_HOURS = Duration.ofHours(1);
	private final static Duration TTL_10_MINUTES = Duration.ofMinutes(10);
	private final static Duration TTL_5_MINUTES = Duration.ofMinutes(5);
	private final static String EMAIL_VERIFY_CODE = "email-verify:code:";
	private final static String EMAIL_VERIFY_ATTEMPTS = "email-verify:attempts:";
	private final static String EMAIL_VERIFY_SEND_COUNT = "email-verify:send-count:";
	private final static String EMAIL_VERIFIED = "email-verify:verified:";
	private final static String EMAIL_VERIFIED_VALUE = "1";

	@Value("${spring.mail.username}")
	private String serviceName;

	public void joinEmail(EmailSendRequestDTO request) {
		String email = EmailNormalizer.normalizeEmail(request.email());
		if (!EmailValidator.getInstance().isValid(email)) {
			throw new BusinessException(ErrorCode.INVALID_EMAIL);
		}

		String sendCountKey = EMAIL_VERIFY_SEND_COUNT + email;
		checkAndIncreaseAttempts(sendCountKey, TTL_1_HOURS);

		String authCode = makeRandomNum();

		try {
			String attemptsKey = EMAIL_VERIFY_ATTEMPTS + email;
			String key = EMAIL_VERIFY_CODE + email;
			redisSingleDataService.setSingleData(key, authCode, TTL_10_MINUTES);
			redisSingleDataService.deleteSingleData(attemptsKey);
		} catch (Exception e) {
			throw new IllegalStateException("Redis 작업 중 오류가 발생했습니다.");
		}

		String title = "[소곤] 이메일 인증 코드";
		String content = buildContent(authCode);
		mailSend(serviceName, email, title, content, authCode);
	}

	public void verifyEmailCode(EmailVerifyCodeRequestDTO request) {
		String email = EmailNormalizer.normalizeEmail(request.email());
		if (!EmailValidator.getInstance().isValid(email)) {
			throw new BusinessException(ErrorCode.INVALID_EMAIL);
		}

		if (request.code() == null || request.code().isBlank()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}

		String key = EMAIL_VERIFY_CODE + email;
		String attemptsKey = EMAIL_VERIFY_ATTEMPTS + email;
		String attempts = redisSingleDataService.getSingleData(attemptsKey);
		if (attempts != null && Integer.parseInt(attempts) >= 5) {
			throw new BusinessException(ErrorCode.TOO_MANY_ATTEMPTS);
		}

		if (!redisSingleDataService.isMatchCode(key, request.code())) {
			checkAndIncreaseAttempts(attemptsKey, TTL_10_MINUTES);

			throw new BusinessException(ErrorCode.NO_MATCH_CODE);
		}

		redisSingleDataService.deleteSingleData(key);

		String successKey = EMAIL_VERIFIED + email;
		redisSingleDataService.setSingleData(successKey, EMAIL_VERIFIED_VALUE, TTL_10_MINUTES);
	}

	public void consumeVerifiedEmail(String email) {
		String key = EMAIL_VERIFIED + email;
		if (!EMAIL_VERIFIED_VALUE.equals(redisSingleDataService.getSingleData(key))) {
			throw new BusinessException(ErrorCode.NOT_VERIFY_EMAIL);
		}

		redisSingleDataService.deleteSingleData(key);
	}

	// 랜덤 인증번호 생성
	private String makeRandomNum() {
		SecureRandom random = new SecureRandom();
		StringBuffer randomNumber = new StringBuffer();
		for (int i = 0; i < 6; i++) {
			randomNumber.append(Integer.toString(random.nextInt(10)));
		}

		return randomNumber.toString();
	}

	private void mailSend(String setFrom, String toMail, String title, String content, String code) {
		MimeMessage message = javaMailSender.createMimeMessage();
		try {
			MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.toString());
			helper.setFrom(setFrom);
			helper.setTo(toMail);
			helper.setSubject(title);
			helper.setText(content, true);

			javaMailSender.send(message);
		} catch (MessagingException e) {
			throw new IllegalStateException("메일 전송에 실패했습니다.");
		}
	}

	private String buildContent(String code) {
		try {
			String template = new ClassPathResource("templates/mail/verification-code.html")
				.getContentAsString(StandardCharsets.UTF_8);

			return template.replace("{{code}}", code)
				.replace("{{ttl}}", String.valueOf(TTL_10_MINUTES.toMinutes()));
		} catch (IOException e) {
			log.error(e.getMessage());
			throw new IllegalStateException("메일 템플릿을 읽을 수 없습니다.");
		}
	}

	private void checkAndIncreaseAttempts(String key, Duration ttl) {
		Long attempts = redisSingleDataService.increaseAttemptsCount(key);
		if (attempts != null && attempts == 1) {
			redisSingleDataService.setExpire(key, ttl);
		}

		if (attempts != null && attempts > 5) {
			throw new BusinessException(ErrorCode.TOO_MANY_ATTEMPTS);
		}
	}
}
