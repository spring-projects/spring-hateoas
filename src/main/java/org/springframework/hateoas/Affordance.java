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

import java.util.List;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 * Abstract representation of an action a link is able to take. Web frameworks must provide concrete implementation.
 *
 * @author Greg Turnquist
 * @author Oliver Gierke
 */
public interface Affordance {

	/**
	 * HTTP method this affordance covers. For multiple methods, add multiple {@link Affordance}s.
	 *
	 * @return
	 */
	HttpMethod getHttpMethod();

	/**
	 * Name for the REST action this {@link Affordance} can take.
	 *
	 * @return
	 */
	String getName();

	/**
	 * Look up the {@link AffordanceModel} for the requested {@link MediaType}.
	 *
	 * @param mediaType
	 * @return
	 */
	<T extends AffordanceModel> T getAffordanceModel(MediaType mediaType);

	/**
	 * Get a listing of {@link MethodParameter}s.
	 *
	 * @return
	 */
	List<MethodParameter> getInputMethodParameters();

	/**
	 * Get a listing of {@link QueryParameter}s.
	 * @return
	 */
	List<QueryParameter> getQueryMethodParameters();
}
