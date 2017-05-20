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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.hateoas.hal.forms.support.DummyController.RemoteOptions;

import org.springframework.hateoas.affordance.springmvc.AffordanceBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnotherSubItem implements Serializable {

	public static final AnotherSubItem[] VALIDS = {
		new AnotherSubItem("1", "S" + 1),
		new AnotherSubItem("2", "S" + 2),
		new AnotherSubItem("3", "S" + 3),
		new AnotherSubItem("4", "S" + 4)
	};

	public static final AnotherSubItem INVALID_VALUE = new AnotherSubItem(Integer.MAX_VALUE + "", "S" + Integer.MAX_VALUE);

	private String name;

	private String owner;

	public static class SubItemSearchableOptions extends RemoteOptions {
		
		public SubItemSearchableOptions() {
			super(AffordanceBuilder.methodOn(DummyController.class).searchAnother(null), null, "name");
		}
	}
}
