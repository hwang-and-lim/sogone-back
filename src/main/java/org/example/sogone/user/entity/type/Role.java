package org.example.sogone.user.entity.type;

public enum Role {
	USER,
	ADMIN;

	public String getAuthority() {
		return "ROLE_" + name();
	}

	public static String getRole(String roleName) {
		return "ROLE_" + roleName;
	}
}
