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
package org.springframework.hateoas.hal.forms.support;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

import org.springframework.hateoas.affordance.SuggestType;
import org.springframework.hateoas.affordance.formaction.Input;
import org.springframework.hateoas.affordance.formaction.Select;
import org.springframework.hateoas.affordance.formaction.Type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class SubEntity implements Serializable {

	private int key;
	private String name;
	private List<SubItem> multiple;
	private ItemType type;
	private SubSubEntity subEntity;

	@JsonCreator
	public SubEntity(@JsonProperty("key") @Input(value = Type.NUMBER) final int key,
			final @JsonProperty("name") @Input(value = Type.TEXT) String name,
			@JsonProperty("multiple") @Select(options = SubItem.SubItemOptions.class, type = SuggestType.EXTERNAL) final List<SubItem> multiple,
			@JsonProperty("type") @Select final ItemType type, @JsonProperty("subEntity") final SubSubEntity subEntity) {
		this.key = key;
		this.name = name;
		this.type = type;
		this.multiple = multiple;
		this.subEntity = subEntity;
	}
}
