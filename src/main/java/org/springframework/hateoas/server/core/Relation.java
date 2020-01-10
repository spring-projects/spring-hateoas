/*
 * Copyright 2013-2020 the original author or authors.
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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;

/**
 * Annotation to configure the relation to be used when embedding objects in HAL representations of {@link EntityModel}s
 * and {@link CollectionModel}.
 *
 * @author Alexander Baetz
 * @author Oliver Gierke
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Relation {

	String NO_RELATION = "";

	/**
	 * Defines the relation to be used when referring to a single resource. Alias for {@link #itemRelation()}.
	 *
	 * @return
	 */
	@AliasFor("itemRelation")
	String value() default NO_RELATION;

	/**
	 * Defines the relation to be used when referring to a single resource. Alias of {@link #value()}.
	 *
	 * @return
	 */
	@AliasFor("value")
	String itemRelation() default NO_RELATION;

	/**
	 * Defines the relation to be used when referring to a collection of resources.
	 *
	 * @return
	 */
	String collectionRelation() default NO_RELATION;
}
