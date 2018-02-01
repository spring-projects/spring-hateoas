/*
 * Copyright 2012 the original author or authors.
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

import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Custom mixin to render {@link Link}s in HAL.
 *
 * @author Alexander Baetz
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
public abstract class ResourceSupportMixin extends ResourceSupport {

	@Override
	@JsonProperty("_links")
	@JsonInclude(Include.NON_EMPTY)
	@JsonSerialize(using = Jackson2HalModule.HalLinkListSerializer.class)
	@JsonDeserialize(using = Jackson2HalModule.HalLinkListDeserializer.class)
	public abstract List<Link> getLinks();
}
