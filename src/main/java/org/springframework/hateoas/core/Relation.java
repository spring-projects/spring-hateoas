/*
 * Copyright 2013 the original author or authors.
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
package org.springframework.hateoas.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;

/**
 * Annotation to configure the relation to be used when embedding objects in HAL representations of {@link Resource}s
 * and {@link Resources}.
 * 
 * @author Alexander Baetz
 * @author Oliver Gierke
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Relation {

	static final String NO_RELATION = "";

	/**
	 * Defines the relation to be used when referring to a single resource.
	 * 
	 * @return
	 */
	String value() default NO_RELATION;

	/**
	 * Defines the relation to be used when referring to a collection of resources.
	 * 
	 * @return
	 */
	String collectionRelation() default NO_RELATION;
}
