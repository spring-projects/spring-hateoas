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

import static org.springframework.http.HttpMethod.*;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
 * @since 0.11
 */
public class Traverson {

	private final URI baseUri;
	private final RestTemplate template;
	private final LinkDiscoverers discoverers;
	private final List<MediaType> mediaTypes;

	/**
	 * Creates a new {@link Traverson} interacting with the given base URI and using the given {@link MediaType}s to
	 * interact with the service.
	 * 
	 * @param baseUri must not be {@literal null}.
	 * @param mediaType must not be {@literal null} or empty.
	 */
	public Traverson(URI baseUri, MediaType... mediaTypes) {

		Assert.notNull(baseUri, "Base URI must not be null!");
		Assert.notEmpty(mediaTypes, "At least one media must be given!");

		this.mediaTypes = Arrays.asList(mediaTypes);
		this.template = prepareTemplate(this.mediaTypes);

		this.baseUri = baseUri;

		LinkDiscoverer discoverer = new HalLinkDiscoverer();
		this.discoverers = new LinkDiscoverers(OrderAwarePluginRegistry.create(Arrays.asList(discoverer)));
	}

	private final RestTemplate prepareTemplate(List<MediaType> mediaTypes) {

		List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();
		converters.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));

		if (mediaTypes.contains(MediaTypes.HAL_JSON)) {
			converters.add(getHalConverter());
		}

		RestTemplate template = new RestTemplate();
		template.setMessageConverters(converters);
		return template;
	}

	private final HttpMessageConverter<?> getHalConverter() {

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Jackson2HalModule());
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();

		converter.setObjectMapper(mapper);
		converter.setSupportedMediaTypes(Arrays.asList(MediaTypes.HAL_JSON));

		return converter;
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

	private HttpEntity<?> prepareRequest(HttpHeaders headers) {

		HttpHeaders toSent = new HttpHeaders();
		toSent.putAll(headers);

		if (headers.getAccept().isEmpty()) {
			toSent.setAccept(mediaTypes);
		}

		return new HttpEntity<Void>(headers);
	}

	/**
	 * Builder API to customize traversals.
	 * 
	 * @author Oliver Gierke
	 */
	public class TraversalBuilder {

		private List<String> rels = new ArrayList<String>();
		private Map<String, Object> templateParameters = new HashMap<String, Object>();
		private HttpHeaders headers = new HttpHeaders();

		private TraversalBuilder() {}

		/**
		 * Follows the given rels one by one, which means a request per rel to discover the next resource with the rel in
		 * line.
		 * 
		 * @param rels must not be {@literal null}.
		 * @return
		 */
		private TraversalBuilder follow(String... rels) {

			Assert.notNull(rels, "Rels must not be null!");

			this.rels.addAll(Arrays.asList(rels));
			return this;
		}

		/**
		 * Adds the given template parameters to the traversal. If a link discovered by the traversal is templated, the
		 * given parameters will be used to expand the template into a resolvable URI.
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
			return template.exchange(traverseToFinalUrl(), GET, prepareRequest(headers), type).getBody();
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
			return template.exchange(traverseToFinalUrl(), GET, prepareRequest(headers), type).getBody();
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

			String forObject = template.getForObject(traverseToFinalUrl(), String.class);
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

			return template.getForEntity(traverseToFinalUrl(), type);
		}

		private String traverseToFinalUrl() {

			String uri = getAndFindLinkWithRel(baseUri.toString(), rels.iterator());
			return new UriTemplate(uri).expand(templateParameters).toString();
		}

		private String getAndFindLinkWithRel(String uri, Iterator<String> rels) {

			if (!rels.hasNext()) {
				return uri;
			}

			HttpEntity<?> request = prepareRequest(headers);
			UriTemplate uriTemplate = new UriTemplate(uri);

			ResponseEntity<String> responseEntity = template.exchange(uriTemplate.expand(templateParameters), GET, request,
					String.class);
			MediaType contentType = responseEntity.getHeaders().getContentType();
			String responseBody = responseEntity.getBody();

			Rel rel = Rels.getRelFor(rels.next(), discoverers);
			Link link = rel.findInResponse(responseBody, contentType);

			if (link == null) {
				throw new IllegalStateException(String.format("Expected to find link with rel '%s' in response %s!", rel,
						responseBody));
			}

			return getAndFindLinkWithRel(link.getHref(), rels);
		}
	}
}
