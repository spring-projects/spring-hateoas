/*
 * Copyright 2013-2019 the original author or authors.
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
package org.springframework.hateoas.server.mvc;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.util.Assert;

/**
 * {@link LinkRelationProvider} inspecting {@link ExposesResourceFor} annotations on controller classes.
 *
 * @author Oliver Drotbohm
 */
public class ControllerLinkRelationProvider implements LinkRelationProvider {

	private final Class<?> controllerType;
	private final Class<?> entityType;
	private final PluginRegistry<LinkRelationProvider, LookupContext> providers;

	/**
	 * Creates a new {@link ControllerLinkRelationProvider}
	 *
	 * @param controller must not be {@literal null}.
	 * @param providers must not be {@literal null}.
	 */
	public ControllerLinkRelationProvider(Class<?> controller,
			PluginRegistry<LinkRelationProvider, LookupContext> providers) {

		Assert.notNull(controller, "Controller must not be null!");
		Assert.notNull(providers, "LinkRelationProviders must not be null!");

		ExposesResourceFor annotation = AnnotatedElementUtils.findMergedAnnotation(controller, ExposesResourceFor.class);

		if (annotation == null) {
			throw new IllegalArgumentException(
					String.format("Controller %s must be annotated with @ExposesResourceFor(…)!", controller.getName()));
		}

		this.controllerType = controller;
		this.entityType = annotation.value();
		this.providers = providers;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.LinkRelationProvider#getItemResourceRelFor(java.lang.Class)
	 */
	@Override
	public LinkRelation getItemResourceRelFor(Class<?> resource) {

		LookupContext context = LookupContext.forItemResourceRelLookup(entityType);

		return providers.getRequiredPluginFor(context).getItemResourceRelFor(resource);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.LinkRelationProvider#getCollectionResourceRelFor(java.lang.Class)
	 */
	@Override
	public LinkRelation getCollectionResourceRelFor(Class<?> resource) {

		LookupContext context = LookupContext.forCollectionResourceRelLookup(entityType);

		return providers.getRequiredPluginFor(context).getCollectionResourceRelFor(resource);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.plugin.core.Plugin#supports(java.lang.Object)
	 */
	@Override
	public boolean supports(LookupContext context) {
		return context.handlesType(controllerType);
	}
}
