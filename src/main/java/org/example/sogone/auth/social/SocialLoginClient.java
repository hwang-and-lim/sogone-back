package org.example.sogone.auth.social;

import org.example.sogone.auth.dto.SocialLoginRequestDTO;
import org.example.sogone.auth.entity.type.Provider;

public interface SocialLoginClient {
	Provider provider();

	SocialUserInfo getUserInfo(SocialLoginRequestDTO request);
}
