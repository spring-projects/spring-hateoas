/*
 * Copyright 2013-2015 the original author or authors.
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
import static org.hamcrest.Matchers.*;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.UUID;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.core.EvoInflectorRelProvider;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Helper class for integration tests.
 * 
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
public class Server implements Closeable {

	private final ObjectMapper mapper;
	private final RelProvider relProvider;

	private final MultiValueMap<Link, Link> baseResources = new LinkedMultiValueMap<>();
	private final ResourceLoader resourceLoader = new DefaultResourceLoader();

	public Server() {

		this.relProvider = new EvoInflectorRelProvider();

		this.mapper = new ObjectMapper();
		this.mapper.registerModule(new Jackson2HalModule());
		this.mapper.setHandlerInstantiator(new Jackson2HalModule.HalHandlerInstantiator(relProvider, null, null));

		initJadler() //
				.withDefaultResponseContentType(MediaTypes.HAL_JSON.toString()) //
				.withDefaultResponseEncoding(Charset.forName("UTF-8")) //
				.withDefaultResponseStatus(200);

		onRequest(). //
				havingPathEqualTo("/"). //
				respond(). //
				withBody("");

		// For GitHubLinkDiscoverer tests

		onRequest(). //
				havingPathEqualTo("/github"). //
				respond(). //
				withBody("{ \"foo_url\" : \"" + rootResource() + "/github/4711\"}"). //
				withContentType(MediaType.APPLICATION_JSON_VALUE);

		onRequest(). //
				havingPathEqualTo("/github/4711"). //
				respond(). //
				withBody("{ \"key\" : \"value\"}"). //
				withContentType(MediaType.APPLICATION_JSON_VALUE);

		// For templated link access

		onRequest(). //
				havingPathEqualTo("/link"). //
				respond(). //
				withBody("{ \"_links\" : { \"self\" : { \"href\" : \"/{?template}\" }}}"). //
				withContentType(MediaTypes.HAL_JSON.toString());

		// Sample traversal of HAL docs based on Spring-a-Gram showcase
		org.springframework.core.io.Resource springagramRoot = resourceLoader
				.getResource("classpath:springagram-root.json");
		org.springframework.core.io.Resource springagramItems = resourceLoader
				.getResource("classpath:springagram-items.json");
		org.springframework.core.io.Resource springagramItem = resourceLoader
				.getResource("classpath:springagram-item.json");
		org.springframework.core.io.Resource springagramItemWithoutImage = resourceLoader
				.getResource("classpath:springagram-item-without-image.json");

		String springagramRootTemplate;
		String springagramItemsTemplate;
		String springagramItemTemplate;
		String springagramItemWithoutImageTemplate;

		try {
			springagramRootTemplate = StreamUtils.copyToString(springagramRoot.getInputStream(), Charset.forName("UTF-8"));
			springagramItemsTemplate = StreamUtils.copyToString(springagramItems.getInputStream(), Charset.forName("UTF-8"));
			springagramItemTemplate = StreamUtils.copyToString(springagramItem.getInputStream(), Charset.forName("UTF-8"));
			springagramItemWithoutImageTemplate = StreamUtils.copyToString(springagramItemWithoutImage.getInputStream(),
					Charset.forName("UTF-8"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		String springagramRootHalDocument = String.format(springagramRootTemplate, rootResource(), rootResource());
		String springagramItemsHalDocument = String.format(springagramItemsTemplate, rootResource(), rootResource(),
				rootResource());
		String springagramItemHalDocument = String.format(springagramItemTemplate, rootResource(), rootResource());
		String springagramItemWithoutImageHalDocument = String.format(springagramItemWithoutImageTemplate, rootResource());

		onRequest(). //
				havingPathEqualTo("/springagram"). //
				respond(). //
				withBody(springagramRootHalDocument). //
				withContentType(MediaTypes.HAL_JSON.toString());

		onRequest(). //
				havingPathEqualTo("/springagram/items"). //
				havingQueryString(equalTo("projection=noImages")). //
				respond(). //
				withBody(springagramItemsHalDocument). //
				withContentType(MediaTypes.HAL_JSON.toString());

		onRequest(). //
				havingPathEqualTo("/springagram/items/1"). //
				respond(). //
				withBody(springagramItemHalDocument). //
				withContentType(MediaTypes.HAL_JSON.toString());

		onRequest(). //
				havingPathEqualTo("/springagram/items/1"). //
				havingQueryString(equalTo("projection=noImages")). //
				respond(). //
				withBody(springagramItemWithoutImageHalDocument). //
				withContentType(MediaTypes.HAL_JSON.toString());

		// For Traverson URI double encoding test

		onRequest(). //
				havingPathEqualTo("/springagram/items"). //
				havingQueryString(equalTo("projection=no%20images")). //
				respond(). //
				withBody(springagramItemsHalDocument). //
				withContentType(MediaTypes.HAL_JSON.toString());

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

		Resources<String> resources = new Resources<>(Collections.emptyList());

		for (Link link : baseResources.keySet()) {

			resources.add(link);

			Resources<String> nested = new Resources<>(Collections.emptyList());
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
