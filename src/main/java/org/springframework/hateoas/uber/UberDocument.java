/*
 * Copyright 2018 the original author or authors.
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
package org.springframework.hateoas.uber;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Wither;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Top-level element in an UBER representation.
 *
 * @author Greg Turnquist
 * @since 1.0
 */
@Value
@Wither(AccessLevel.PACKAGE)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class UberDocument {

	private Uber uber;

	@JsonCreator
	UberDocument(@JsonProperty("version") String version, @JsonProperty("data") List<UberData> data,
				 @JsonProperty("error") UberError error) {
		this.uber = new Uber(version, data, error);
	}

	UberDocument() {
		this("1.0", null, null);
	}

	/**
	 * Transform an object into a {@link UberDocument}.
	 *
	 * @param object
	 * @return
	 */
	static UberDocument toUberDocument(final Object object) {

		if (object == null) {
			return null;
		}

		if (object instanceof UberDocument) {
			return (UberDocument) object;
		}

		throw new IllegalArgumentException("Don't know how to handle type : " + object.getClass());
	}
}