/*
 * Copyright 2012-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas.mediatype.hal;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule.TrueOnlyBooleanSerializer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Custom mixin to avoid rel attributes being rendered for HAL.
 *
 * @author Alexander Baetz
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
@JsonIgnoreProperties(value = { "rel", "media", "affordances", "template" }, ignoreUnknown = true)
public abstract class LinkMixin extends Link {

	private static final long serialVersionUID = 4720588561299667409L;

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.Link#getHreflang()
	 */
	@Override
	@JsonInclude(Include.NON_NULL)
	public abstract String getHreflang();

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.Link#getTitle()
	 */
	@Override
	@JsonInclude(Include.NON_NULL)
	public abstract String getTitle();

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.Link#getType()
	 */
	@Override
	@JsonInclude(Include.NON_NULL)
	public abstract String getType();

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.Link#getDeprecation()
	 */
	@Override
	@JsonInclude(Include.NON_NULL)
	public abstract String getDeprecation();

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.Link#getProfile()
	 */
	@Override
	@JsonInclude(Include.NON_NULL)
	public abstract String getProfile();

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.Link#getName()
	 */
	@Override
	@JsonInclude(Include.NON_NULL)
	public abstract String getName();

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.Link#isTemplate()
	 */
	@Override
	@JsonInclude(Include.NON_EMPTY)
	@JsonSerialize(using = TrueOnlyBooleanSerializer.class)
	public abstract boolean isTemplated();
}
