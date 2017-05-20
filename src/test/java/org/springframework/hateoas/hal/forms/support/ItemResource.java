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

import static org.springframework.hateoas.affordance.springmvc.AffordanceBuilder.linkTo;
import static org.springframework.hateoas.affordance.springmvc.AffordanceBuilder.methodOn;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.util.SerializationUtils;

import org.springframework.hateoas.affordance.Affordance;
import org.springframework.hateoas.affordance.springmvc.AffordanceBuilder;

public class ItemResource extends Resource<Item> {

	public static final ItemResource DUMMY = new ItemResource(new Item(-1, ""));

	public ItemResource(final Item item) {

		super(clone(item));

		add(linkTo(methodOn(DummyController.class).get(item.getId())).withSelfRel());
		AffordanceBuilder editTransferBuilder = linkTo(methodOn(DummyController.class).edit(item.getId(), item));
		add(editTransferBuilder.withRel(HttpMethod.PUT.toString()));
		AffordanceBuilder deleteTransferBuilder = linkTo(methodOn(DummyController.class).delete(item.getId()));
		add(deleteTransferBuilder.withRel(HttpMethod.DELETE.toString()));
		AffordanceBuilder createItemBuilder = linkTo(methodOn(DummyController.class).create(item));
		add(createItemBuilder.withRel(HttpMethod.POST.toString()));
	}

	private static Item clone(final Item item) {
		return (Item) SerializationUtils.deserialize(SerializationUtils.serialize(item));
	}

	public static ItemResource findById(final int id, final Resources<ItemResource> processedResource) {

		for (ItemResource itemResource : processedResource) {
			if (itemResource.getContent().getId() == id) {
				return itemResource;
			}
		}
		return null;
	}

	public Affordance getMethod(final HttpMethod method) {
		
		if (method == HttpMethod.POST) {
			getContent().setId(-1);
		}
		return (Affordance) getLink(method.toString());
	}

}
