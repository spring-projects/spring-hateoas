/*
 * Copyright 2013-2014 the original author or authors.
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

import org.springframework.plugin.core.Plugin;

/**
 * API to provide relation types for collections and items of the given type.
 * 
 * @author Oliver Gierke
 */
public interface RelProvider extends Plugin<Class<?>> {

	/**
	 * Returns the relation type to be used to point to an item resource of the given type.
	 * 
	 * @param type must not be {@literal null}.
	 * @return
	 */
	String getItemResourceRelFor(Class<?> type);

	/**
	 * Returns the relation type to be used to point to a collection resource of the given type.
	 * 
	 * @param type must not be {@literal null}.
	 * @return
	 */
	String getCollectionResourceRelFor(Class<?> type);
}
