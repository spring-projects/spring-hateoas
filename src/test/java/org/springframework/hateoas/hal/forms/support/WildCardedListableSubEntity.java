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

import java.io.Serializable;
import java.util.List;

import lombok.Data;

import org.springframework.hateoas.affordance.formaction.DTOParam;
import org.springframework.hateoas.affordance.formaction.Input;
import org.springframework.hateoas.affordance.formaction.Select;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class WildCardedListableSubEntity implements Serializable {

	private int lkey;
	private String lname;
	private ListableItemType type;
	private List<ListableItemType> multiple;
	private List<SubItem> subItemList;

	@JsonCreator
	public WildCardedListableSubEntity(
		@JsonProperty("lkey") @Input(value = NUMBER) final int lkey,
		@JsonProperty("lname") @Input(value = TEXT) String lname,
		@JsonProperty("type") @Select ListableItemType ltype,
		@JsonProperty("multiple") @Select(type = EXTERNAL) List<ListableItemType> multiple,
		@JsonProperty("subItemList") @DTOParam(wildcard = true) List<SubItem> subItemList) {

		this.lkey = lkey;
		this.lname = lname;
		this.type = ltype;
		this.multiple = multiple;
		this.subItemList = subItemList;
	}
}
