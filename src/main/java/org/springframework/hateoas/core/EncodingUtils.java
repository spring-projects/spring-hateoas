/*
 * Copyright 2016 the original author or authors.
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
package org.springframework.hateoas.core;

import java.io.UnsupportedEncodingException;

import lombok.experimental.UtilityClass;

import org.springframework.util.Assert;
import org.springframework.web.util.UriUtils;

/**
 * Utilities for URI encoding.
 * 
 * @author Oliver Gierke
 * @since 0.22
 * @soundtrack Don Philippe - Between Now And Now (Between Now And Now)
 */
@UtilityClass
public class EncodingUtils {

	private static final String ENCODING = "UTF-8";

	/**
	 * Encodes the given path value.
	 * 
	 * @param source must not be {@literal null}.
	 * @return
	 */
	public static String encodePath(Object source) {

		Assert.notNull(source, "Path value must not be null!");

		try {
			return UriUtils.encodePath(source.toString(), ENCODING);
		} catch (UnsupportedEncodingException o_O) {
			throw new IllegalStateException(o_O);
		}
	}

	/**
	 * Encodes the given request parameter value.
	 * 
	 * @param source must not be {@literal null}.
	 * @return
	 */
	public static String encodeParameter(Object source) {

		Assert.notNull(source, "Request parameter value must not be null!");

		try {
			return UriUtils.encodeQueryParam(source.toString(), ENCODING);
		} catch (UnsupportedEncodingException o_O) {
			throw new IllegalStateException(o_O);
		}
	}

	/**
	 * Encodes the given fragment value.
	 * 
	 * @param source must not be {@literal null}.
	 * @return
	 */
	public static String encodeFragment(Object source) {

		Assert.notNull(source, "Fragment value must not be null!");

		try {
			return UriUtils.encodeFragment(source.toString(), ENCODING);
		} catch (UnsupportedEncodingException o_O) {
			throw new IllegalStateException(o_O);
		}
	}
}
