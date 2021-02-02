/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas.mediatype.html;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collection;

import org.springframework.core.Constants;
import org.springframework.core.Constants.ConstantException;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The types of HTML {@code <input … />} elements.
 * <p>
 * Note, that there are currently only constants defined for the input types we currently support in any of the media
 * types supported by Spring HATEOAS out of the box. If your media type needs an additional one, please file a ticket.
 *
 * @author Oliver Drotbohm
 * @since 1.3
 * @see https://www.w3.org/TR/html52/sec-forms.html#element-attrdef-input-type
 */
public class HtmlInputType {

	public static final String COLOR_VALUE = "color";
	public static final HtmlInputType COLOR = new HtmlInputType(COLOR_VALUE);

	public static final String DATE_VALUE = "date";
	public static final HtmlInputType DATE = new HtmlInputType(DATE_VALUE);

	public static final String DATETIME_LOCAL_VALUE = "datetime-local";
	public static final HtmlInputType DATETIME_LOCAL = new HtmlInputType(DATETIME_LOCAL_VALUE);

	public static final String EMAIL_VALUE = "email";
	public static final HtmlInputType EMAIL = new HtmlInputType(EMAIL_VALUE);

	public static final String HIDDEN_VALUE = "hidden";
	public static final HtmlInputType HIDDEN = new HtmlInputType(HIDDEN_VALUE);

	public static final String MONTH_VALUE = "month";
	public static final HtmlInputType MONTH = new HtmlInputType(MONTH_VALUE);

	public static final String NUMBER_VALUE = "number";
	public static final HtmlInputType NUMBER = new HtmlInputType(NUMBER_VALUE);

	public static final String PASSWORD_VALUE = "password";
	public static final HtmlInputType PASSWORD = new HtmlInputType(PASSWORD_VALUE);

	public static final String RANGE_VALUE = "range";
	public static final HtmlInputType RANGE = new HtmlInputType(RANGE_VALUE);

	public static final String SEARCH_VALUE = "search";
	public static final HtmlInputType SEARCH = new HtmlInputType(SEARCH_VALUE);

	public static final String TEL_VALUE = "tel";
	public static final HtmlInputType TEL = new HtmlInputType(TEL_VALUE);

	public static final String TEXT_VALUE = "text";
	public static final HtmlInputType TEXT = new HtmlInputType(TEXT_VALUE);

	public static final String TEXTAREA_VALUE = "textarea";
	public static final HtmlInputType TEXTAREA = new HtmlInputType(TEXTAREA_VALUE);

	public static final String TIME_VALUE = "time";
	public static final HtmlInputType TIME = new HtmlInputType(TIME_VALUE);

	public static final String URL_VALUE = "url";
	public static final HtmlInputType URL = new HtmlInputType(URL_VALUE);

	public static final String WEEK_VALUE = "week";
	public static final HtmlInputType WEEK = new HtmlInputType(WEEK_VALUE);

	private static final Constants CONSTANTS = new Constants(HtmlInputType.class);
	static final Collection<Class<?>> NUMERIC_TYPES = Arrays.asList(int.class, long.class, float.class,
			double.class, short.class, Integer.class, Long.class, Float.class, Double.class, Short.class, BigDecimal.class);

	@JsonValue //
	private String value;

	/**
	 * Creates a new {@link HtmlInputType} for the given value.
	 *
	 * @param value must not be {@literal null} or empty.
	 */
	private HtmlInputType(String value) {

		Assert.hasText(value, "Value must not be null or empty!");

		this.value = value;
	}

	/**
	 * Returns the {@link HtmlInputType} for the given string value.
	 *
	 * @param value must not be {@literal null} or empty.
	 * @return the {@link HtmlInputType} or {@literal null} if no match was found.
	 */
	@Nullable
	public static HtmlInputType of(String value) {

		Assert.hasText(value, "Value must not be null or empty!");

		int underscore = value.indexOf('_');
		String lookup = underscore < 0 ? value : value.substring(0, underscore);

		try {
			return (HtmlInputType) CONSTANTS.asObject(lookup);
		} catch (ConstantException o_O) {
			return null;
		}
	}

	/**
	 * Returns the {@link HtmlInputType} derived from the given {@link ResolvableType}.
	 *
	 * @param resolvableType must not be {@literal null}.
	 * @return
	 */
	@Nullable
	public static HtmlInputType from(Class<?> type) {

		Assert.notNull(type, "Type must not be null!");

		if (LocalDate.class.equals(type)) {
			return DATE;
		}

		if (LocalDateTime.class.equals(type)) {
			return DATETIME_LOCAL;
		}

		if (NUMERIC_TYPES.contains(type)) {
			return NUMBER;
		}

		if (URI.class.equals(type) || URL.class.equals(type)) {
			return URL;
		}

		if (String.class.equals(type)) {
			return TEXT;
		}

		if (LocalTime.class.equals(type)) {
			return TIME;
		}

		return null;
	}

	public String value() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return value;
	}
}
