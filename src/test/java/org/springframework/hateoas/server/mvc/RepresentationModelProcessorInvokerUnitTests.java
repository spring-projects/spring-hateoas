/*
 * Copyright 2020 the original author or authors.
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

import static org.assertj.core.api.Assertions.*;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;

/**
 * Unit tests for {@link RepresentationModelProcessorInvoker}.
 *
 * @author Oliver Drotbohm
 */
public class RepresentationModelProcessorInvokerUnitTests {

	@Test // #1280
	void doesNotInvokeGenericProcessorForCollectionModel() {

		RepresentationModelProcessorInvoker invoker = new RepresentationModelProcessorInvoker(
				Collections.singletonList(new GenericPostProcessor<>()));

		assertThatCode(() -> invoker.invokeProcessorsFor(CollectionModel.empty())) //
				.doesNotThrowAnyException();
	}

	// #1280

	static class GenericPostProcessor<T extends GenericModel<T>> implements RepresentationModelProcessor<T> {

		@Override
		public T process(T model) {
			return model;
		}
	}

	static class GenericModel<T extends RepresentationModel<T>> extends RepresentationModel<T> {}
}
