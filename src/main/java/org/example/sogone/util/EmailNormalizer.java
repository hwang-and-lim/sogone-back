package org.example.sogone.util;

import java.util.Locale;

public class EmailNormalizer {
	public static String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
