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
public class ListableSubEntity implements Serializable {

	private int lkey;
	private String lname;
	private ListableItemType type;
	private List<ListableItemType> multiple;

	@JsonCreator
	public ListableSubEntity(@JsonProperty("lkey") @Input(value = Type.NUMBER) final int lkey,
			final @JsonProperty("lname") @Input(value = Type.TEXT) String lname, @JsonProperty("type") @Select final ListableItemType ltype,
			@JsonProperty("multiple") @Select(type = SuggestType.EXTERNAL) final List<ListableItemType> multiple) {

		this.lkey = lkey;
		this.lname = lname;
		this.type = ltype;
		this.multiple = multiple;
	}

}
