package org.example.sogone.auth.social;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.example.sogone.auth.entity.type.Provider;
import org.example.sogone.exception.BusinessException;
import org.example.sogone.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class SocialLoginClientRegistry {
	private final Map<Provider, SocialLoginClient> clients;

	public SocialLoginClientRegistry(List<SocialLoginClient> clients) {
		this.clients = clients.stream()
			.collect(Collectors.toUnmodifiableMap(SocialLoginClient::provider, Function.identity()));
	}

	public SocialLoginClient get(Provider provider) {
		SocialLoginClient client = clients.get(provider);
		if (client == null) {
			throw new BusinessException(ErrorCode.UNSUPPORTED_PROVIDER);
		}

		return client;
	}
}
