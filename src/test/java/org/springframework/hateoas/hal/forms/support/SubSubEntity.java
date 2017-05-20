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

import static org.springframework.hateoas.affordance.SuggestType.*;
import static org.springframework.hateoas.affordance.formaction.Type.*;
import static org.springframework.hateoas.hal.forms.support.SubItem.*;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

import org.springframework.hateoas.affordance.formaction.Input;
import org.springframework.hateoas.affordance.formaction.Select;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class SubSubEntity implements Serializable {

	private int key;
	private List<SubItem> multiple;
	private String name;
	private ItemType type;

	@JsonCreator
	public SubSubEntity(
		@JsonProperty("key") @Input(value = NUMBER) int key,
		@JsonProperty("name") @Input(value = TEXT) String name,
		@JsonProperty("multiple") @Select(options = SubItemOptions.class, type = EXTERNAL) List<SubItem> multiple,
		@JsonProperty("type") @Select ItemType type) {

		this.key = key;
		this.name = name;
		this.type = type;
		this.multiple = multiple;
	}
}
