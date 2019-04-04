/*
 * Copyright 2014-2019 the original author or authors.
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
package org.springframework.hateoas.mediatype.uber;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.LinkDiscoverer;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Find links by rel in an {@literal UBER+JSON} representation. TODO: Pending
 * https://github.com/json-path/JsonPath/issues/429, replace deserializing solution with JsonPath-based expression
 * "$.uber.data[?(@.rel.indexOf('%s') != -1)].url"
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 * @since 1.0
 */
public class UberLinkDiscoverer implements LinkDiscoverer {

	private final ObjectMapper mapper;

	UberLinkDiscoverer() {

		this.mapper = new ObjectMapper();
		this.mapper.registerModules(new Jackson2UberModule());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkDiscoverer#findLinkWithRel(org.springframework.hateoas.LinkRelation, java.lang.String)
	 */
	@Override
	public Optional<Link> findLinkWithRel(LinkRelation rel, String representation) {

		return getLinks(representation).stream() //
				.filter(it -> it.hasRel(rel)) //
				.findFirst();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkDiscoverer#findLinkWithRel(org.springframework.hateoas.LinkRelation, java.io.InputStream)
	 */
	@Override
	public Optional<Link> findLinkWithRel(LinkRelation rel, InputStream representation) {

		return getLinks(representation).stream() //
				.filter(it -> it.hasRel(rel)) //
				.findFirst();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkDiscoverer#findLinksWithRel(org.springframework.hateoas.LinkRelation, java.lang.String)
	 */
	@Override
	public Links findLinksWithRel(LinkRelation rel, String representation) {

		return getLinks(representation).stream() //
				.filter(it -> it.hasRel(rel)) //
				.collect(Links.collector());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkDiscoverer#findLinksWithRel(org.springframework.hateoas.LinkRelation, java.io.InputStream)
	 */
	@Override
	public Links findLinksWithRel(LinkRelation rel, InputStream representation) {

		return getLinks(representation).stream() //
				.filter(it -> it.hasRel(rel)) //
				.collect(Links.collector());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.plugin.core.Plugin#supports(java.lang.Object)
	 */
	@Override
	public boolean supports(MediaType delimiter) {
		return delimiter.isCompatibleWith(MediaTypes.UBER_JSON);
	}

	/**
	 * Deserialize the entire document to find links.
	 *
	 * @param json
	 * @return
	 */
	private Links getLinks(String json) {

		try {
			return this.mapper.readValue(json, UberDocument.class).getUber().getLinks();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Deserialize the entire document to find links.
	 *
	 * @param stream
	 * @return
	 */
	private Links getLinks(InputStream stream) {

		try {
			return this.mapper.readValue(stream, UberDocument.class).getUber().getLinks();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
