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

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.*;

import java.util.function.Supplier;

import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.util.Assert;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

/**
 * Syntactic sugar to create {@link Link} instances from {@link MvcUriComponentsBuilder} invocations.
 *
 * @author Oliver Drotbohm
 */
public class MvcLink {

	/**
	 * Creates a new {@link Link} from the given {@link MvcUriComponentsBuilder} invocation defaulting to the
	 * {@link IanaLinkRelations#SELF} link relation.
	 *
	 * @param invocation must not be {@literal null}.
	 * @return will never be {@literal null}.
	 * @since 1.3
	 */
	public static Link of(Object invocation) {
		return of(invocation, IanaLinkRelations.SELF);
	}

	/**
	 * Creates a new {@link Link} from the given lazy {@link MvcUriComponentsBuilder} invocation defaulting to the
	 * {@link IanaLinkRelations#SELF} link relation.
	 *
	 * @param invocation must not be {@literal null}.
	 * @return will never be {@literal null}.
	 * @since 1.3
	 */
	public static Link of(Supplier<Object> invocation) {
		return of(invocation, IanaLinkRelations.SELF);
	}

	/**
	 * Creates a new {@link Link} from the given {@link MvcUriComponentsBuilder} invocation.
	 *
	 * @param invocation must not be {@literal null}.
	 * @param relation must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public static Link of(Object invocation, LinkRelation relation) {

		Assert.notNull(invocation, "MethodInvocation must not be null!");
		Assert.notNull(relation, "Link relation must not be null!");

		return Link.of(fromMethodCall(invocation).toUriString(), relation);
	}

	/**
	 * Creates a new {@link Link} from the given lazy {@link MvcUriComponentsBuilder} invocation.
	 *
	 * @param invocation must not be {@literal null}.
	 * @param relation must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public static Link of(Supplier<Object> invocation, LinkRelation relation) {

		Assert.notNull(invocation, "MethodInvocation must not be null!");
		Assert.notNull(relation, "Link relation must not be null!");

		return Link.of(fromMethodCall(invocation.get()).toUriString(), relation);
	}

	/**
	 * Syntactic sugar for {@link MvcUriComponentsBuilder#on(Class)} to avoid the additional static import.
	 *
	 * @param controller must not be {@literal null}.
	 * @return will never be {@literal null}.
	 * @since 1.3
	 */
	public static <T> T on(Class<T> controller) {

		Assert.notNull(controller, "Controller must not be null!");

		return MvcUriComponentsBuilder.on(controller);
	}
}
