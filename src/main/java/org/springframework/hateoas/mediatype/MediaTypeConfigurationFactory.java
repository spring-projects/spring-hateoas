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

import java.util.function.Supplier;
import java.util.stream.Stream;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.Assert;

/**
 * Factory to provide instances of media type-specific configuration processed by
 * {@link MediaTypeConfigurationCustomizer}s.
 *
 * @author Oliver Drotbohm
 * @since 2.2
 */
public class MediaTypeConfigurationFactory<T, S extends MediaTypeConfigurationCustomizer<T>> {

	private final Supplier<T> supplier;
	private final Supplier<Stream<S>> customizers;

	private T resolved;

	/**
	 * Creates a new {@link MediaTypeConfigurationFactory} for the given supplier of the original instance and all
	 * {@link MediaTypeConfigurationCustomizer}s.
	 *
	 * @param supplier must not be {@literal null}.
	 * @param customizers must not be {@literal null}.
	 */
	MediaTypeConfigurationFactory(Supplier<T> supplier, Supplier<Stream<S>> customizers) {

		Assert.notNull(supplier, "Supplier must not be null!");
		Assert.notNull(customizers, "Customizers must not be null!");

		this.supplier = supplier;
		this.customizers = customizers;
	}

	public MediaTypeConfigurationFactory(Supplier<T> supplier, ObjectProvider<S> customizers) {
		this(supplier, () -> customizers.orderedStream());
	}

	/**
	 * Returns the customized configuration instance.
	 *
	 * @return will never be {@literal null}.
	 */
	public T getConfiguration() {

		if (resolved == null) {

			var source = supplier.get();

			Assert.notNull(source, "Source instance must not be null!");

			this.resolved = this.customizers.get()
					.reduce(source, (config, customizer) -> customizer.customize(config), (__, r) -> r);
		}

		return resolved;
	}
}
