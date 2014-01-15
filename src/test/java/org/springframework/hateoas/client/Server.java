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
package org.springframework.hateoas.client;

import static net.jadler.Jadler.*;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.UUID;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.core.EvoInflectorRelProvider;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Helper class for integration tests.
 * 
 * @author Oliver Gierke
 */
public class Server implements Closeable {

	private final ObjectMapper mapper;
	private final RelProvider relProvider;

	private final MultiValueMap<Link, Link> baseResources = new LinkedMultiValueMap<Link, Link>();

	public Server() {

		this.relProvider = new EvoInflectorRelProvider();

		this.mapper = new ObjectMapper();
		this.mapper.registerModule(new Jackson2HalModule());
		this.mapper.setHandlerInstantiator(new Jackson2HalModule.HalHandlerInstantiator(relProvider, null));

		initJadler(). //
				that().//
				respondsWithDefaultContentType(MediaTypes.HAL_JSON.toString()). //
				respondsWithDefaultStatus(200).//
				respondsWithDefaultEncoding(Charset.forName("UTF-8"));

		onRequest(). //
				havingPathEqualTo("/"). //
				respond(). //
				withBody("");
	}

	public String rootResource() {
		return "http://localhost:" + port();
	}

	public String mockResourceFor(Resource<?> resource) {

		Object content = resource.getContent();

		Class<? extends Object> type = content.getClass();
		String collectionRel = relProvider.getCollectionResourceRelFor(type);
		String singleRel = relProvider.getItemResourceRelFor(type);

		String baseResourceUri = String.format("%s/%s", rootResource(), collectionRel);
		String resourceUri = String.format("%s/%s", baseResourceUri, UUID.randomUUID().toString());

		baseResources.add(new Link(baseResourceUri, collectionRel), new Link(resourceUri, singleRel));

		register(resourceUri, resource);

		return resourceUri;
	}

	public void finishMocking() {

		Resources<String> resources = new Resources<String>(Collections.<String> emptyList());

		for (Link link : baseResources.keySet()) {

			resources.add(link);

			Resources<String> nested = new Resources<String>(Collections.<String> emptyList());
			nested.add(baseResources.get(link));

			register(link.getHref(), nested);
		}

		register("/", resources);
	}

	private void register(String path, Object response) {

		path = path.startsWith(rootResource()) ? path.substring(rootResource().length()) : path;

		try {
			onRequest(). //
					havingMethodEqualTo("GET"). //
					havingPathEqualTo(path). //
					respond().//
					withBody(mapper.writeValueAsString(response));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	/* 
	 * (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {
		closeJadler();
	}
}
