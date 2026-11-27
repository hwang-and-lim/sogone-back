package org.example.sogone.auth.social.kakao;

import org.example.sogone.auth.entity.type.Provider;
import org.example.sogone.auth.social.SocialUserInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoResponse() {
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record KakaoTokenInfoResponse(Long id, @JsonProperty("app_id") Long appId) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record KakaoTokenResponse(@JsonProperty("access_token") String accessToken) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record KakaoUserResponse(Long id, @JsonProperty("kakao_account") KakaoAccount kakaoAccount) {
		@JsonIgnoreProperties(ignoreUnknown = true)
		public record KakaoAccount(
			String email,
			@JsonProperty("is_email_valid") boolean isEmailValid,
			@JsonProperty("is_email_verified") boolean isEmailVerified,
			Profile profile
		) {
		}

		@JsonIgnoreProperties(ignoreUnknown = true)
		public record Profile(String nickname) {
		}

		public SocialUserInfo toSocialUserInfo() {
			KakaoAccount account = kakaoAccount;
			String email = (account != null) ? account.email() : null;
			boolean verified = account != null
				&& Boolean.TRUE.equals(account.isEmailValid)
				&& Boolean.TRUE.equals(account.isEmailVerified);
			String nickname = (account != null && account.profile() != null)
				? account.profile().nickname() : null;
			
			return new SocialUserInfo(Provider.KAKAO, String.valueOf(id), email, verified, nickname);
		}
	}
}
