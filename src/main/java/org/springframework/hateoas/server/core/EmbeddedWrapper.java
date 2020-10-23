/*
 * Copyright 2014-2020 the original author or authors.
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
package org.springframework.hateoas.server.core;

import java.util.Optional;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.LinkRelation;
import org.springframework.lang.Nullable;

/**
 * A wrapper to handle values to be embedded into a {@link EntityModel}.
 *
 * @author Oliver Gierke
 */
public interface EmbeddedWrapper {

	/**
	 * Returns the rel to be used when embedding. If this returns {@literal null}, the rel will be calculated based on the
	 * type returned by {@link #getRelTargetType()}. A wrapper returning {@literal null} for both {@link #getRel()} and
	 * {@link #getRelTargetType()} is considered invalid.
	 *
	 * @return
	 * @see #getRelTargetType()
	 */
	Optional<LinkRelation> getRel();

	/**
	 * Returns whether the wrapper has the given rel.
	 *
	 * @param rel can be {@literal null}.
	 * @return
	 */
	boolean hasRel(LinkRelation rel);

	/**
	 * Returns whether the wrapper is a collection value.
	 *
	 * @return
	 */
	boolean isCollectionValue();

	/**
	 * Returns the actual value to embed.
	 *
	 * @return
	 */
	Object getValue();

	/**
	 * Returns the type to be used to calculate a type based rel. Can return {@literal null} in case an explicit rel is
	 * returned in {@link #getRel()}. A wrapper returning {@literal null} for both {@link #getRel()} and
	 * {@link #getRelTargetType()} is considered invalid.
	 *
	 * @return
	 */
	@Nullable
	Class<?> getRelTargetType();
}
