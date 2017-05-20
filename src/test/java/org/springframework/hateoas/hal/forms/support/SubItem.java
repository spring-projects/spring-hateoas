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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.affordance.Suggest;
import org.springframework.hateoas.affordance.SuggestImpl;
import org.springframework.hateoas.affordance.formaction.Options;
import org.springframework.hateoas.hal.forms.support.DummyController.RemoteOptions;

@Data
@EqualsAndHashCode(of = {"id", "name"})
@AllArgsConstructor
@NoArgsConstructor
public class SubItem implements Identifiable<Integer>, Serializable {

	private Integer id;

	private String name;
	
	public static final SubItem[] VALIDS = {
		new SubItem(1, "S" + 1),
		new SubItem(2, "S" + 2),
		new SubItem(3, "S" + 3),
		new SubItem(4, "S" + 4)
	};

	public static final SubItem INVALID_VALUE = new SubItem(Integer.MAX_VALUE, "S" + Integer.MAX_VALUE);

	public static final SubItem[][] VALIDS_SELECTED = new SubItem[VALIDS.length + 1][];

	static {
		for (int i = 0; i < VALIDS_SELECTED.length; i++) {
			List<SubItem> valid = new ArrayList<SubItem>();
			for (int j = 0; j < i; j++) {
				valid.add(VALIDS[j]);
			}
			VALIDS_SELECTED[i] = valid.toArray(new SubItem[valid.size()]);
		}
	}

	public static class SubItemOptions implements Options<SubItem> {

		@Override
		public List<Suggest<SubItem>> get(final String[] value, final Object... args) {
			return SuggestImpl.wrap(Arrays.asList(VALIDS), null, "name");
		}
	}

	public static class SubItemOptionsId implements Options<SubItem> {

		@Override
		public List<Suggest<SubItem>> get(final String[] value, final Object... args) {
			return SuggestImpl.wrap(Arrays.asList(VALIDS), "id", "name");
		}
	}

	public static class SubItemSearchableOptions extends RemoteOptions {

		public SubItemSearchableOptions() {
			super(methodOn(DummyController.class).search(null), "id", "name");
		}
	}

}
