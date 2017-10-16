/*
 * Copyright 2012 the original author or authors.
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
package org.springframework.hateoas;

import java.io.Serializable;
import java.util.Optional;

/**
 * Interface to mark objects that are identifiable by an ID of any type.
 * 
 * @author Oliver Gierke
 */
public interface Identifiable<ID extends Serializable> {

	/**
	 * Returns the id identifying the object.
	 * 
	 * @return the identifier or {@link Optional#empty()} if not available.
	 */
	Optional<ID> getId();
}
