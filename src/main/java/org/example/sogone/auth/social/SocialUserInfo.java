package org.example.sogone.auth.social;

import org.example.sogone.auth.entity.type.Provider;
import org.example.sogone.util.EmailNormalizer;

public record SocialUserInfo(
	Provider provider,
	String providerUserId,
	String email,
	boolean emailVerified,
	String nickname
) {
	public SocialUserInfo {
		email = (email == null) ? null : EmailNormalizer.normalizeEmail(email);
	}
}
