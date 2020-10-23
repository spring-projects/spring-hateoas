/*
 * Copyright 2019-2020 the original author or authors.
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

import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mediatype.Affordances.AffordanceBuilder;

/**
 * Operations commons to all builder APIs.
 *
 * @author Oliver Drotbohm
 * @see AffordanceBuilder
 */
public interface AffordanceOperations {

	/**
	 * Returns a {@link Link} equipped with the {@link Affordance} currently under construction.
	 *
	 * @return will never be {@literal null}.
	 */
	Link toLink();
}
