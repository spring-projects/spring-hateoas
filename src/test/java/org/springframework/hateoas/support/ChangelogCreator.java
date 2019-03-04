/*
 * Copyright 2013-2016 the original author or authors.
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
package org.springframework.hateoas.support;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;

import net.minidev.json.JSONArray;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import com.jayway.jsonpath.JsonPath;

/**
 * Little helper to build a changelog from the tickets of a particular milestone.
 * 
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
class ChangelogCreator {

	private static final int MILESTONE_ID = 25;
	private static final String URI_TEMPLATE = "https://api.github.com/repos/spring-projects/spring-hateoas/issues?milestone={id}&state=closed";

	public static void main(String... args) {

		RestTemplate template = new RestTemplate();

		HttpEntity<String> response = template.getForEntity(URI_TEMPLATE, String.class, MILESTONE_ID);

		boolean keepChecking = true;
		boolean printHeader = true;

		while (keepChecking) {

			readPage(response.getBody(), printHeader);
			printHeader = false;

			String linkHeader = response.getHeaders().get("Link").get(0);

			Links links = Arrays.stream(linkHeader.split(", ")) //
					.map(Link::valueOf) //
					.collect(Collectors.collectingAndThen(Collectors.toList(), Links::of));

			if (links.getLink(IanaLinkRelations.NEXT).isPresent()) {

				response = template.getForEntity(links.getRequiredLink(IanaLinkRelations.NEXT).expand().getHref(),
						String.class);

			} else {
				keepChecking = false;
			}
		}
	}

	private static void readPage(String content, boolean header) {

		JsonPath titlePath = JsonPath.compile("$[*].title");
		JsonPath idPath = JsonPath.compile("$[*].number");

		JSONArray titles = titlePath.read(content);
		Iterator<Object> ids = ((JSONArray) idPath.read(content)).iterator();

		if (header) {
			System.out.println("Milestone - " + JsonPath.read(content, "$[1].milestone.title"));
		}

		for (Object title : titles) {

			String format = String.format("- #%s - %s", ids.next(), title);
			System.out.println(format.endsWith(".") ? format : format.concat("."));
		}
	}
}
