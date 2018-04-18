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
package org.springframework.hateoas;

import lombok.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.ResolvableType;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.hateoas.core.AffordanceModelFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;

/**
 * Hold all the {@link GenericAffordanceModel}s for all supported media types.
 *
 * @author Greg Turnquist
 * @author Oliver Gierke
 */
@Value
public class Affordance {

	private static List<AffordanceModelFactory> factories = SpringFactoriesLoader.loadFactories(AffordanceModelFactory.class, Affordance.class.getClassLoader());

	/**
	 * Collection of {@link GenericAffordanceModel}s related to this affordance.
	 */
	private final Map<MediaType, GenericAffordanceModel> affordanceModels = new HashMap<>();

	public Affordance(String name, Link link, HttpMethod httpMethod, ResolvableType inputType, List<QueryParameter> queryMethodParameters, ResolvableType outputType) {

		Assert.notNull(httpMethod, "httpMethod must not be null!");
		Assert.notNull(queryMethodParameters, "queryMethodParameters must not be null!");

		for (AffordanceModelFactory factory : factories) {
			this.affordanceModels.put(factory.getMediaType(), factory.getAffordanceModel(name, link, httpMethod, inputType, queryMethodParameters, outputType));
		}
	}

	/**
	 * Look up the {@link GenericAffordanceModel} for the requested {@link MediaType}.
	 *
	 * @param mediaType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends GenericAffordanceModel> T getAffordanceModel(MediaType mediaType) {
		return (T) this.affordanceModels.get(mediaType);
	}
}
