/*
 * Copyright 2012-2021 the original author or authors.
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

import java.util.HashMap;
import java.util.Map;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.hateoas.server.LinkBuilder;
import org.springframework.hateoas.server.LinkBuilderFactory;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * {@link EntityLinks} implementation which assumes a certain URI mapping structure:
 * <ol>
 * <li>A class-level {@link ExposesResourceFor} annotation to declare that the annotated controller exposes collection
 * and item resources for.</li>
 * <li>An {@link RequestMapping} annotation to form the base URI of the collection resource.</li>
 * <li>A controller method with a mapping annotation to actually handle at least one HTTP method.</li>
 * <li>A controller method that maps a subordinate resource taking a path variable to identify an item resource.</li>
 * </ol>
 *
 * <pre>
 * &#64;Controller
 * &#64;ExposesResourceFor(Order.class)
 * &#64;RequestMapping("/orders")
 * class OrderController {
 *
 *   &#64;GetMapping
 *   ResponseEntity orders(…) { … }
 *
 *   &#64;GetMapping("/{id}")
 *   ResponseEntity order(@PathVariable("id") … ) { … }
 * }
 * </pre>
 *
 * @author Oliver Gierke
 */
public class ControllerEntityLinks extends AbstractEntityLinks {

	private final Map<Class<?>, Class<?>> entityToController;
	private final LinkBuilderFactory<? extends LinkBuilder> linkBuilderFactory;

	/**
	 * Creates a new {@link ControllerEntityLinks} inspecting the configured classes for the given annotation.
	 *
	 * @param controllerTypes the controller classes to be inspected, must not be {@literal null}.
	 * @param linkBuilderFactory the {@link LinkBuilder} to use to create links, must not be {@literal null}.
	 */
	public ControllerEntityLinks(Iterable<? extends Class<?>> controllerTypes,
			LinkBuilderFactory<? extends LinkBuilder> linkBuilderFactory) {

		Assert.notNull(controllerTypes, "ControllerTypes must not be null!");
		Assert.notNull(linkBuilderFactory, "LinkBuilderFactory must not be null!");

		this.linkBuilderFactory = linkBuilderFactory;
		this.entityToController = new HashMap<>();

		controllerTypes.forEach(this::registerControllerClass);
	}

	private void registerControllerClass(Class<?> controllerType) {

		Assert.notNull(controllerType, "Controller type must nor be null!");
		ExposesResourceFor annotation = AnnotationUtils.findAnnotation(controllerType, ExposesResourceFor.class);

		if (annotation != null) {
			entityToController.put(annotation.value(), controllerType);
		} else {
			throw new IllegalArgumentException(
					String.format("Controller %s must be annotated with @ExposesResourceFor!", controllerType.getName()));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.EntityLinks#linkTo(java.lang.Class)
	 */
	@Override
	public LinkBuilder linkFor(Class<?> entity) {
		return linkFor(entity, new Object[0]);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.EntityLinks#linkTo(java.lang.Class, java.lang.Object)
	 */
	@Override
	public LinkBuilder linkFor(Class<?> entity, Object... parameters) {

		Assert.notNull(entity, "Entity must not be null!");

		Class<?> controllerType = entityToController.get(entity);

		if (controllerType == null) {
			throw new IllegalArgumentException(String.format(
					"Type %s is not managed by a Spring MVC controller. Make sure you have annotated your controller with %s!",
					entity.getName(), ExposesResourceFor.class.getName()));
		}

		return linkBuilderFactory.linkTo(controllerType, parameters);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.EntityLinks#getLinkToCollectionResource(java.lang.Class)
	 */
	@Override
	public Link linkToCollectionResource(Class<?> entity) {
		return linkFor(entity).withSelfRel();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.EntityLinks#getLinkToSingleResource(java.lang.Class, java.lang.Object)
	 */
	@Override
	public Link linkToItemResource(Class<?> entity, Object id) {
		return linkFor(entity).slash(id).withSelfRel();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.plugin.core.Plugin#supports(java.lang.Object)
	 */
	@Override
	public boolean supports(Class<?> delimiter) {
		return entityToController.containsKey(delimiter);
	}
}
