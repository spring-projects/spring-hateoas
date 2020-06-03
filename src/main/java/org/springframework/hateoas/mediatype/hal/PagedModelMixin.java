/*
 * Copyright 2012-2020 the original author or authors.
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

import java.util.Collection;

import org.springframework.hateoas.PagedModel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


/**
 * Custom mixin to render collection content under {@literal _embedded} element.
 *
 * @author Patrick ALLAIN
 */
@JsonPropertyOrder({ "content", "links" })
abstract class PagedModelMixin<T> extends PagedModel<T> {

	@Override
	@JsonProperty("_embedded")
	@JsonInclude(Include.NON_EMPTY)
	@JsonSerialize(using = Jackson2HalModule.HalResourcesSerializer.class)
	@JsonDeserialize(using = Jackson2HalModule.HalResourcesDeserializer.class)
	public abstract Collection<T> getContent();
}
