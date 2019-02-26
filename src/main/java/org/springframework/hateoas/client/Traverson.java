/*
 * Copyright 2013-2019 the original author or authors.
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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkDiscoverers;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.client.Rels.Rel;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.RestOperations;

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
 * @author Manish Misra
 * @since 0.11
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Traverson {

	private URI baseUri;
	private RestOperations operations;
	private LinkDiscoverers discoverers;
	private List<MediaType> mediaTypes;

	public Traverson(RestOperations operations, LinkDiscoverers discoverers) {

		this.operations = operations;
		this.discoverers = discoverers;
	}

	/**
	 * Specify the URI to start from
	 * 
	 * @param uri
	 */
	public Traverson uri(URI uri) {
		return new Traverson(uri, this.operations, this.discoverers, this.mediaTypes);
	}

	/**
	 * Specify the accept header(s).
	 * 
	 * @param mediaTypes
	 */
	public Traverson accept(MediaType... mediaTypes) {
		return new Traverson(this.baseUri, this.operations, this.discoverers, Arrays.asList(mediaTypes));
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
	 * Builder API to customize traversals.
	 *
	 * @author Oliver Gierke
	 */
	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	public class TraversalBuilder {

		private final List<Hop> rels = new ArrayList<>();
		private Map<String, Object> templateParameters = new HashMap<>();
		private HttpHeaders headers = new HttpHeaders();

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
		 * @deprecated Migrate to {@link #as(Class)}.
		 */
		@Deprecated
		public <T> T toObject(Class<T> type) {
			return as(type);
		}

		/**
		 * Executes the traversal and marshals the final response into an object of the given type.
		 */
		public <T> T as(Class<T> type) {
			return toEntity(type).getBody();
		}

		/**
		 * Executes the traversal and marshals the final response into an object of the given
		 * {@link ParameterizedTypeReference}.
		 *
		 * @deprecated Migrate to {@link #as(ParameterizedTypeReference)}.
		 */
		@Deprecated
		public <T> T toObject(ParameterizedTypeReference<T> type) {
			return as(type);
		}

		/**
		 * Executes the traversal and marshals the final response into an object of the given
		 * {@link ParameterizedTypeReference}.
		 */
		public <T> T as(ParameterizedTypeReference<T> type) {
			return toEntity(type).getBody();
		}

		/**
		 * Executes the traversal and returns the result of the given JSON Path expression evaluated against the final
		 * representation.
		 *
		 * @deprecated Migrate to {@link #as(String)}.
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

			Assert.hasText(jsonPath, "JSON path must not be null or empty!");

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
		 * Returns the templated {@link Link} found for the last rel in the rels configured to follow.
		 *
		 * @return
		 * @since 0.17
		 */
		public Link asTemplatedLink() {
			return traverseToLink(false);
		}

		private Link traverseToLink(boolean expandFinalUrl) {

			Assert.isTrue(this.rels.size() > 0, "At least one rel needs to be provided!");

			URIAndHeaders expandedFinalUriAndHeaders = traverseToExpandedFinalUrl();
			UriStringAndHeaders finalUriAndHeaders = traverseToFinalUrl();

			return new Link(expandFinalUrl ? expandedFinalUriAndHeaders.getUri().toString() : finalUriAndHeaders.getUri(),
					this.rels.get(this.rels.size() - 1).getRel());
		}

		private UriStringAndHeaders traverseToFinalUrl() {

			UriStringAndHeaders uriAndHeaders = getAndFindLinkWithRel(baseUri.toString(), this.rels.iterator(),
					HttpHeaders.EMPTY);
			return new UriStringAndHeaders(new UriTemplate(uriAndHeaders.getUri()).toString(),
					uriAndHeaders.getHttpHeaders());
		}

		private URIAndHeaders traverseToExpandedFinalUrl() {

			UriStringAndHeaders uriAndHeaders = getAndFindLinkWithRel(baseUri.toString(), this.rels.iterator(),
					HttpHeaders.EMPTY);
			return new URIAndHeaders(new UriTemplate(uriAndHeaders.getUri()).expand(this.templateParameters),
					uriAndHeaders.getHttpHeaders());
		}

		private UriStringAndHeaders getAndFindLinkWithRel(String uri, Iterator<Hop> rels, HttpHeaders extraHeaders) {

			if (!rels.hasNext()) {
				return new UriStringAndHeaders(uri, extraHeaders);
			}

			HttpEntity<?> request = prepareRequest(mergeHeaders(this.headers, extraHeaders));
			UriTemplate template = new UriTemplate(uri);

			ResponseEntity<String> responseEntity = operations.exchange(template.expand(), GET, request, String.class);
			MediaType contentType = responseEntity.getHeaders().getContentType();
			String responseBody = responseEntity.getBody();

			Hop thisHop = rels.next();
			Rel rel = Rels.getRelFor(thisHop.getRel(), discoverers);

			Link link = rel.findInResponse(responseBody, contentType) //
					.orElseThrow(() -> new IllegalStateException(
							String.format("Expected to find link with rel '%s' in response %s!", rel, responseBody)));

			/*
			 * Don't expand if the parameters are empty
			 */
			if (!thisHop.hasParameters()) {
				return getAndFindLinkWithRel(link.getHref(), rels, thisHop.getHeaders());
			} else {
				return getAndFindLinkWithRel(link.expand(thisHop.getMergedParameters(this.templateParameters)).getHref(), rels,
						thisHop.getHeaders());
			}
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
	 * Temporary container for a string-base {@literal URI} and {@link HttpHeaders}.
	 */
	@Value
	@RequiredArgsConstructor
	private static class UriStringAndHeaders {

		private final String uri;
		private final HttpHeaders httpHeaders;
	}

	/**
	 * Temporary container for a {@link URI}-based {@literal URI} and {@link HttpHeaders}.
	 */
	@Value
	@RequiredArgsConstructor
	private static class URIAndHeaders {

		private final URI uri;
		private final HttpHeaders httpHeaders;
	}
}
