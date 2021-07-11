package org.springframework.hateoas.mediatype;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.NamingBase;

/*
 * Utility class for converting name cases e.g. camelCase to snake_case.
 */
public class StringUtils {
	
	public static String convertCase(NamingBase strategy, String input) {
		return strategy.translate(input);
	}

}
