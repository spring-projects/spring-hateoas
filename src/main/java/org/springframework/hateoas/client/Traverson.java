/*
 * Copyright 2013-2020 the original author or authors.
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

import static org.springframework.http.HttpMethod.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.client.Rels.Rel;
import org.springframework.hateoas.mediatype.hal.HalLinkDiscoverer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.util.Assert;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import com.jayway.jsonpath.JsonPath;

/**
 * Component to ease traversing hypermedia APIs by following links with relation types. Highly inspired by the equally
 * named JavaScript library.
 *
 * @see {@link https://github.com/basti1302/traverson}
 * @author Oliver Gierke
 * @author Dietrich Schulten
 * @author Greg Turnquist
 * @author Tom Bunting
 * @author Manish Misra
 * @author Michael Wirth
 * @since 0.11
 */
public class Traverson {

	private static final TraversonDefaults DEFAULTS;

	static {

		List<TraversonDefaults> ALL_DEFAULTS = SpringFactoriesLoader.loadFactories(TraversonDefaults.class,
				Traverson.class.getClassLoader());

		Assert.isTrue(ALL_DEFAULTS.size() == 1,
				() -> String.format("Expected to find only one TraversonDefaults instance, but found: %s", //
						ALL_DEFAULTS.stream() //
								.map(Object::getClass) //
								.map(Class::getName) //
								.collect(Collectors.joining(", "))));

		DEFAULTS = ALL_DEFAULTS.get(0);
	}

	private final URI baseUri;
	private final List<MediaType> mediaTypes;

	private RestOperations operations;
	private LinkDiscoverers discoverers;

	/**
	 * Creates a new {@link Traverson} with {@literal null} {@literal URI} and empty list of {@link MediaType}s. This is
	 * to support using {@link #baseUri(URI)}} and {@link #accept(MediaType...)}} fluently.
	 */
	public Traverson() {

		this.baseUri = null;
		this.mediaTypes = Collections.emptyList();

		setRestOperations(createDefaultTemplate(this.mediaTypes));
		setLinkDiscoverers(Collections.emptyList());
	}

	/**
	 * Creates a new {@link Traverson} interacting with the given base URI and using the given {@link MediaType}s to
	 * interact with the service.
	 *
	 * @param baseUri must not be {@literal null}.
	 * @param mediaTypes must not be {@literal null}.
	 * @deprecated Migrate to {@link #baseUri(URI)} combined with {@link #accept(MediaType...)} before this constructor is made private.
	 */
	@Deprecated
	public Traverson(URI baseUri, MediaType... mediaTypes) {
		this(baseUri, Arrays.asList(mediaTypes));
	}

	/**
	 * Creates a new {@link Traverson} interacting with the given base URI and using the given {@link MediaType}s to
	 * interact with the service.
	 *
	 * @param baseUri must not be {@literal null}.
	 * @param mediaTypes must not be {@literal null} or empty.
	 * @deprecated Migrate to {@link #baseUri(URI)} combined with {@link #accept(MediaType...)} before this constructor is made private.
	 */
	@Deprecated
	public Traverson(URI baseUri, List<MediaType> mediaTypes) {

		Assert.notNull(baseUri, "Base URI must not be null!");
		Assert.notNull(mediaTypes, "At least one media type must be given!");

		this.mediaTypes = mediaTypes;
		this.baseUri = baseUri;

		setLinkDiscoverers(DEFAULTS.getLinkDiscoverers(mediaTypes));
		setRestOperations(createDefaultTemplate(this.mediaTypes));
	}
	/**
	 * Create a new {@link Traverson} using the provided {@code baseUri}.
	 *
	 * @param baseUri
	 * @return {@link Traverson}
	 */
	public Traverson baseUri(URI baseUri) {

		Assert.notNull(baseUri, "baseUri must not be null!");

		return new Traverson(baseUri, this.mediaTypes);
	}

	/**
	 * Create a new {@link Traverson} using the provided {@link MediaType}s.
	 *
	 * @param mediaTypes
	 * @return {@link Traverson}
	 */
	public Traverson accept(MediaType... mediaTypes) {

		Assert.notEmpty(mediaTypes, "mediaTypes must not be null!");

		return new Traverson(this.baseUri, Arrays.asList(mediaTypes));
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

	/**
	 * Returns all {@link HttpMessageConverter}s that will be registered for the given {@link MediaType}s by default.
	 *
	 * @param mediaTypes must not be {@literal null}.
	 * @return
	 */
	public static List<HttpMessageConverter<?>> getDefaultMessageConverters(MediaType... mediaTypes) {
		return DEFAULTS.getHttpMessageConverters(Arrays.asList(mediaTypes));
	}

	/**
	 * Configures the {@link RestOperations} to use. If {@literal null} is provided a default {@link RestTemplate} will be
	 * used.
	 *
	 * @param operations
	 * @return
	 */
	public Traverson setRestOperations(@Nullable RestOperations operations) {

		this.operations = operations == null //
				? createDefaultTemplate(this.mediaTypes) //
				: operations;

		return this;
	}

	/**
	 * Sets the {@link LinkDiscoverers} to use. By default a single {@link HalLinkDiscoverer} is registered. If
	 * {@literal null} is provided the default is reapplied.
	 *
	 * @param discoverer can be {@literal null}.
	 * @return
	 */
	public Traverson setLinkDiscoverers(@Nullable List<? extends LinkDiscoverer> discoverer) {

		List<? extends LinkDiscoverer> defaultedDiscoverers = discoverer == null //
				? DEFAULTS.getLinkDiscoverers(mediaTypes) //
				: discoverer;

		this.discoverers = new LinkDiscoverers(PluginRegistry.of(defaultedDiscoverers));

		return this;
	}

	private static final RestOperations createDefaultTemplate(List<MediaType> mediaTypes) {

		RestTemplate template = new RestTemplate();
		template.setMessageConverters(DEFAULTS.getHttpMessageConverters(mediaTypes));

		return template;
	}

	/**
	 * Builder API to customize traversals.
	 *
	 * @author Oliver Gierke
	 */
	public class TraversalBuilder {

		private static final String MEDIA_TYPE_HEADER_NOT_FOUND = "Response for request to %s did not expose a content type! Unable to identify links!";
		private static final String LINK_NOT_FOUND = "Expected to find link with rel '%s' in response %s!";

		private final List<Hop> rels = new ArrayList<>();
		private Map<String, Object> templateParameters = new HashMap<>();
		private HttpHeaders headers = new HttpHeaders();

		private TraversalBuilder() {

		}

		/**
		 * Follows the given rels one by one, which means a request per rel to discover the next resource with the rel in
		 * line.
		 *
		 * @param rels must not be {@literal null}.
		 * @return
		 */
		public TraversalBuilder follow(String... rels) {

			Assert.notNull(rels, "Rels must not be null!");

			Arrays.stream(rels) //
					.map(Hop::rel) //
					.forEach(this.rels::add);

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

			Assert.notNull(parameters, "Parameters must not be null!");

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

			Assert.notNull(headers, "Headers must not be null!");

			this.headers = headers;
			return this;
		}

		/**
		 * Executes the traversal and marshals the final response into an object of the given type.
		 *
		 * @param type must not be {@literal null}.
		 * @return
		 * @deprecated Use {@link #as(Class)}.
		 */
		@Nullable
		@Deprecated
		public <T> T toObject(Class<T> type) {
			return as(type);
		}

		/**
		 * Executes the traversal and marshals the final response into an object of the given type.
		 *
		 * @param type must not be {@literal null}.
		 * @return
		 */
		@Nullable
		public <T> T as(Class<T> type) {
			return toEntity(type).getBody();
		}

		/**
		 * Executes the traversal and marshals the final response into an object of the given
		 * {@link ParameterizedTypeReference}.
		 *
		 * @param type must not be {@literal null}.
		 * @return
		 * @deprecated Use {@link #as(ParameterizedTypeReference)}.
		 */
		@Nullable
		@Deprecated
		public <T> T toObject(ParameterizedTypeReference<T> type) {
			return as(type);
		}

		/**
		 * Executes the traversal and marshals the final response into an object of the given
		 * {@link ParameterizedTypeReference}.
		 *
		 * @param type must not be {@literal null}.
		 * @return
		 */
		@Nullable
		public <T> T as(ParameterizedTypeReference<T> type) {
			return toEntity(type).getBody();
		}

		/**
		 * Executes the traversal and returns the result of the given JSON Path expression evaluated against the final
		 * representation.
		 *
		 * @deprecated Use {@link #as(String)}.
		 */
		@Deprecated
		public <T> T toObject(String jsonPath) {
			return as(jsonPath);
		}

		/**
		 * Executes the traversal and returns the result of the given JSON Path expression evaluated against the final
		 * representation.
		 *
		 * @param jsonPath must not be {@literal null} or empty.
		 * @return
		 */
		public <T> T as(String jsonPath) {

			Assert.hasText(jsonPath, "jsonPath must not be null!");

			return JsonPath.read(as(String.class), jsonPath);
		}

		/**
		 * Returns the raw {@link ResponseEntity} with the representation unmarshalled into an instance of the given type.
		 *
		 * @param type must not be {@literal null}.
		 * @return
		 */
		public <T> ResponseEntity<T> toEntity(Class<T> type) {

			Assert.notNull(type, "Target type must not be null!");

			URIAndHeaders uriAndHeaders = traverseToExpandedFinalUrl();
			HttpEntity<?> requestEntity = prepareRequest(mergeHeaders(this.headers, uriAndHeaders.getHttpHeaders()));

			return operations.exchange(uriAndHeaders.getUri(), GET, requestEntity, type);
		}

		/**
		 * Returns the raw {@link ResponseEntity} with the representation unmarshalled into an instance of the given
		 * {@link ParameterizedTypeReference}..
		 *
		 * @param type must not be {@literal null}.
		 * @return
		 */
		public <T> ResponseEntity<T> toEntity(ParameterizedTypeReference<T> type) {

			Assert.notNull(type, "Target type must not be null!");

			URIAndHeaders uriAndHeaders = traverseToExpandedFinalUrl();
			HttpEntity<?> requestEntity = prepareRequest(mergeHeaders(this.headers, uriAndHeaders.getHttpHeaders()));

			return operations.exchange(uriAndHeaders.getUri(), GET, requestEntity, type);
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
		 * Returns the templated {@link Link} found for the last relation in the rels configured to follow.
		 *
		 * @return
		 * @since 0.17
		 */
		public Link asTemplatedLink() {
			return traverseToLink(false);
		}

		private Link traverseToLink(boolean expandFinalUrl) {

			Assert.isTrue(rels.size() > 0, "At least one rel needs to be provided!");

			return Link.of(expandFinalUrl ? traverseToExpandedFinalUrl().getUri().toString() : traverseToFinalUrl().getUri(),
					rels.get(rels.size() - 1).getRel());
		}

		private UriStringAndHeaders traverseToFinalUrl() {

			UriStringAndHeaders uriAndHeaders = getAndFindLinkWithRel(baseUri.toString(), rels.iterator(), HttpHeaders.EMPTY);

			return new UriStringAndHeaders(UriTemplate.of(uriAndHeaders.getUri()).toString(), uriAndHeaders.getHttpHeaders());
		}

		private URIAndHeaders traverseToExpandedFinalUrl() {

			UriStringAndHeaders uriAndHeaders = getAndFindLinkWithRel(baseUri.toString(), rels.iterator(), HttpHeaders.EMPTY);

			return new URIAndHeaders(UriTemplate.of(uriAndHeaders.getUri()).expand(templateParameters),
					uriAndHeaders.getHttpHeaders());
		}

		private UriStringAndHeaders getAndFindLinkWithRel(String uri, Iterator<Hop> rels, HttpHeaders extraHeaders) {

			if (!rels.hasNext()) {
				return new UriStringAndHeaders(uri, extraHeaders);
			}

			HttpEntity<?> request = prepareRequest(mergeHeaders(this.headers, extraHeaders));
			URI target = UriTemplate.of(uri).expand();

			ResponseEntity<String> responseEntity = operations.exchange(target, GET, request, String.class);
			MediaType contentType = responseEntity.getHeaders().getContentType();

			if (contentType == null) {
				throw new IllegalStateException(String.format(MEDIA_TYPE_HEADER_NOT_FOUND, target));
			}

			String responseBody = responseEntity.getBody();

			Hop thisHop = rels.next();
			Rel rel = Rels.getRelFor(thisHop.getRel(), discoverers);

			Link link = rel.findInResponse(responseBody == null ? "" : responseBody, contentType) //
					.orElseThrow(() -> new IllegalStateException(String.format(LINK_NOT_FOUND, rel, responseBody)));

			String href = thisHop.hasParameters() //
					? link.expand(thisHop.getMergedParameters(this.templateParameters)).getHref() //
					: link.getHref();

			return getAndFindLinkWithRel(href, rels, thisHop.getHeaders());
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
		 * Combine two sets of {@link HttpHeaders} into one.
		 *
		 * @param headersA
		 * @param headersB
		 * @return
		 */
		private HttpHeaders mergeHeaders(HttpHeaders headersA, HttpHeaders headersB) {

			HttpHeaders mergedHeaders = new HttpHeaders();

			mergedHeaders.addAll(headersA);
			mergedHeaders.addAll(headersB);

			return mergedHeaders;
		}
	}

	/**
	 * Temporary container for a string-based {@literal URI} and {@link HttpHeaders}.
	 */
	private static final class UriStringAndHeaders {

		private final String uri;
		private final HttpHeaders httpHeaders;

		UriStringAndHeaders(String uri, HttpHeaders httpHeaders) {

			this.uri = uri;
			this.httpHeaders = httpHeaders;
		}

		String getUri() {
			return this.uri;
		}

		HttpHeaders getHttpHeaders() {
			return this.httpHeaders;
		}

		@Override
		public boolean equals(Object o) {

			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			UriStringAndHeaders that = (UriStringAndHeaders) o;
			return Objects.equals(this.uri, that.uri) && Objects.equals(this.httpHeaders, that.httpHeaders);
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.uri, this.httpHeaders);
		}

		public String toString() {
			return "Traverson.UriStringAndHeaders(uri=" + this.uri + ", httpHeaders=" + this.httpHeaders + ")";
		}
	}

	/**
	 * Temporary container for a {@link URI}-based {@literal URI} and {@link HttpHeaders}.
	 */
	private static final class URIAndHeaders {

		private final URI uri;
		private final HttpHeaders httpHeaders;

		URIAndHeaders(URI uri, HttpHeaders httpHeaders) {

			this.uri = uri;
			this.httpHeaders = httpHeaders;
		}

		URI getUri() {
			return this.uri;
		}

		HttpHeaders getHttpHeaders() {
			return this.httpHeaders;
		}

		@Override
		public boolean equals(Object o) {

			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			URIAndHeaders that = (URIAndHeaders) o;
			return Objects.equals(this.uri, that.uri) && Objects.equals(this.httpHeaders, that.httpHeaders);
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.uri, this.httpHeaders);
		}

		public String toString() {
			return "Traverson.URIAndHeaders(uri=" + this.uri + ", httpHeaders=" + this.httpHeaders + ")";
		}
	}
}
