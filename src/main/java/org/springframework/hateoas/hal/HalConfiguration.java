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
package org.springframework.hateoas.hal;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

import org.springframework.hateoas.Link;

/**
 * @author Greg Turnquist
 * @author Oliver Gierke
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HalConfiguration {

	private @Wither @Getter RenderSingleLinks renderSingleLinks = RenderSingleLinks.AS_SINGLE;

	public enum RenderSingleLinks {

		/**
		 * A single {@link Link} is rendered as a JSON object.
		 */
		AS_SINGLE,

		/**
		 * A single {@link Link} is rendered as a JSON Array.
		 */
		AS_ARRAY
	}
}
