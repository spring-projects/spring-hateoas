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

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;

import org.springframework.hateoas.Resources;
import org.springframework.hateoas.hal.forms.HalFormsDeserializers.HalFormsResourcesDeserializer;
import org.springframework.hateoas.hal.forms.HalFormsSerializers.HalFormsResourcesSerializer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Greg Turnquist
 */
@JsonSerialize(using = HalFormsResourcesSerializer.class)
abstract class ResourcesMixin<T> extends Resources<T> {

	@Override
	@XmlElement(name = "embedded")
	@JsonProperty("_embedded")
	@JsonInclude(Include.NON_EMPTY)
	@JsonDeserialize(using = HalFormsResourcesDeserializer.class)
	public abstract Collection<T> getContent();

}
