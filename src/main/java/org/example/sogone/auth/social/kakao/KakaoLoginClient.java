package org.example.sogone.auth.social.kakao;

import java.time.Duration;
import java.util.function.Supplier;

import org.example.sogone.auth.dto.SocialLoginRequestDTO;
import org.example.sogone.auth.entity.type.Provider;
import org.example.sogone.auth.social.OAuthProperties;
import org.example.sogone.auth.social.SocialLoginClient;
import org.example.sogone.auth.social.SocialUserInfo;
import org.example.sogone.exception.BusinessException;
import org.example.sogone.exception.ErrorCode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class KakaoLoginClient implements SocialLoginClient {
	private final RestClient apiClient;
	private final RestClient authClient;
	private final OAuthProperties.Kakao props;
	private final static String KAPI_BASE_URL = "https://kapi.kakao.com";
	private final static String KAUTH_BASE_URL = "https://kauth.kakao.com";

	public KakaoLoginClient(RestClient.Builder builder, OAuthProperties properties) {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(Duration.ofSeconds(3));
		factory.setReadTimeout(Duration.ofSeconds(3));

		this.apiClient = builder.clone().requestFactory(factory).baseUrl(KAPI_BASE_URL).build();
		this.authClient = builder.clone().requestFactory(factory).baseUrl(KAUTH_BASE_URL).build();
		this.props = properties.kakao();
	}

	@Override
	public Provider provider() {
		return Provider.KAKAO;
	}

	@Override
	public SocialUserInfo getUserInfo(SocialLoginRequestDTO request) {
		String accessToken = (request.code() != null && !request.code().isBlank())
			? exchangeCode(request.code())
			: request.token();

		verifyAppId(accessToken);
		return fetchUser(accessToken).toSocialUserInfo();
	}

	// 웹: 인가 코드 -> access token
	private String exchangeCode(String code) {
		MultiValueMap<String, String> from = new LinkedMultiValueMap<>();
		from.add("grant_type", "authorization_code");
		from.add("client_id", props.restApiKey());
		from.add("client_secret", props.clientSecret());
		from.add("redirect_uri", props.redirectUri());
		from.add("code", code);

		KakaoResponse.KakaoTokenResponse response = call(() -> authClient.post().uri("/oauth/token")
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.body(from)
			.retrieve()
			.body(KakaoResponse.KakaoTokenResponse.class));

		return response.accessToken();
	}

	// 다른 앱에서 발급된 토큰 차단
	private void verifyAppId(String accessToken) {
		KakaoResponse.KakaoTokenInfoResponse info = call(() -> apiClient.get().uri("/v1/user/access_token_info")
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
			.retrieve()
			.body(KakaoResponse.KakaoTokenInfoResponse.class));

		if (info.appId() == null || info.appId() != props.appId()) {
			throw new BusinessException(ErrorCode.INVALID_SOCIAL_TOKEN);
		}
	}

	private KakaoResponse.KakaoUserResponse fetchUser(String accessToken) {
		return call(() -> apiClient.get().uri("/v2/user/me")
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
			.retrieve()
			.body(KakaoResponse.KakaoUserResponse.class));
	}

	private <T> T call(Supplier<T> request) {
		try {
			return request.get();
		} catch (HttpClientErrorException e) {
			throw new BusinessException(ErrorCode.INVALID_SOCIAL_TOKEN);
		} catch (RestClientException e) {
			throw new BusinessException(ErrorCode.EXTERNAL_API_UNAVAILABLE);
		}
	}
}
