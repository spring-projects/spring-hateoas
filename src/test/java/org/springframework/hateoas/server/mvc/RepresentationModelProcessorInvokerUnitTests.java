/*
 * Copyright 2020-2021 the original author or authors.
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
package org.springframework.hateoas.server.mvc;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;

/**
 * Unit tests for {@link RepresentationModelProcessorInvoker}.
 *
 * @author Oliver Drotbohm
 * @author Karina Pleskach
 */
public class RepresentationModelProcessorInvokerUnitTests {

	@Test // #1280
	void doesNotInvokeGenericProcessorForCollectionModel() {

		RepresentationModelProcessorInvoker invoker = new RepresentationModelProcessorInvoker(
				singletonList(new GenericPostProcessor<>()));

		assertThatCode(() -> invoker.invokeProcessorsFor(CollectionModel.empty())) //
				.doesNotThrowAnyException();
	}

	@Test // #1379
	void doesNotInvokeProcessorForNonAssignableNestedEntity() {

		FirstEntityProcessor processor = new FirstEntityProcessor();
		RepresentationModelProcessorInvoker invoker = new RepresentationModelProcessorInvoker(
				singletonList(processor));

		EntityModel<SecondEntity> entityModel = EntityModel.of(new SecondEntity());
		invoker.invokeProcessorsFor(CollectionModel.of(singleton(entityModel)));

		assertThat(processor.invoked).isFalse();
	}

	@Test // #1379
	void doesNotInvokeProcessorForNonAssignableNestedEntityOnSpecializedCollectionModel() {

		FirstEntityProcessor firstProcessor = new FirstEntityProcessor();

		RepresentationModelProcessorInvoker invoker = new RepresentationModelProcessorInvoker(
				singletonList(firstProcessor));

		EntityModel<SecondEntity> entityModel = EntityModel.of(new SecondEntity());
		invoker.invokeProcessorsFor(new MyCollectionModelInheritor<>(singletonList(entityModel)));

		assertThat(firstProcessor.invoked).isFalse();
	}

	@Test // #1425
	void doesInvokeProcessorForCollectionModelOfRepresentationModel() {

		CollectionModelOfGenericModelProcessor processor = new CollectionModelOfGenericModelProcessor();
		RepresentationModelProcessorInvoker invoker = new RepresentationModelProcessorInvoker(
				singletonList(processor));

		GenericModel<?> model = new GenericModel<>();

		invoker.invokeProcessorsFor(CollectionModel.of(singletonList(model)));

		assertThat(processor.invoked).isTrue();
	}

	// #1280

	static class GenericPostProcessor<T extends GenericModel<T>> implements RepresentationModelProcessor<T> {

		@Override
		public T process(T model) {
			return model;
		}
	}

	static class GenericModel<T extends RepresentationModel<T>> extends RepresentationModel<T> {}

	static class FirstEntity {}

	static class SecondEntity {};

	static class FirstEntityProcessor implements RepresentationModelProcessor<CollectionModel<EntityModel<FirstEntity>>> {

		boolean invoked = false;

		@Override
		public CollectionModel<EntityModel<FirstEntity>> process(CollectionModel<EntityModel<FirstEntity>> model) {

			invoked = true;

			return model;
		}
	}

	// 1425
	static class CollectionModelOfGenericModelProcessor
			implements RepresentationModelProcessor<CollectionModel<GenericModel<?>>> {

		boolean invoked = false;

		@Override
		public CollectionModel<GenericModel<?>> process(CollectionModel<GenericModel<?>> model) {

			invoked = true;

			return model;
		}
	}

	static class MyCollectionModelInheritor<T> extends CollectionModel<T> {
		public MyCollectionModelInheritor(Iterable<T> content) {
			super(content);
		}
	}
}
