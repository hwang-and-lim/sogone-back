package org.example.sogone.user.entity.type;

public enum Role {
	USER,
	ADMIN;

	public String getAuthority() {
		return "ROLE_" + name();
	}
}
