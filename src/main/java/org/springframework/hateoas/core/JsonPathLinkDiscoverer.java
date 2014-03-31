/*
 * Copyright 2012-2014 the original author or authors.
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
package org.springframework.hateoas.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minidev.json.JSONArray;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkDiscoverer;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;

/**
 * {@link LinkDiscoverer} that uses {@link JsonPath} to find links inside a representation.
 * 
 * @author Oliver Gierke
 */
public class JsonPathLinkDiscoverer implements LinkDiscoverer {

	private final String pathTemplate;
	private final MediaType mediaType;

	/**
	 * Creates a new {@link JsonPathLinkDiscoverer} using the given path template supporting the given {@link MediaType}.
	 * The template has to contain a single {@code %s} placeholder which will be replaced by the relation type.
	 * 
	 * @param pathTemplate must not be {@literal null} or empty and contain a single placeholder.
	 * @param mediaType the {@link MediaType} to support.
	 */
	public JsonPathLinkDiscoverer(String pathTemplate, MediaType mediaType) {

		Assert.hasText(pathTemplate, "Path template must not be null!");
		Assert.isTrue(StringUtils.countOccurrencesOf(pathTemplate, "%s") == 1,
				"Path template must contain a single placeholder!");

		this.pathTemplate = pathTemplate;
		this.mediaType = mediaType;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkDiscoverer#findLinkWithRel(java.lang.String, java.lang.String)
	 */
	@Override
	public Link findLinkWithRel(String rel, String representation) {

		List<Link> links = findLinksWithRel(rel, representation);
		return links.isEmpty() ? null : links.get(0);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkDiscoverer#findLinkWithRel(java.lang.String, java.io.InputStream)
	 */
	@Override
	public Link findLinkWithRel(String rel, InputStream representation) {

		List<Link> links = findLinksWithRel(rel, representation);
		return links.isEmpty() ? null : links.get(0);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkDiscoverer#findLinksWithRel(java.lang.String, java.lang.String)
	 */
	@Override
	public List<Link> findLinksWithRel(String rel, String representation) {

		try {
			Object parseResult = getExpression(rel).read(representation);
			return createLinksFrom(parseResult, rel);
		} catch (InvalidPathException e) {
			return Collections.emptyList();
		}
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkDiscoverer#findLinksWithRel(java.lang.String, java.io.InputStream)
	 */
	@Override
	public List<Link> findLinksWithRel(String rel, InputStream representation) {

		try {
			Object parseResult = getExpression(rel).read(representation);
			return createLinksFrom(parseResult, rel);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the {@link JsonPath} to find links with the given relation type.
	 * 
	 * @param rel
	 * @return
	 */
	private JsonPath getExpression(String rel) {
		return JsonPath.compile(String.format(pathTemplate, rel));
	}

	/**
	 * Creates {@link Link} instances from the given parse result.
	 * 
	 * @param parseResult the result originating from parsing the source content using the JSON path expression.
	 * @param rel the relation type that was parsed for.
	 * @return
	 */
	private List<Link> createLinksFrom(Object parseResult, String rel) {

		if (parseResult instanceof JSONArray) {

			List<Link> links = new ArrayList<Link>();
			JSONArray array = (JSONArray) parseResult;

			for (Object element : array) {
				links.add(new Link(element.toString(), rel));
			}

			return Collections.unmodifiableList(links);
		}

		return Collections.unmodifiableList(Arrays.asList(new Link(parseResult.toString(), rel)));
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.plugin.core.Plugin#supports(java.lang.Object)
	 */
	@Override
	public boolean supports(MediaType delimiter) {
		return this.mediaType == null ? true : this.mediaType.isCompatibleWith(delimiter);
	}
}
