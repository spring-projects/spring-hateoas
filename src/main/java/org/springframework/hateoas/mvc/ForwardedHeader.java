/*
 * Copyright 2017 the original author or authors.
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
package org.springframework.hateoas.mvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Value object to partially implement the {@literal Forwarded} header defined in RFC 7239.
 *
 * @author Oliver Gierke
 * @see http://tools.ietf.org/html/rfc7239
 */
class ForwardedHeader {

	public static String NAME = "Forwarded";
	private static final ForwardedHeader NO_HEADER = new ForwardedHeader(Collections.emptyMap());

	private final Map<String, String> elements;

	private ForwardedHeader(Map<String, String> elements) {
		this.elements = elements;
	}

	/**
	 * Creates a new {@link ForwardedHeader} from the given source.
	 * 
	 * @param source can be {@literal null}.
	 * @return
	 */
	public static ForwardedHeader of(String source) {

		if (!StringUtils.hasText(source)) {
			return NO_HEADER;
		}

		Map<String, String> elements = Arrays.stream(source.split(";"))
				.map(part -> part.split("="))
				.filter(keyValue -> keyValue.length == 2)
				.collect(Collectors.toMap((it) -> it[0].trim(), (it) -> it[1].trim()));

		Assert.isTrue(!elements.isEmpty(), "At least one forwarded element needs to be present!");

		return new ForwardedHeader(elements);
	}

	/**
	 * Returns the value defined for the {@code proto} parameter of the header.
	 * 
	 * @return
	 */
	public String getProto() {
		return elements.get("proto");
	}

	/**
	 * Returns the value defined for the {@code host} parameter of the header.
	 * 
	 * @return
	 */
	public String getHost() {
		return elements.get("host");
	}
}
