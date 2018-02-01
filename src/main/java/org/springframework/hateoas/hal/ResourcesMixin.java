/*
 * Copyright 2012-2016 the original author or authors.
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

import java.util.Collection;

import org.springframework.hateoas.Resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Custom mixin to to render collection content as {@literal _embedded}.
 *
 * @author Alexander Baetz
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
@JsonPropertyOrder({ "content", "links" })
public abstract class ResourcesMixin<T> extends Resources<T> {

	@Override
	@JsonProperty("_embedded")
	@JsonInclude(Include.NON_EMPTY)
	@JsonSerialize(using = Jackson2HalModule.HalResourcesSerializer.class)
	@JsonDeserialize(using = Jackson2HalModule.HalResourcesDeserializer.class)
	public abstract Collection<T> getContent();
}
