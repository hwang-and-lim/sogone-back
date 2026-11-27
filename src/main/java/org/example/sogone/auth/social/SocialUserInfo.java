package org.example.sogone.auth.social;

import org.example.sogone.auth.entity.UserAuthProvider;
import org.example.sogone.auth.entity.type.Provider;

public record SocialUserInfo(
	Provider provider,
	String providerUserId,
	String email,
	boolean emailVerified,
	String nickname
) {
	public SocialUserInfo {
		email = (email == null) ? null : UserAuthProvider.normalizeEmail(email);
	}
}
