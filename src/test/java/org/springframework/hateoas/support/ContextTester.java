/*
 * Copyright 2019 the original author or authors.
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
package org.springframework.hateoas.support;

import java.util.function.Function;

import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

/**
 * @author Greg Turnquist
 */
public class ContextTester {

	public static <E extends Exception> void withContext(Class<?> configuration,
			ConsumerWithException<AnnotationConfigWebApplicationContext, E> consumer) throws E {

		try (AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext()) {

			context.register(configuration);
			context.refresh();

			consumer.accept(context);
		}
	}

	public static <E extends Exception> void withServletContext(Class<?> configuration,
			ConsumerWithException<AnnotationConfigWebApplicationContext, E> consumer) throws E {

		withServletContext(configuration, Function.identity(), consumer);
	}

	public static <E extends Exception> void withServletContext(Class<?> configuration,
			Function<AnnotationConfigWebApplicationContext, AnnotationConfigWebApplicationContext> preparer,
			ConsumerWithException<AnnotationConfigWebApplicationContext, E> consumer) throws E {

		AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
		context.register(configuration);
		context.setServletContext(new MockServletContext());

		try (AnnotationConfigWebApplicationContext prepared = preparer.apply(context)) {

			prepared.refresh();
			consumer.accept(prepared);
		}
	}

	public interface ConsumerWithException<T, E extends Exception> {

		void accept(T element) throws E;
	}
}
