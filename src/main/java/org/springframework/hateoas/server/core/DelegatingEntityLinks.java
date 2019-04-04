/*
 * Copyright 2012-2013 the original author or authors.
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

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.LinkBuilder;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.util.Assert;

/**
 * {@link EntityLinks} implementation that delegates to the {@link EntityLinks} instances registered in the
 * {@link PluginRegistry} given on instance creation.
 * 
 * @author Oliver Gierke
 */
public class DelegatingEntityLinks extends AbstractEntityLinks {

	private final PluginRegistry<EntityLinks, Class<?>> delegates;

	/**
	 * Creates a new {@link DelegatingEntityLinks} using the given {@link PluginRegistry}.
	 * 
	 * @param plugins must not be {@literal null}.
	 */
	public DelegatingEntityLinks(PluginRegistry<EntityLinks, Class<?>> plugins) {

		Assert.notNull(plugins, "PluginRegistry must not be null!");
		this.delegates = plugins;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.EntityLinks#linkFor(java.lang.Class)
	 */
	@Override
	public LinkBuilder linkFor(Class<?> type) {
		return getPluginFor(type).linkFor(type);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.EntityLinks#linkFor(java.lang.Class, java.lang.Object[])
	 */
	@Override
	public LinkBuilder linkFor(Class<?> type, Object... parameters) {
		return getPluginFor(type).linkFor(type, parameters);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.EntityLinks#getLinkToCollectionResource(java.lang.Class)
	 */
	@Override
	public Link linkToCollectionResource(Class<?> type) {
		return getPluginFor(type).linkToCollectionResource(type);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.EntityLinks#getLinkToSingleResource(java.lang.Class, java.lang.Object)
	 */
	@Override
	public Link linkToItemResource(Class<?> type, Object id) {
		return getPluginFor(type).linkToItemResource(type, id);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.plugin.core.Plugin#supports(java.lang.Object)
	 */
	@Override
	public boolean supports(Class<?> delimiter) {
		return delegates.hasPluginFor(delimiter);
	}

	/**
	 * Returns the plugin for the given type or throws an {@link IllegalArgumentException} if no delegate
	 * {@link EntityLinks} can be found.
	 * 
	 * @param type must not be {@literal null}.
	 * @return
	 */
	private EntityLinks getPluginFor(Class<?> type) {

		return delegates.getPluginFor(type) //
				.orElseThrow(() -> new IllegalArgumentException(
						String.format("Cannot determine link for %s! No EntityLinks instance found supporting the domain type!",
								type.getName())));
	}
}
