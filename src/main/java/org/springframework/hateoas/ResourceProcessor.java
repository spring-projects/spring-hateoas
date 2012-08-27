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

/**
 * SPI interface to allow components to process the {@link ResourceSupport} instances returned from Spring MVC
 * controllers.
 * 
 * @see Resource
 * @see Resources
 * @author Oliver Gierke
 */
public interface ResourceProcessor<T extends ResourceSupport> {

	/**
	 * Processes the given resource, add links, alter the domain data etc.
	 * 
	 * @param resource
	 * @return the processed resource
	 */
	T process(T resource);
}
