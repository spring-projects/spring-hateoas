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
package org.springframework.hateoas.core;

import java.util.Optional;

import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.AffordanceModel;
import org.springframework.hateoas.core.DummyInvocationUtils.MethodInvocation;
import org.springframework.http.MediaType;
import org.springframework.plugin.core.Plugin;
import org.springframework.web.util.UriComponents;

/**
 * TODO: Replace this with an interface and a default implementation of {@link #supports(MediaType)} in Java 8.
 * 
 * @author Greg Turnquist
 * @author Oliver Gierke
 */
public interface AffordanceModelFactory extends Plugin<MediaType> {

	/**
	 * Declare the {@link MediaType} this factory supports.
	 * 
	 * @return
	 */
	default MediaType getMediaType() {
		return null;
	};

	/**
	 * Look up the {@link AffordanceModel} for this factory.
	 * 
	 * @param affordance
	 * @param invocationValue
	 * @param components
	 * @return
	 */
	AffordanceModel getAffordanceModel(Affordance affordance, MethodInvocation invocationValue, UriComponents components);

	/**
	 * Returns if a plugin should be invoked according to the given delimiter.
	 *
	 * @param delimiter
	 * @return if the plugin should be invoked
	 */
	@Override
	default boolean supports(MediaType delimiter) {
		return Optional.ofNullable(getMediaType())
			.map(mediaType -> mediaType.equals(delimiter))
			.orElse(false);
	}
}
