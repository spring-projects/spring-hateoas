/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas.aot;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;

/**
 * Registers reflection metadata for {@link RepresentationModel} types.
 *
 * @author Oliver Drotbohm
 */
class RepresentationModelRuntimeHints implements RuntimeHintsRegistrar {

	private static final List<Class<?>> REPRESENTATION_MODELS = List.of(RepresentationModel.class, //
			// EntityModel.class, // treated specially below
			CollectionModel.class, //
			PagedModel.class,
			PagedModel.PageMetadata.class);

	/*
	 * (non-Javadoc)
	 * @see org.springframework.aot.hint.RuntimeHintsRegistrar#registerHints(org.springframework.aot.hint.RuntimeHints, java.lang.ClassLoader)
	 */
	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {

		var reflection = hints.reflection();
		var entityModelAndNested = Arrays.stream(EntityModel.class.getNestMembers());

		Stream.concat(REPRESENTATION_MODELS.stream(), entityModelAndNested).forEach(it -> { //
			reflection.registerType(it, //
					MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_DECLARED_METHODS);
		});
	}
}
