/*
 * Copyright 2012-2019 the original author or authors.
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
package org.springframework.hateoas.server;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;

/**
 * SPI interface to allow components to process the {@link RepresentationModel} instances returned from Spring MVC
 * controllers.
 *
 * @see EntityModel
 * @see CollectionModel
 * @author Oliver Gierke
 */
public interface RepresentationModelProcessor<T extends RepresentationModel<?>> {

	/**
	 * Processes the given representation model, add links, alter the domain data etc.
	 *
	 * @param model
	 * @return the processed model
	 */
	T process(T model);
}
