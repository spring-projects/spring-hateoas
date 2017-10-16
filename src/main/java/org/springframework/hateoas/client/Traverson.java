/*
 * Copyright 2013-2017 the original author or authors.
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

import static org.springframework.http.HttpMethod.*;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkDiscoverer;
import org.springframework.hateoas.LinkDiscoverers;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.client.Rels.Rel;
import org.springframework.hateoas.hal.HalLinkDiscoverer;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.util.Assert;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

/**
 * Component to ease traversing hypermedia APIs by following links with relation types. Highly inspired by the equally
 * named JavaScript library.
 * 
 * @see https://github.com/basti1302/traverson
 * @author Oliver Gierke
 * @author Dietrich Schulten
 * @author Greg Turnquist
 * @author Tom Bunting
 * @since 0.11
 */
public class Traverson {

	private static final LinkDiscoverers DEFAULT_LINK_DISCOVERERS;

	static {
		LinkDiscoverer discoverer = new HalLinkDiscoverer();
		DEFAULT_LINK_DISCOVERERS = new LinkDiscoverers(OrderAwarePluginRegistry.create(Arrays.asList(discoverer)));
	}

	private final URI baseUri;
	private final List<MediaType> mediaTypes;

	private RestOperations operations;
	private LinkDiscoverers discoverers;

	/**
	 * Creates a new {@link Traverson} interacting with the given base URI and using the given {@link MediaType}s to
	 * interact with the service.
	 * 
	 * @param baseUri must not be {@literal null}.
	 * @param mediaTypes must not be {@literal null} or empty.
	 */
	public Traverson(URI baseUri, MediaType... mediaTypes) {
		this(baseUri, Arrays.asList(mediaTypes));
	}

	/**
	 * Creates a new {@link Traverson} interacting with the given base URI and using the given {@link MediaType}s to
	 * interact with the service.
	 * 
	 * @param baseUri must not be {@literal null}.
	 * @param mediaTypes must not be {@literal null} or empty.
	 */
	public Traverson(URI baseUri, List<MediaType> mediaTypes) {

		Assert.notNull(baseUri, "Base URI must not be null!");
		Assert.notEmpty(mediaTypes, "At least one media type must be given!");

		this.mediaTypes = mediaTypes;
		this.baseUri = baseUri;
		this.discoverers = DEFAULT_LINK_DISCOVERERS;

		setRestOperations(createDefaultTemplate(this.mediaTypes));
	}

	/**
	 * Returns all {@link HttpMessageConverter}s that will be registered for the given {@link MediaType}s by default.
	 * 
	 * @param mediaTypes must not be {@literal null}.
	 * @return
	 */
	public static List<HttpMessageConverter<?>> getDefaultMessageConverters(MediaType... mediaTypes) {
		return getDefaultMessageConverters(Arrays.asList(mediaTypes));
	}

	/**
	 * Returns all {@link HttpMessageConverter}s that will be registered for the given {@link MediaType}s by default.
	 * 
	 * @param mediaTypes must not be {@literal null}.
	 * @return
	 */
	public static List<HttpMessageConverter<?>> getDefaultMessageConverters(List<MediaType> mediaTypes) {

		Assert.notNull(mediaTypes, "Media types must not be null!");

		List<HttpMessageConverter<?>> converters = new ArrayList<>();
		converters.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));

		List<MediaType> halFlavors = getHalJsonFlavors(mediaTypes);

		if (!halFlavors.isEmpty()) {
			converters.add(getHalConverter(halFlavors));
		}

		return converters;
	}

	/**
	 * Returns all HAL JSON compatible media types from the given list.
	 * 
	 * @param mediaTypes must not be {@literal null}.
	 * @return
	 */
	private static List<MediaType> getHalJsonFlavors(Collection<MediaType> mediaTypes) {

		return mediaTypes.stream() //
				.filter(MediaTypes.HAL_JSON::isCompatibleWith) //
				.collect(Collectors.toList());
	}

	private static final RestOperations createDefaultTemplate(List<MediaType> mediaTypes) {

		RestTemplate template = new RestTemplate();
		template.setMessageConverters(getDefaultMessageConverters(mediaTypes));

		return template;
	}

	/**
	 * Creates a new {@link HttpMessageConverter} to support HAL.
	 * 
	 * @return
	 */
	private static final HttpMessageConverter<?> getHalConverter(List<MediaType> halFlavours) {

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Jackson2HalModule());
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();

		converter.setObjectMapper(mapper);
		converter.setSupportedMediaTypes(halFlavours);

		return converter;
	}

	/**
	 * Configures the {@link RestOperations} to use. If {@literal null} is provided a default {@link RestTemplate} will be
	 * used.
	 * 
	 * @param operations
	 * @return
	 */
	public Traverson setRestOperations(RestOperations operations) {

		this.operations = operations == null ? createDefaultTemplate(mediaTypes) : operations;
		return this;
	}

	/**
	 * Sets the {@link LinkDiscoverers} to use. By default a single {@link HalLinkDiscoverer} is registered. If
	 * {@literal null} is provided the default is reapplied.
	 * 
	 * @param discoverer can be {@literal null}.
	 * @return
	 */
	public Traverson setLinkDiscoverers(List<? extends LinkDiscoverer> discoverer) {

		this.discoverers = discoverers == null ? DEFAULT_LINK_DISCOVERERS
				: new LinkDiscoverers(OrderAwarePluginRegistry.create(discoverer));

		return this;
	}

	/**
	 * Sets up a {@link TraversalBuilder} to follow the given rels.
	 * 
	 * @param rels must not be {@literal null} or empty.
	 * @return
	 * @see TraversalBuilder
	 */
	public TraversalBuilder follow(String... rels) {
		return new TraversalBuilder().follow(rels);
	}

	/**
	 * Sets up a {@link TraversalBuilder} for a single rel with customized details.
	 *
	 * @param hop must not be {@literal null}
	 * @return
	 */
	public TraversalBuilder follow(Hop hop) {
		return new TraversalBuilder().follow(hop);
	}

	private HttpEntity<?> prepareRequest(HttpHeaders headers) {

		HttpHeaders toSend = new HttpHeaders();
		toSend.putAll(headers);

		if (headers.getAccept().isEmpty()) {
			toSend.setAccept(mediaTypes);
		}

		return new HttpEntity<Void>(toSend);
	}

	/**
	 * Builder API to customize traversals.
	 * 
	 * @author Oliver Gierke
	 */
	public class TraversalBuilder {

		private final List<Hop> rels = new ArrayList<>();
		private Map<String, Object> templateParameters = new HashMap<>();
		private HttpHeaders headers = new HttpHeaders();

		private TraversalBuilder() {}

		/**
		 * Follows the given rels one by one, which means a request per rel to discover the next resource with the rel in
		 * line.
		 * 
		 * @param rels must not be {@literal null}.
		 * @return
		 */
		public TraversalBuilder follow(String... rels) {

			Assert.notNull(rels, "Rels must not be null!");

			for (String rel : rels) {
				this.rels.add(Hop.rel(rel));
			}

			return this;
		}

		/**
		 * Follows the given rels one by one, which means a request per rel to discover the next resource with the rel in
		 * line.
		 *
		 * @param hop must not be {@literal null}.
		 * @return
		 * @see Hop#rel(String)
		 */
		public TraversalBuilder follow(Hop hop) {

			Assert.notNull(hop, "Hop must not be null!");

			this.rels.add(hop);

			return this;
		}

		/**
		 * Adds the given operations parameters to the traversal. If a link discovered by the traversal is templated, the
		 * given parameters will be used to expand the operations into a resolvable URI.
		 * 
		 * @param parameters can be {@literal null}.
		 * @return
		 */
		public TraversalBuilder withTemplateParameters(Map<String, Object> parameters) {

			this.templateParameters = parameters;
			return this;
		}

		/**
		 * The {@link HttpHeaders} that shall be used for the requests of the traversal.
		 * 
		 * @param headers can be {@literal null}.
		 * @return
		 */
		public TraversalBuilder withHeaders(HttpHeaders headers) {

			this.headers = headers;
			return this;
		}

		/**
		 * Executes the traversal and marshals the final response into an object of the given type.
		 * 
		 * @param type must not be {@literal null}.
		 * @return
		 */
		public <T> T toObject(Class<T> type) {

			Assert.notNull(type, "Target type must not be null!");
			return operations.exchange(traverseToExpandedFinalUrl(), GET, prepareRequest(headers), type).getBody();
		}

		/**
		 * Executes the traversal and marshals the final response into an object of the given
		 * {@link ParameterizedTypeReference}.
		 * 
		 * @param type must not be {@literal null}.
		 * @return
		 */
		public <T> T toObject(ParameterizedTypeReference<T> type) {

			Assert.notNull(type, "Target type must not be null!");
			return operations.exchange(traverseToExpandedFinalUrl(), GET, prepareRequest(headers), type).getBody();
		}

		/**
		 * Executes the traversal and returns the result of the given JSON Path expression evaluated against the final
		 * representation.
		 * 
		 * @param jsonPath must not be {@literal null} or empty.
		 * @return
		 */
		public <T> T toObject(String jsonPath) {

			Assert.hasText(jsonPath, "JSON path must not be null or empty!");

			String forObject = operations.exchange(traverseToExpandedFinalUrl(), GET, prepareRequest(headers), String.class)
					.getBody();
			return JsonPath.read(forObject, jsonPath);
		}

		/**
		 * Returns the raw {@link ResponseEntity} with the representation unmarshalled into an instance of the given type.
		 * 
		 * @param type must not be {@literal null}.
		 * @return
		 */
		public <T> ResponseEntity<T> toEntity(Class<T> type) {

			Assert.notNull(type, "Target type must not be null!");
			return operations.exchange(traverseToExpandedFinalUrl(), GET, prepareRequest(headers), type);
		}

		/**
		 * Returns the {@link Link} found for the last rel in the rels configured to follow. Will expand the final
		 * {@link Link} using the
		 * 
		 * @return
		 * @see #withTemplateParameters(Map)
		 * @since 0.15
		 */
		public Link asLink() {
			return traverseToLink(true);
		}

		/**
		 * Returns the templated {@link Link} found for the last rel in the rels configured to follow.
		 * 
		 * @return
		 * @since 0.17
		 */
		public Link asTemplatedLink() {
			return traverseToLink(false);
		}

		private Link traverseToLink(boolean expandFinalUrl) {

			Assert.isTrue(rels.size() > 0, "At least one rel needs to be provided!");
			return new Link(expandFinalUrl ? traverseToExpandedFinalUrl().toString() : traverseToFinalUrl(),
					rels.get(rels.size() - 1).getRel());
		}

		private String traverseToFinalUrl() {

			String uri = getAndFindLinkWithRel(baseUri.toString(), rels.iterator());
			return new UriTemplate(uri).toString();
		}

		private URI traverseToExpandedFinalUrl() {

			String uri = getAndFindLinkWithRel(baseUri.toString(), rels.iterator());
			return new UriTemplate(uri).expand(templateParameters);
		}

		private String getAndFindLinkWithRel(String uri, Iterator<Hop> rels) {

			if (!rels.hasNext()) {
				return uri;
			}

			HttpEntity<?> request = prepareRequest(headers);
			UriTemplate template = new UriTemplate(uri);

			ResponseEntity<String> responseEntity = operations.exchange(template.expand(), GET, request, String.class);
			MediaType contentType = responseEntity.getHeaders().getContentType();
			String responseBody = responseEntity.getBody();

			Hop thisHop = rels.next();

			Rel rel = Rels.getRelFor(thisHop.getRel(), discoverers);
			Link link = rel.findInResponse(responseBody, contentType);

			if (link == null) {
				throw new IllegalStateException(
						String.format("Expected to find link with rel '%s' in response %s!", rel, responseBody));
			}

			/**
			 * Don't expand if the parameters are empty
			 */
			if (!thisHop.hasParameters()) {
				return getAndFindLinkWithRel(link.getHref(), rels);
			} else {
				return getAndFindLinkWithRel(link.expand(thisHop.getMergedParameters(templateParameters)).getHref(), rels);
			}
		}
	}
}
