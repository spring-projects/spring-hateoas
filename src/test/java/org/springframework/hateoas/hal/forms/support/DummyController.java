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

import static org.springframework.hateoas.affordance.springmvc.AffordanceBuilder.*;
import static org.springframework.hateoas.affordance.support.Path.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.affordance.ActionInputParameter;
import org.springframework.hateoas.affordance.Suggest;
import org.springframework.hateoas.affordance.SuggestImpl;
import org.springframework.hateoas.affordance.formaction.DTOParam;
import org.springframework.hateoas.affordance.formaction.Input;
import org.springframework.hateoas.affordance.formaction.Options;
import org.springframework.hateoas.affordance.formaction.Type;
import org.springframework.hateoas.affordance.springmvc.AffordanceBuilder;
import org.springframework.hateoas.core.DummyInvocationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(value = "/test")
public class DummyController {

	static final int INITIAL = 10;

	public static final List<Integer> NAME_READONLY = Arrays.asList(0, 2);

	public static final List<Integer> NAME_REQUIRED = Arrays.asList(0, 1);

	public static final List<Integer> AMOUNT_READONLY = Arrays.asList(9);

	public static final List<Integer> AMOUNT_REQUIRED = Arrays.asList(1);

	public static final List<Integer> TYPE_READONLY = Arrays.asList(0);

	public static final List<Integer> TYPE_REQUIRED = Arrays.asList(6);

	public static final List<Integer> SUBITEM_ID_READONLY = Arrays.asList(5);

	public static final List<Integer> SUBITEM_ID_REQUIRED = Arrays.asList(4);

	public static final List<Integer> SUBITEM_READONLY = Arrays.asList(3);

	public static final List<Integer> SUBITEM_REQUIRED = Arrays.asList(2);

	public static final List<Integer> SEARCHED_SUBITEM_READONLY = Arrays.asList(6);

	public static final List<Integer> SEARCHED_SUBITEM_REQUIRED = Arrays.asList(3);

	public static final List<Integer> ANOTHER_SUBITEM_READONLY = Arrays.asList(8);

	public static final List<Integer> ANOTHER_SUBITEM_REQUIRED = Arrays.asList(9);

	public static final List<Integer> FLAG_READONLY = Arrays.asList(8);

	public static final List<Integer> FLAG_REQUIRED = Arrays.asList(9);

	public static final List<Integer> INTEGER_LIST_READONLY = Arrays.asList(8);

	public static final List<Integer> INTEGER_LIST_REQUIRED = Arrays.asList(9);

	public static final List<Integer> SUBENTITY_NAME_REQUIRED = Arrays.asList(4);

	public static final List<Integer> SUBENTITY_NAME_READONLY = Arrays.asList(6);

	public static final List<Integer> SUBENTITY_MULTIPLE_REQUIRED = Arrays.asList(5);

	public static final List<Integer> SUBENTITY_MULTIPLE_READONLY = Arrays.asList(7);

	public static final List<Integer> LIST_SUBENTITY_KEY_REQUIRED = Arrays.asList(3);

	public static final List<Integer> LIST_SUBENTITY_KEY_READONLY = Arrays.asList(1);

	public static final List<Integer> LIST_WC_SUBENTITY_KEY_REQUIRED = Arrays.asList(3, 4);

	public static final List<Integer> LIST_WC_SUBENTITY_KEY_READONLY = Arrays.asList(1, 4, 5);

	public static final List<Integer> LIST_WC_SUBENTITY_NAME_REQUIRED = Arrays.asList(1);

	public static final List<Integer> LIST_WC_SUBENTITY_NAME_READONLY = Arrays.asList(1);

	public static final List<Integer> LIST_WC_SUBENTITYLIST_ID_REQUIRED = Arrays.asList(1);

	public static final List<Integer> LIST_WC_SUBENTITYLIST_ID_READONLY = Arrays.asList(1, 3);

	public static final List<Integer> ARRAY_REQUIRED = Arrays.asList(1, 9);

	public static final List<Integer> ARRAY_READONLY = Arrays.asList(3);

	public List<Item> items = new ArrayList<Item>();

	private final List<ItemResource> resources = new ArrayList<ItemResource>();

	public static final String MODIFY = "modify";

	public static final String DELETE = "delete";

	public DummyController() {
		for (int i = 0; i < INITIAL; i++) {
			SubSubEntity subEntity = new SubSubEntity(i + 1, "SE" + i + 1,
					Arrays.asList(SubItem.VALIDS_SELECTED[(i + 2) % SubItem.VALIDS_SELECTED.length]),
					ItemType.values()[(i + 2) % ItemType.values().length]);
			SubEntity entity = new SubEntity(i, "E" + i,
					Arrays.asList(SubItem.VALIDS_SELECTED[(i + 1) % SubItem.VALIDS_SELECTED.length]),
					ItemType.values()[(i + 1) % ItemType.values().length], subEntity);
			List<ListableSubEntity> listSubEntity = new ArrayList<ListableSubEntity>();
			List<ListableSubEntity> listWCEntity = new ArrayList<ListableSubEntity>();
			List<WildCardedListableSubEntity> listDoubleLevelWCEntity = new ArrayList<WildCardedListableSubEntity>();
			ArrayList<SubItem> listSubEntitySubItem;
			for (int j = 0; j < 2; j++) {

				listSubEntity.add(
						new ListableSubEntity(j, "LSE" + j, ListableItemType.values()[(i + 2) % ListableItemType.values().length],
								Arrays.asList(ListableItemType.values()[(i) % ListableItemType.values().length],
										ListableItemType.values()[(i + 1) % ListableItemType.values().length])));

				listWCEntity
						.add(new ListableSubEntity(2, "LSE", ListableItemType.values()[(i + 2) % ListableItemType.values().length],
								Arrays.asList(ListableItemType.values()[(i) % ListableItemType.values().length],
										ListableItemType.values()[(i + 1) % ListableItemType.values().length])));

				listSubEntitySubItem = new ArrayList<SubItem>();

				for (int x = 0; x < i + 2; x++) {
					listSubEntitySubItem.add(new SubItem(i * 10, String.valueOf(i * 10).concat("_name")));
				}
				listDoubleLevelWCEntity.add(new WildCardedListableSubEntity(2, "LSE",
						ListableItemType.values()[(i + 2) % ListableItemType.values().length],
						Arrays.asList(ListableItemType.values()[(i) % ListableItemType.values().length],
								ListableItemType.values()[(i + 1) % ListableItemType.values().length]),
						listSubEntitySubItem));

			}

			items.add(new Item(i, "Name" + Integer.toString(i), ItemType.values()[i % ItemType.values().length],
					Arrays.asList(SubItem.VALIDS_SELECTED[i % SubItem.VALIDS_SELECTED.length]),
					SubItem.VALIDS[i % SubItem.VALIDS.length], SubItem.VALIDS[i % SubItem.VALIDS.length].getId(),
					SubItem.VALIDS[(i + 1) % SubItem.VALIDS.length].getId(),
					AnotherSubItem.VALIDS[(i) % AnotherSubItem.VALIDS.length], entity, listSubEntity, 1.0, i % 2 == 0,
					getIntegerList(i), new ArrayList<String>(), listWCEntity, listDoubleLevelWCEntity,
					new String[] { "def_element", "second_one" },
					listSubEntity.toArray(new ListableSubEntity[listSubEntity.size()]),
					listSubEntity.toArray(new ListableSubEntity[listSubEntity.size()])));
		}
	}

	public void setUp() {
		for (Item transfer : items) {
			resources.add(new ItemResource(transfer));
		}
	}

	public ResourceSupport getById(final Integer id, final String rel) {
		Item item = findById(id);
		ResourceSupport resourceSupport = new ResourceSupport();
		AffordanceBuilder builder = linkTo(methodOn(DummyController.class).get(id, null));
		if (MODIFY.equals(rel)) {
			AffordanceBuilder editTransferBuilder = linkTo(methodOn(DummyController.class).edit(id, item));

			ActionInputParameter nameInputParameter = editTransferBuilder.getActionDescriptors().get(0)
					.getActionInputParameter(path(on(Item.class).getName()));
			ActionInputParameter amountInputParameter = editTransferBuilder.getActionDescriptors().get(0)
					.getActionInputParameter(path(on(Item.class).getAmount()));
			ActionInputParameter typeInputParameter = editTransferBuilder.getActionDescriptors().get(0)
					.getActionInputParameter(path(on(Item.class).getType()));
			ActionInputParameter subItemIdInputParameter = editTransferBuilder.getActionDescriptors().get(0)
					.getActionInputParameter(path(on(Item.class).getSubItemId()));
			ActionInputParameter subItemInputParameter = editTransferBuilder.getActionDescriptors().get(0)
					.getActionInputParameter(path(on(Item.class).getSingleSub()));
			ActionInputParameter searchedSubItemInputParameter = editTransferBuilder.getActionDescriptors().get(0)
					.getActionInputParameter(path(on(Item.class).getSearchedSubItem()));
			ActionInputParameter anotherSubItemInputParameter = editTransferBuilder.getActionDescriptors().get(0)
					.getActionInputParameter(path(on(Item.class).getAnother()));
			ActionInputParameter flagInputParameter = editTransferBuilder.getActionDescriptors().get(0)
					.getActionInputParameter(path(on(Item.class).isFlag()));
			ActionInputParameter integerListInputParameter = editTransferBuilder.getActionDescriptors().get(0)
					.getActionInputParameter(path(on(Item.class).getIntegerList()));
			ActionInputParameter subEntityNameInputParameter = editTransferBuilder.getActionDescriptors().get(0)
					.getActionInputParameter(path(on(Item.class).getSubEntity().getName()));
			ActionInputParameter subEntityMultipleInputParameter = editTransferBuilder.getActionDescriptors().get(0)
					.getActionInputParameter(path(on(Item.class).getSubEntity().getMultiple()));
			ActionInputParameter wildcardsubEntityKeyInputParameter = editTransferBuilder.getActionDescriptors().get(0)
					.getActionInputParameter(
							path(on(Item.class).getDoubleLevelWildCardEntityList()) + DTOParam.WILDCARD_LIST_MASK + ".lkey");
			ActionInputParameter wildcardsubEntityNameInputParameter = editTransferBuilder.getActionDescriptors().get(0)
					.getActionInputParameter(
							path(on(Item.class).getDoubleLevelWildCardEntityList()) + DTOParam.WILDCARD_LIST_MASK + ".lname");
			ActionInputParameter wildcardsubItemListSubEntityIdInputParameter = editTransferBuilder.getActionDescriptors()
					.get(0).getActionInputParameter(path(on(Item.class).getDoubleLevelWildCardEntityList())
							+ DTOParam.WILDCARD_LIST_MASK + ".subItemList" + DTOParam.WILDCARD_LIST_MASK + ".id");
			ActionInputParameter subEntityListKeyInputParameter = editTransferBuilder.getActionDescriptors().get(0)
					.getActionInputParameter(path(on(Item.class).getListSubEntity()) + "[0].lkey");

			ActionInputParameter stringArrayInputParameter = editTransferBuilder.getActionDescriptors().get(0)
					.getActionInputParameter(path(on(Item.class).getStringArray()));

			param(item, nameInputParameter, NAME_READONLY, NAME_REQUIRED);
			param(item, amountInputParameter, AMOUNT_READONLY, AMOUNT_REQUIRED);
			param(item, typeInputParameter, TYPE_READONLY, TYPE_REQUIRED);
			param(item, subItemIdInputParameter, SUBITEM_ID_READONLY, SUBITEM_ID_REQUIRED);
			param(item, subItemInputParameter, SUBITEM_READONLY, SUBITEM_REQUIRED);
			param(item, searchedSubItemInputParameter, SEARCHED_SUBITEM_READONLY, SEARCHED_SUBITEM_REQUIRED);
			param(item, anotherSubItemInputParameter, ANOTHER_SUBITEM_READONLY, ANOTHER_SUBITEM_REQUIRED);
			param(item, flagInputParameter, FLAG_READONLY, FLAG_REQUIRED);
			param(item, integerListInputParameter, INTEGER_LIST_READONLY, INTEGER_LIST_REQUIRED);
			param(item, subEntityNameInputParameter, SUBENTITY_NAME_READONLY, SUBENTITY_NAME_REQUIRED);
			param(item, subEntityMultipleInputParameter, SUBENTITY_MULTIPLE_READONLY, SUBENTITY_MULTIPLE_REQUIRED);
			param(item, subEntityListKeyInputParameter, LIST_SUBENTITY_KEY_READONLY, LIST_SUBENTITY_KEY_REQUIRED);
			param(item, wildcardsubEntityKeyInputParameter, LIST_WC_SUBENTITY_KEY_READONLY, LIST_WC_SUBENTITY_KEY_REQUIRED);
			param(item, wildcardsubEntityNameInputParameter, LIST_WC_SUBENTITY_NAME_READONLY,
					LIST_WC_SUBENTITY_NAME_REQUIRED);
			param(item, wildcardsubItemListSubEntityIdInputParameter, LIST_WC_SUBENTITYLIST_ID_READONLY,
					LIST_WC_SUBENTITYLIST_ID_REQUIRED);
			param(item, stringArrayInputParameter, ARRAY_READONLY, ARRAY_REQUIRED);
			builder.and(editTransferBuilder);
			resourceSupport.add(editTransferBuilder.withRel(rel));
		} else if (DELETE.equals(rel)) {
			AffordanceBuilder deleteTransferBuilder = linkTo(methodOn(DummyController.class).delete(id));
			builder.and(deleteTransferBuilder);
		}
		resourceSupport.add(builder.withSelfRel());

		return resourceSupport;
	}

	public static List<Integer> getIntegerList(final int value) {
		List<Integer> values = new ArrayList<Integer>();
		for (int i = 0; i < value % 5; i++) {
			values.add(i);
		}
		return values;
	}

	public static int getInitialLinks() {
		// Self + Filter (Number of items * Number of links per item)
		return 1 + 1 + (INITIAL * ItemResource.DUMMY.getLinks().size());
	}

	@RequestMapping(value = "/item/{id}", method = RequestMethod.PUT)
	public ResponseEntity<Resource<Item>> edit(@PathVariable("id") final Integer id, @RequestBody final Item item) {
		Item found = findById(id);
		items.remove(found);
		items.add(item);

		Resource<Item> resource = new ItemResource(item);
		resource.add(linkTo(methodOn(DummyController.class).get(item.getId())).withSelfRel());
		return new ResponseEntity<Resource<Item>>(resource, HttpStatus.ACCEPTED);
	}

	@RequestMapping(value = "/item/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<Object> delete(@PathVariable("id") final Integer id) {
		items.remove(findById(id));
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}

	@RequestMapping(value = "/item", method = RequestMethod.POST)
	public ResponseEntity<Resource<Item>> create(@RequestBody final Item item) {
		items.add(item);
		Resource<Item> resource = new ItemResource(item);
		resource.add(linkTo(methodOn(DummyController.class).get(item.getId())).withSelfRel());
		return new ResponseEntity<Resource<Item>>(resource, HttpStatus.CREATED);
	}

	@RequestMapping(value = "/item/{id}", method = RequestMethod.GET)
	public Resource<Item> get(@Input(required = true, pattern = "^[1-9]\\d*$") @PathVariable("id") final Integer id) {
		Item item = findById(id);

		Resource<Item> resource = new ItemResource(item);
		AffordanceBuilder editItemBuilder = linkTo(methodOn(DummyController.class).edit(id, item));
		resource.add(linkTo(methodOn(DummyController.class).get(item.getId())).and(editItemBuilder).withSelfRel());
		return resource;
	}

	@RequestMapping(value = "/item/", method = RequestMethod.GET)
	public Resources<ItemResource> get() {
		return new Resources<ItemResource>(resources, linkTo(methodOn(DummyController.class).get()).withSelfRel(),
				linkTo(methodOn(DummyController.class).getFiltered((Date) null, (Date) null, null))
						.withRel("list-after-date-transfers"));
	}

	@RequestMapping(value = "/item/{id}", method = RequestMethod.GET, params = "rel",
			produces = "application/prs.hal-forms+json")
	public ResourceSupport get(@PathVariable("id") final Integer id, @RequestParam final String rel) {
		return getById(id, rel);
	}

	private void param(final Item item, final ActionInputParameter parameter, final List<Integer> readOnly,
			final List<Integer> required) {
		if (readOnly.contains(item.getId())) {
			parameter.setReadOnly(true);
		}
		if (required.contains(item.getId())) {
			parameter.setRequired(true);
		}
	}

	@RequestMapping(value = "/item/", method = RequestMethod.GET, params = "rel",
			produces = "application/prs.hal-forms+json")
	public ResourceSupport get(@RequestParam final String rel) {
		ResourceSupport resourceSupport = new ResourceSupport();

		AffordanceBuilder getByUser = linkTo(methodOn(DummyController.class).get());
		AffordanceBuilder transferBuilder = linkTo(methodOn(DummyController.class).create(new Item(1, null)));

		ActionInputParameter inputParameter;
		// Separated iterations because they do not have to contain the same parameters
		if (paramRequiredValuesMap != null) {
			for (Entry<ItemParams, Boolean> param : paramRequiredValuesMap.entrySet()) {
				inputParameter = transferBuilder.getActionDescriptors().get(0)
						.getActionInputParameter(param.getKey().getPath());
				inputParameter.setRequired(param.getValue());
			}
		}
		if (paramReadOnlyValuesMap != null) {
			for (Entry<ItemParams, Boolean> param : paramReadOnlyValuesMap.entrySet()) {
				inputParameter = transferBuilder.getActionDescriptors().get(0)
						.getActionInputParameter(param.getKey().getPath());
				inputParameter.setReadOnly(param.getValue());
			}
		}
		Link link = linkTo(methodOn(DummyController.class).get(rel)).and(getByUser).and(transferBuilder).withSelfRel();
		resourceSupport.add(link);
		return resourceSupport;
	}

	@RequestMapping(value = "/item/filter", method = RequestMethod.GET, params = "rel",
			produces = "application/prs.hal-forms+json")
	public ResourceSupport getFiltered(@RequestParam final String rel) {
		ResourceSupport resourceSupport = new ResourceSupport();

		AffordanceBuilder getByDate = linkTo(methodOn(DummyController.class).getFiltered((Date) null, (Date) null, null));
		Link link = linkTo(methodOn(DummyController.class).getFiltered(rel)).and(getByDate).withSelfRel();
		resourceSupport.add(link);
		return resourceSupport;
	}

	@RequestMapping(value = "/item/filter", method = RequestMethod.GET)
	public Resources<ItemResource> getFiltered(
			@RequestParam(value = "dateFrom",
					required = false) @Input(value = Type.DATE) @DateTimeFormat(pattern = "yyyy-MM-dd") final Date dateFrom,
			@RequestParam(value = "dateTo",
					required = false) @Input(value = Type.DATE) @DateTimeFormat(pattern = "yyyy-MM-dd") final Date dateTo,
			@RequestParam(value = "status", required = false) final ItemType type) {

		List<ItemResource> resources = new ArrayList<ItemResource>();
		for (Item transfer : items) {
			if (transfer.getType() == type || type == null) {
				resources.add(new ItemResource(transfer));
			}
		}

		return new Resources<ItemResource>(resources, linkTo(methodOn(DummyController.class).get()).withSelfRel(),
				linkTo(methodOn(DummyController.class).getFiltered((Date) null, (Date) null, null))
						.withRel("list-after-date-transfers"));
	}

	@RequestMapping(value = "/subitem/filter", method = RequestMethod.GET, params = "filter")
	public Resources<SubItem> search(@RequestParam final String filter) {
		List<SubItem> subItems = new ArrayList<SubItem>();
		for (SubItem subItem : SubItem.VALIDS) {
			if (subItem.getName().contains(filter)) {
				subItems.add(subItem);
			}
		}
		return new Resources<SubItem>(subItems, linkTo(methodOn(DummyController.class).get()).withSelfRel());
	}

	@RequestMapping(value = "/subitem/anotherFilter/{filter}/", method = RequestMethod.GET)
	public Resources<AnotherSubItem> searchAnother(@PathVariable final String filter) {
		List<AnotherSubItem> subItems = new ArrayList<AnotherSubItem>();
		for (AnotherSubItem subItem : AnotherSubItem.VALIDS) {
			if (subItem.getOwner().contains(filter)) {
				subItems.add(subItem);
			}
		}
		return new Resources<AnotherSubItem>(subItems, linkTo(methodOn(DummyController.class).get()).withSelfRel());
	}

	private Item findById(final int id) {
		for (Item item : items) {
			if (item.getId() == id) {
				return item;
			}
		}
		return null;
	}

	public void setRequiredValues(final BooleanMap params) {
		paramRequiredValuesMap = params;
	}

	public void setReadOnlyValues(final BooleanMap params) {
		paramReadOnlyValuesMap = params;
	}

	private BooleanMap paramRequiredValuesMap = null;

	private BooleanMap paramReadOnlyValuesMap = null;

	public static class RemoteOptions implements Options<String> {

		private final Object lastInvocation;

		private final String idField;

		private final String textField;

		public RemoteOptions(final Object lastInvocation, final String idField, final String textField) {
			Assert.isInstanceOf(DummyInvocationUtils.LastInvocationAware.class, lastInvocation);
			this.lastInvocation = lastInvocation;
			this.idField = idField;
			this.textField = textField;
		}

		@Override
		public List<Suggest<String>> get(final String[] value, final Object... args) {
			Link link = AffordanceBuilder.linkTo(lastInvocation).withSelfRel();
			return SuggestImpl.wrap(Arrays.asList(link.getHref()), idField, textField);
		}

		public static List<Suggest<String>> wrap(final String url, final Suggest<String> suggest) {
			return SuggestImpl.wrap(Arrays.asList(url), suggest.getValueField(), suggest.getTextField());
		}
	}

}
