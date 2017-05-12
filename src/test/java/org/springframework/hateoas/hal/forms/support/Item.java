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
import java.util.Collections;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.springframework.hateoas.affordance.SimpleSuggest;
import org.springframework.hateoas.affordance.Suggest;
import org.springframework.hateoas.affordance.SuggestObjectWrapper;
import org.springframework.hateoas.affordance.SuggestType;
import org.springframework.hateoas.affordance.formaction.DTOParam;
import org.springframework.hateoas.affordance.formaction.Input;
import org.springframework.hateoas.affordance.formaction.Options;
import org.springframework.hateoas.affordance.formaction.Select;
import org.springframework.hateoas.affordance.formaction.Type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@EqualsAndHashCode(of = {"id", "name"})
public class Item implements Serializable {

	private int id;
	private String name;
	private ItemType type;
	private List<SubItem> multiple;
	private SubItem singleSub;
	private int searchedSubItem;
	private int subItemId;
	private double amount;
	private AnotherSubItem another;
	private SubEntity subEntity;
	private List<ListableSubEntity> listSubEntity;
	private boolean flag;
	private List<Integer> integerList;
	private List<String> undefinedList;
	private List<ListableSubEntity> wildCardEntityList;
	private List<WildCardedListableSubEntity> doubleLevelWildCardEntityList;
	private String[] stringArray;
	private ListableSubEntity[] arraySubEntity;
	private ListableSubEntity[] wildcardArraySubEntity;

	public Item(final int id, final String name) {

		this(id, name, ItemType.ONE, Collections.<SubItem> emptyList(), SubItem.VALIDS[0], SubItem.VALIDS[0].getId(),
				SubItem.VALIDS[0].getId(), AnotherSubItem.VALIDS[0], null, null, 1.0, false, Collections.<Integer> emptyList(),
				Collections.<String> emptyList(), Collections.<ListableSubEntity> emptyList(),
				Collections.<WildCardedListableSubEntity> emptyList(), new String[1], new ListableSubEntity[1],
				new ListableSubEntity[1]);
	}

	@JsonCreator
	public Item(@JsonProperty("id") @Input(value = Type.NUMBER) final int id,
			final @JsonProperty("name") @Input(value = Type.TEXT) String name,
			@JsonProperty("type") @Select final ItemType type,
			@JsonProperty("multiple") @Select(options = SubItem.SubItemOptions.class,
					type = SuggestType.EXTERNAL) final List<SubItem> multiple,
			@JsonProperty("singleSub") @Select(options = SubItem.SubItemOptions.class,
					type = SuggestType.EXTERNAL) final SubItem singleSub,
			@JsonProperty("subItemId") @Select(options = SubItem.SubItemOptionsId.class,
					type = SuggestType.EXTERNAL) final int subItemId,
			@JsonProperty("searchedSubItem") @Select(options = SubItem.SubItemSearchableOptions.class,
					type = SuggestType.REMOTE) final int searchedSubItem,
			@JsonProperty("another") @Select(options = AnotherSubItem.SubItemSearchableOptions.class,
					type = SuggestType.REMOTE) final AnotherSubItem another,
			@JsonProperty("subEntity") final SubEntity subEntity,
			@JsonProperty("listSubEntity") @DTOParam(wildcard = false) final List<ListableSubEntity> listSubEntity,
			@JsonProperty("amount") @Input(value = Type.NUMBER) final double amount,
			@JsonProperty("flag") @Select(value = { "true", "false" }) final boolean flag,
			@JsonProperty("integerList") @Select(options = IntegerListOptions.class) final List<Integer> integerList,
			@JsonProperty("undefinedList") @Input(maxLength = 10) final List<String> undefinedList,
			@JsonProperty("wildCardEntityList") @DTOParam(wildcard = true) final List<ListableSubEntity> wildCardEntityList,
			@JsonProperty("doubleLevelWildCardEntityList") @DTOParam(
					wildcard = true) final List<WildCardedListableSubEntity> doubleLevelWildCardEntityList,
			@JsonProperty("stringArray") @Input final String[] stringArray,
			@JsonProperty("arraySubEntity") @DTOParam final ListableSubEntity[] arraySubEntity,
			@JsonProperty("wildcardArraySubEntity") @DTOParam(
					wildcard = true) final ListableSubEntity[] wildcardArraySubEntity) {

		this.id = id;
		this.name = name;
		this.type = type;
		this.multiple = multiple;
		this.singleSub = singleSub;
		this.subItemId = subItemId;
		this.searchedSubItem = searchedSubItem;
		this.amount = amount;
		this.another = another;
		this.subEntity = subEntity;
		this.listSubEntity = listSubEntity;
		this.flag = flag;
		this.integerList = integerList;
		this.wildCardEntityList = wildCardEntityList;
		this.doubleLevelWildCardEntityList = doubleLevelWildCardEntityList;
		this.stringArray = stringArray;
		this.arraySubEntity = arraySubEntity;
		this.wildcardArraySubEntity = wildcardArraySubEntity;
	}

	public static class IntegerListOptions implements Options<SuggestObjectWrapper<Integer>> {

		@Override
		public List<Suggest<SuggestObjectWrapper<Integer>>> get(final String[] value, final Object... args) {
			return SimpleSuggest.wrap(new Integer[] { 0, 1, 2, 3, 4, 5 });
		}

	}
}
