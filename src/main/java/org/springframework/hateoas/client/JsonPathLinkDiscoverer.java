/*
 * Copyright 2012-2021 the original author or authors.
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

import net.minidev.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.Links;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

/**
 * {@link LinkDiscoverer} that uses {@link JsonPath} to find links inside a representation.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
public class JsonPathLinkDiscoverer implements LinkDiscoverer {

	private final String pathTemplate;
	private final List<MediaType> mediaTypes;

	/**
	 * Creates a new {@link JsonPathLinkDiscoverer} using the given path template supporting the given {@link MediaType}.
	 * The template has to contain a single {@code %s} placeholder which will be replaced by the relation type.
	 *
	 * @param pathTemplate must not be {@literal null} or empty and contain a single placeholder.
	 * @param mediaTypes the {@link MediaType}s to support.
	 */
	public JsonPathLinkDiscoverer(String pathTemplate, MediaType... mediaTypes) {

		Assert.hasText(pathTemplate, "Path template must not be null!");
		Assert.notNull(mediaTypes, "Primary MediaType must not be null!");

		this.pathTemplate = pathTemplate;
		this.mediaTypes = Arrays.asList(mediaTypes);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkDiscoverer#findLinkWithRel(org.springframework.hateoas.LinkRelation, java.lang.String)
	 */
	@Override
	public Optional<Link> findLinkWithRel(LinkRelation relation, String representation) {
		return firstOrEmpty(findLinksWithRel(relation, representation));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkDiscoverer#findLinkWithRel(org.springframework.hateoas.LinkRelation, java.io.InputStream)
	 */
	@Override
	public Optional<Link> findLinkWithRel(LinkRelation relation, InputStream representation) {
		return firstOrEmpty(findLinksWithRel(relation, representation));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkDiscoverer#findLinksWithRel(org.springframework.hateoas.LinkRelation, java.lang.String)
	 */
	@Override
	public Links findLinksWithRel(LinkRelation relation, String representation) {

		Assert.notNull(relation, "LinkRelation must not be null!");

		try {
			Object parseResult = getExpression(relation).read(representation);
			return createLinksFrom(parseResult, relation);
		} catch (InvalidPathException e) {
			return Links.NONE;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkDiscoverer#findLinksWithRel(org.springframework.hateoas.LinkRelation, java.io.InputStream)
	 */
	@Override
	public Links findLinksWithRel(LinkRelation relation, InputStream representation) {

		Assert.notNull(relation, "LinkRelation must not be null!");

		try {

			Object parseResult = getExpression(relation).read(representation);
			return createLinksFrom(parseResult, relation);

		} catch (IOException o_O) {
			throw new RuntimeException(o_O);
		} catch (PathNotFoundException o_O) {
			return Links.NONE;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.plugin.core.Plugin#supports(java.lang.Object)
	 */
	@Override
	public boolean supports(@NonNull MediaType delimiter) {

		return this.mediaTypes.stream() //
				.anyMatch(mediaType -> mediaType.isCompatibleWith(delimiter));
	}

	/**
	 * Callback for each {@link LinkDiscoverer} to extract relevant attributes and generate a {@link Link}.
	 *
	 * @param element
	 * @param rel
	 * @return link
	 */
	protected Link extractLink(Object element, LinkRelation rel) {
		return Link.of(element.toString(), rel);
	}

	/**
	 * Returns the {@link JsonPath} to find links with the given relation type.
	 *
	 * @param rel
	 * @return
	 */
	private JsonPath getExpression(LinkRelation rel) {
		return JsonPath.compile(String.format(pathTemplate, rel.value()));
	}

	/**
	 * Creates {@link Link} instances from the given parse result.
	 *
	 * @param parseResult the result originating from parsing the source content using the JSON path expression.
	 * @param rel the relation type that was parsed for.
	 * @return
	 */
	private Links createLinksFrom(Object parseResult, LinkRelation rel) {

		if (JSONArray.class.isInstance(parseResult)) {

			JSONArray jsonArray = (JSONArray) parseResult;

			return jsonArray.stream() //
					.flatMap(it -> JSONArray.class.isInstance(it) ? ((JSONArray) it).stream() : Stream.of(it)) //
					.map(it -> extractLink(it, rel)) //
					.collect(Collectors.collectingAndThen(Collectors.toList(), Links::of));
		}

		return Links.of(Map.class.isInstance(parseResult) //
				? extractLink(parseResult, rel) //
				: Link.of(parseResult.toString(), rel));
	}

	private static <T> Optional<T> firstOrEmpty(Iterable<T> source) {

		Iterator<T> iterator = source.iterator();

		return iterator.hasNext() ? Optional.of(iterator.next()) : Optional.empty();
	}
}
