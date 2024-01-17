/*
 * Copyright 2023-2024 the original author or authors.
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
package org.springframework.hateoas.mediatype;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link MediaTypeConfigurationFactory};
 *
 * @author Oliver Drotbohm
 */
@ExtendWith(MockitoExtension.class)
class MediaTypeConfigurationFactoryUnitTests {

	@Mock MediaTypeConfigurationCustomizer<Object> first, second;

	@Test // GH-2035
	void invokesCustomizers() {

		var source = new Object();
		var afterFirst = new Object();
		var afterSecond = new Object();

		doReturn(afterFirst).when(first).customize(source);
		doReturn(afterSecond).when(second).customize(afterFirst);

		var factory = new MediaTypeConfigurationFactory<>(() -> source, () -> Stream.of(first, second));

		assertThat(factory.getConfiguration()).isSameAs(afterSecond);

		verify(first, times(1)).customize(source);
		verify(second, times(1)).customize(afterFirst);

		assertThat(factory.getConfiguration()).isSameAs(afterSecond);

		// Does not re-process source instance
		verify(first, times(1)).customize(any());
		verify(second, times(1)).customize(any());
	}
}
