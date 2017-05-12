/*
 * Copyright 2013-2017 the original author or authors.
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

package org.springframework.hateoas.affordance.formaction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows to explicitly qualify a method handler as resource with defined cardinality.
 * Normally a Collection or a Resources return type (optionally wrapped into an HttpEntity)
 * or the presence of a POST method implicitly qualifies a resource a collection.
 *
 * @author Dietrich Schulten
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ResourceHandler {

	/**
	 * Allows to disambiguate if the annotated method handler manages a single or a collection resource.
	 * This can be helpful when there is a return type which doesn't allow to decide the cardinality
	 * of a resource, or when the default recognition comes to the wrong result.
	 * E.g. one can annotate a POST handler so that renderers can render the related resource as a single resource.
	 * <pre>
	 * &#64;ResourceHandler(Cardinality.SINGLE)
	 * &#64;RequestMapping(method=RequestMethod.POST)
	 * public ResponseEntity&lt;String&gt; myPostHandler() {}
	 * </pre>
	 *
	 * @return cardinality
	 */
	Cardinality value();
}
