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

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.AffordanceModel;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Spring MVC-based representation of an {@link Affordance}.
 * 
 * @author Greg Turnquist
 */
@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class SpringMvcAffordance implements Affordance {

	/**
	 * Request method verb associated with the Spring MVC controller method.
	 */
	private final HttpMethod httpMethod;

	/**
	 * Handle on the Spring MVC controller {@link Method}.
	 */
	private final Method method;
	private final Map<MediaType, AffordanceModel> affordanceModels;

	/**
	 * Construct a Spring MVC-based {@link Affordance} based on Spring MVC controller method and {@link RequestMethod}.
	 */
	public SpringMvcAffordance(HttpMethod httpMethod, Method method) {
		this(httpMethod, method, new HashMap<MediaType, AffordanceModel>());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.Affordance#getName()
	 */
	@Override
	public String getName() {
		return this.method.getName();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.Affordance#getAffordanceModel(org.springframework.http.MediaType)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T extends AffordanceModel> T getAffordanceModel(MediaType mediaType) {
		return (T) this.affordanceModels.get(mediaType);
	}

	/**
	 * Adds the given {@link AffordanceModel} to the {@link Affordance}.
	 * 
	 * @param affordanceModel must not be {@literal null}.
	 */
	public void addAffordanceModel(AffordanceModel affordanceModel) {

		Assert.notNull(affordanceModel, "Affordance model must not be null!");

		for (MediaType mediaType : affordanceModel.getMediaTypes()) {
			this.affordanceModels.put(mediaType, affordanceModel);
		}
	}
}
