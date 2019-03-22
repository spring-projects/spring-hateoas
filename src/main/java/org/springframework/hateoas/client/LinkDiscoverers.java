/*
 * Copyright 2013 the original author or authors.
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
package org.springframework.hateoas.client;

import java.util.Optional;

import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.util.Assert;

/**
 * Value object to wrap a {@link PluginRegistry} for {@link LinkDiscoverer} so that it's easier to inject them into
 * clients wanting to lookup a {@link LinkDiscoverer} for a given {@link MediaTypes}.
 *
 * @author Oliver Gierke
 */
public class LinkDiscoverers {

	private final PluginRegistry<LinkDiscoverer, MediaType> discoverers;

	/**
	 * Creates a new {@link LinkDiscoverers} instance with the given {@link PluginRegistry}.
	 *
	 * @param discoverers must not be {@literal null}.
	 */
	public LinkDiscoverers(PluginRegistry<LinkDiscoverer, MediaType> discoverers) {

		Assert.notNull(discoverers, "Registry of LinkDiscoverer must not be null!");
		this.discoverers = discoverers;
	}

	/**
	 * Returns the {@link LinkDiscoverer} suitable for the given {@link MediaType}.
	 *
	 * @param mediaType
	 * @return will never be {@literal null}.
	 */
	public Optional<LinkDiscoverer> getLinkDiscovererFor(MediaType mediaType) {
		return discoverers.getPluginFor(mediaType);
	}

	/**
	 * Returns the {@link LinkDiscoverer} suitable for the given media type.
	 *
	 * @param mediaType
	 * @return
	 */
	public Optional<LinkDiscoverer> getLinkDiscovererFor(String mediaType) {
		return getLinkDiscovererFor(MediaType.valueOf(mediaType));
	}

	/**
	 * Returns the {@link LinkDiscoverer} suitable for the given {@link MediaType}.
	 *
	 * @param mediaType
	 * @return will never be {@literal null}.
	 */
	public LinkDiscoverer getRequiredLinkDiscovererFor(MediaType mediaType) {
		return discoverers.getRequiredPluginFor(mediaType);
	}

	/**
	 * Returns the {@link LinkDiscoverer} suitable for the given media type.
	 *
	 * @param mediaType
	 * @return
	 */
	public LinkDiscoverer getRequiredLinkDiscovererFor(String mediaType) {
		return getRequiredLinkDiscovererFor(MediaType.valueOf(mediaType));
	}
}
