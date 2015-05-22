/*
 * Copyright 2015 the original author or authors.
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
package org.springframework.hateoas.client;

import java.util.HashMap;
import java.util.Map;

import lombok.Value;

/**
 * Container for cuztomizations to a single traverson "hop"
 *
 * @author Greg Turnquist
 */
@Value(staticConstructor="rel")
public class Hop {

	/**
	 * Name of this hop.
	 */
	private final String rel;

	/**
	 * Collection of URI Template parameters.
	 */
	private final Map<String, String> params = new HashMap<String, String>();

	/**
	 * Add one parameter to the map of parameters.
	 *
	 * @param name
	 * @param value
	 * @return fluent DSL (Lombok @Wither didn't seem to work)
	 */
	public Hop withParam(String name, String value) {
		this.params.put(name, value);
		return this;
	}

	/**
	 * Add a collection of parameters (does NOT clear out existing ones).
	 *
	 * @param newParams
	 * @return fluent DSL (Lombok @Wither didn't seem to work)
	 */
	public Hop withParams(Map<String, String> newParams) {
		this.params.putAll(newParams);
		return this;
	}

	/**
	 * Create a new {@link Map} starting with the supplied template parameters. Then add the ones for this hop. This
	 * allows a local hop to override global parameters.
	 *
	 * @param globalParameters
	 * @return a merged map of URI Template parameters
	 */
	public Map<String, Object> getMergedParameteres(Map<String, Object> globalParameters) {
		Map<String, Object> mergedParameters = new HashMap<String, Object>();
		mergedParameters.putAll(globalParameters);
		mergedParameters.putAll(this.params);
		return mergedParameters;
	}
}
