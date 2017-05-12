/*
 * Copyright 2013-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.hateoas.affordance.support;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

/**
 * Collection of utility methods to handle various data types.
 *
 * @author Dietrich Schulten
 * @author Greg Turnquist
 */
public final class DataTypeUtils {

	/**
	 * Determines if the given class holds only one data item. Can be useful to determine if a value should be rendered as
	 * scalar.
	 *
	 * @param clazz to check
	 * @return true if class is scalar
	 */
	public static boolean isSingleValueType(Class<?> clazz) {

		return isNumber(clazz) ||
			isBoolean(clazz) ||
			isString(clazz) ||
			isEnum(clazz) ||
			isDate(clazz) ||
			isCalendar(clazz) ||
			isCurrency(clazz);
	}

	/**
	 * Is this class a container of multiple values?
	 * 
	 * @param parameterType
	 * @return
	 */
	public static boolean isArrayOrIterable(Class<?> parameterType) {
		return (parameterType.isArray() || Iterable.class.isAssignableFrom(parameterType));
	}

	/**
	 * Determine if the given class is of a numeric type.
	 * 
	 * @param clazz
	 * @return
	 */
	public static boolean isNumber(Class<?> clazz) {

		return Number.class.isAssignableFrom(clazz) ||
			isInteger(clazz) ||
			isLong(clazz) ||
			isFloat(clazz) ||
			isDouble(clazz) ||
			isByte(clazz) ||
			isShort(clazz);
	}

	/*
	 * Check various numeric types, boxed, and scalar.
	 */

	public static boolean isInteger(Class<?> clazz) {
		return (Integer.class.isAssignableFrom(clazz) || int.class == clazz);
	}

	public static boolean isLong(Class<?> clazz) {
		return (Long.class.isAssignableFrom(clazz) || long.class == clazz);
	}

	public static boolean isFloat(Class<?> clazz) {
		return (Float.class.isAssignableFrom(clazz) || float.class == clazz);
	}

	public static boolean isDouble(Class<?> clazz) {
		return (Double.class.isAssignableFrom(clazz) || double.class == clazz);
	}

	public static boolean isByte(Class<?> clazz) {
		return (Byte.class.isAssignableFrom(clazz) || byte.class == clazz);
	}

	public static boolean isShort(Class<?> clazz) {
		return (Short.class.isAssignableFrom(clazz) || short.class == clazz);
	}

	public static boolean isBigInteger(Class<?> clazz) {
		return BigInteger.class.isAssignableFrom(clazz);
	}

	public static boolean isBigDecimal(Class<?> clazz) {
		return BigDecimal.class.isAssignableFrom(clazz);
	}

	/*
	 * Check other Java types
	 */

	public static boolean isBoolean(Class<?> clazz) {
		return (Boolean.class.isAssignableFrom(clazz) || boolean.class == clazz);
	}

	public static boolean isEnum(Class<?> clazz) {
		return Enum.class.isAssignableFrom(clazz);
	}

	public static boolean isString(Class<?> parameterType) {
		return String.class.isAssignableFrom(parameterType);
	}

	public static boolean isDate(Class<?> clazz) {
		return Date.class.isAssignableFrom(clazz);
	}

	public static boolean isCalendar(Class<?> clazz) {
		return Calendar.class.isAssignableFrom(clazz);
	}

	public static boolean isCurrency(Class<?> clazz) {
		return Currency.class.isAssignableFrom(clazz);
	}

	/**
	 * Convert a string-based value into it's proper type.
	 *
	 * @param type
	 * @param value
	 * @return
	 */
	public static Object asType(Class<?> type, String value) {

		if (isBoolean(type)) {
			return Boolean.parseBoolean(value);
		} else if (isInteger(type)) {
			return Integer.parseInt(value);
		} else if (isLong(type)) {
			return Long.parseLong(value);
		} else if (isDouble(type)) {
			return Double.parseDouble(value);
		} else if (isFloat(type)) {
			return Float.parseFloat(value);
		} else if (isByte(type)) {
			return Byte.parseByte(value);
		} else if (isShort(type)) {
			return Short.parseShort(value);
		} else if (isBigInteger(type)) {
			return new BigInteger(value);
		} else if (isBigDecimal(type)) {
			return new BigDecimal(value);
		} else if (isCalendar(type)) {
			return DatatypeConverter.parseDateTime(value);
		} else if (isDate(type)) {
			if (isIsoLatin1Number(value)) {
				return new Date(Long.parseLong(value));
			} else {
				return DatatypeConverter.parseDateTime(value).getTime();
			}
		} else if (isCurrency(type)) {
			return Currency.getInstance(value);
		} else if (type.isEnum()) {
			return Enum.valueOf((Class<? extends Enum>) type, value);
		} else {
			return value;
		}
	}

	/**
	 * Determines if the given string contains only 0-9 [ISO-LATIN-1] or an optional leading +/- sign.
	 *
	 * @param value to check
	 * @return true if condition holds, false otherwise
	 * @see <a href="http://stackoverflow.com/a/29331473/743507">Comparison of regex and char array performance</a>
	 * @see Character#isDigit Examples for non-ISO-Latin-1-Digits
	 */
	public static boolean isIsoLatin1Number(String value) {

		// For null or empty strings, no match
		if (value == null || value.isEmpty()) {
			return false;
		}

		// Start from the beginning
		int index = 0;

		// If it starts with + or -, skip it
		if (value.startsWith("-") || value.startsWith("+")) {
			index = 1;
		}

		// Iterate over remaining digits, and break out when the first one fails to be a digit
		for (char[] data = value.toCharArray(); index < data.length; index++) {
			if (data[index] < '0' || data[index] > '9') {
				return false;
			}
		}
		return true;
	}
}
