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
package org.springframework.hateoas.hal.forms;

import lombok.Getter;

import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.AffordanceModel;
import org.springframework.hateoas.AffordanceModelFactory;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.core.DummyInvocationUtils.MethodInvocation;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponents;

/**
 * Factory for creating {@link HalFormsAffordanceModel}s.
 *
 * @author Greg Turnquist
 */
@Getter
public class HalFormsAffordanceModelFactory extends AffordanceModelFactory {

	private final MediaType mediaType = MediaTypes.HAL_FORMS_JSON;
	
	@Override
	public AffordanceModel getAffordanceModel(Affordance affordance, MethodInvocation invocationValue, UriComponents components) {
		return new HalFormsAffordanceModel(affordance, invocationValue, components);
	}
}
