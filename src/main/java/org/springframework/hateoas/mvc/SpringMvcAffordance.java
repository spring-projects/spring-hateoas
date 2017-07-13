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

import lombok.Data;

import java.lang.reflect.Method;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.AffordanceModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Spring MVC-based representation of an {@link Affordance}.
 * 
 * @author Greg Turnquist
 */
@Data
public class SpringMvcAffordance implements Affordance {

	private static final Logger log = LoggerFactory.getLogger(SpringMvcAffordance.class);

	private final HashMap<MediaType, AffordanceModel> affordanceModels;

	/**
	 * Request method verb associated with the Spring MVC controller method.
	 */
	private final RequestMethod requestMethod;

	/**
	 * Handle on the Spring MVC controller {@link Method}.
	 */
	private final Method method;

	/**
	 * Construct a Spring MVC-based {@link Affordance} based on Spring MVC controller method and {@link RequestMethod}.
	 */
	public SpringMvcAffordance(RequestMethod requestMethod, Method method) {

		this.requestMethod = requestMethod;
		this.method = method;
		this.affordanceModels = new HashMap<MediaType, AffordanceModel>();
	}

	@Override
	public String getHttpMethod() {
		return this.requestMethod.toString();
	}

	@Override
	public String getName() {
		return this.method.getName();
	}

	@Override
	public AffordanceModel getAffordanceModel(MediaType mediaType) {
		return this.affordanceModels.get(mediaType);
	}

	@Override
	public void addAffordanceModel(MediaType mediaType, AffordanceModel affordanceModel) {
		this.affordanceModels.put(mediaType, affordanceModel);
	}
}
