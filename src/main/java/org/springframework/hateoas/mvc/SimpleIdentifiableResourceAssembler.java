/*
 * Copyright 2017 the original author or authors.
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
package org.springframework.hateoas.mvc;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import org.springframework.core.GenericTypeResolver;
import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.SimpleResourceAssembler;
import org.springframework.hateoas.core.EvoInflectorRelProvider;

/**
 * Using a Spring MVC-based {@link ControllerLinkBuilder} and an {@link Identifiable} domain object, construct
 * a {@link SimpleResourceAssembler}.
 *
 * @author Greg Turnquist
 */
public class SimpleIdentifiableResourceAssembler<T extends Identifiable<?>> implements SimpleResourceAssembler<T> {

	/**
	 * The Spring MVC class for the {@link Identifiable} from which links will be built.
	 */
	private final Class<?> controllerClass;

	/**
	 * A {@link RelProvider} to look up names of links as options for resource paths.
	 */
	private final RelProvider relProvider;

	/**
	 * A {@link Class} depicting the {@link Identifiable}'s type.
	 */
	private final Class<?> resourceType;

	/**
	 * Default base path as empty.
	 */
	private String basePath = "";

	/**
	 * Default assembler based on a Spring MVC controller, resource type, and {@link RelProvider}. With this combination
	 * of information, resources can be defined.
	 *
	 * @see #setBasePath(String) to adjust base path to something like "/api"/
	 *
	 * @param controllerClass - Spring MVC controller to base links off of
	 * @param relProvider - generates the links items
	 */
	public SimpleIdentifiableResourceAssembler(Class<?> controllerClass, RelProvider relProvider) {

		this.controllerClass = controllerClass;
		this.relProvider = relProvider;

		// Find the "T" type contained in "T extends Identifiable<?>", e.g. SimpleIdentifiableResourceAssembler<User> -> User
		this.resourceType = GenericTypeResolver.resolveTypeArgument(this.getClass(), SimpleIdentifiableResourceAssembler.class);
	}

	/**
	 * Alternate constructor that falls back to {@link EvoInflectorRelProvider}.
	 * 
	 * @param controllerClass
	 */
	public SimpleIdentifiableResourceAssembler(Class<?> controllerClass) {
		this(controllerClass, new EvoInflectorRelProvider());
	}

	/**
	 * Define links to add to every {@link Resource}.
	 *
	 * @param resource
	 */
	@Override
	public void addLinks(Resource<T> resource) {

		Link resourceSelfLink = getCollectionLinkBuilder().slash(resource.getContent()).withSelfRel();
		Link resourceCollectionLink = getCollectionLinkBuilder().withRel(this.relProvider.getCollectionResourceRelFor(this.resourceType));

		resource.add(resourceSelfLink, resourceCollectionLink);
	}

	/**
	 * Define links to add to {@link Resources} collection.
	 *
	 * @param resources
	 */
	@Override
	public void addLinks(Resources<Resource<T>> resources) {

		Link resourceCollectionLink = getCollectionLinkBuilder().withSelfRel();
		resources.add(resourceCollectionLink);
	}

	/**
	 * Build up a URI for the collection using the Spring MVC controller followed by the resource type transformed
	 * by the {@link RelProvider}.
	 *
	 * Assumption is that a {@literal EmployeeController} serving up {@literal Employee}
	 * objects will be serving resources at {@code /employees} and {@code /employees/1} respectively.
	 *
	 * If this is not the case, simply override this method in your concrete instance, or simply resort to
	 * overriding {@link #addLinks(Resource)} and {@link #addLinks(Resources)} where you have full control over exactly
	 * what links are put in the individual and collection resources.
	 *
	 * @return
	 */
	protected LinkBuilder getCollectionLinkBuilder() {

		String[] pathComponents = (getPrefix() + this.relProvider.getCollectionResourceRelFor(this.resourceType)).split("/");

		ControllerLinkBuilder linkBuilder = linkTo(this.controllerClass);

		for (String pathComponent : pathComponents) {
			if (!pathComponent.isEmpty()) {
				linkBuilder = linkBuilder.slash(pathComponent);
			}
		}

		return linkBuilder;
	}

	/**
	 * Construct the prefix to a resource, with {@literal "/"} being the basis for no prefix at all.
	 *
	 * @return
	 */
	private String getPrefix() {
		return getBasePath().isEmpty() ? "" : getBasePath() + "/";
	}

	public String getBasePath() {
		return this.basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}
}
