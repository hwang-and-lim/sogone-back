package org.example.sogone.redis;

import java.time.Duration;
import java.util.Objects;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

// 단일 데이터를 처리하는 비즈니스 로직
@Service
@RequiredArgsConstructor
public class RedisSingleDataService {
	private final StringRedisTemplate redisTemplate;

	/**
	 * 단일 값 등록/수정
	 *
	 * @param key
	 * @param value
	 * @return
	 */
	public void setSingleData(String key, String value) {
		redisTemplate.opsForValue().set(key, value);
	}

	/**
	 * 단일 값을 등록/수정(duration 값이 존재하면 메모리 상 유효시간을 지정
	 *
	 * @param key
	 * @param value
	 * @param duration
	 * @return
	 */
	public void setSingleData(String key, String value, Duration duration) {
		redisTemplate.opsForValue().set(key, value, duration);
	}

	/**
	 * 키를 기반으로 단일 데이터의 값을 조회
	 *
	 * @param key
	 * @return String
	 */
	public String getSingleData(String key) {
		return redisTemplate.opsForValue().get(key);
	}

	/**
	 * 키를 기반으로 단일 데이터 값을 삭제
	 *
	 * @param key
	 * @return int
	 */
	public void deleteSingleData(String key) {
		redisTemplate.delete(key);
	}

	public boolean isMatchCode(String key, String value) {
		return Objects.equals(getSingleData(key), value);
	}

	public Long increaseAttemptsCount(String key) {
		return redisTemplate.opsForValue().increment(key);
	}

	public void setExpire(String key, Duration ttl) {
		redisTemplate.expire(key, ttl);
	}
}
