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
package org.springframework.hateoas;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Wither;

/**
 * Value object to hold various Affordance-based properties.
 * 
 * @author Greg Turnquist
 */
@Value
@Wither
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AffordancePropertyDetails {

	private final String prompt;
	private final String pattern;
	private final Boolean readOnly;

	public AffordancePropertyDetails(AffordanceProperty affordanceProperty) {

		if (affordanceProperty == null) {
			this.prompt = null;
			this.pattern = null;
		} else {
			this.prompt = affordanceProperty.prompt();
			this.pattern = affordanceProperty.pattern();
		}
		
		this.readOnly = null;
	}

	public AffordancePropertyDetails withAffordancePattern(AffordancePattern affordancePattern) {

		if (affordancePattern == null) {
			return this;
		}

		return this.withPattern(affordancePattern.value());
	}

	public AffordancePropertyDetails withAffordancePrompt(AffordancePrompt affordancePrompt) {

		if (affordancePrompt == null) {
			return this;
		}

		return this.withPrompt(affordancePrompt.value());
	}

	public AffordancePropertyDetails withAffordanceReadOnly(AffordanceReadOnly affordanceReadOnly) {

		if (affordanceReadOnly == null) {
			return this;
		}
		
		return this.withReadOnly(affordanceReadOnly.value());
	}
}
