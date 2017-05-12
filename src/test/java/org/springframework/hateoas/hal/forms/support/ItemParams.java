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

import static org.springframework.hateoas.affordance.support.Path.on;
import static org.springframework.hateoas.affordance.support.Path.path;

public enum ItemParams {
	ID(path(on(Item.class).getId())), NAME(path(on(Item.class).getName())), AMOUNT(path(on(Item.class).getAmount())), SINGLESUB(
			path(on(Item.class).getSingleSub())), SUBITEMID(path(on(Item.class).getSubItemId())), TYPE(
					path(on(Item.class).getType())), LISTSUBENTITY(path(on(Item.class).getListSubEntity())), MULTIPLE(
							path(on(Item.class).getMultiple())), UNDEFINEDLIST(path(on(Item.class).getUndefinedList()));

	private String path;

	ItemParams(final String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}
}