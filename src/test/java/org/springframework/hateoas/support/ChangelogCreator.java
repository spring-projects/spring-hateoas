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

import net.minidev.json.JSONArray;

import java.util.Iterator;

import org.springframework.web.client.RestTemplate;

import com.jayway.jsonpath.JsonPath;

/**
 * Little helper to build a changelog from the tickets of a particular milestone.
 * 
 * @author Oliver Gierke
 */
class ChangelogCreator {

	private static final int MILESTONE_ID = 23;
	private static final String URI_TEMPLATE = "https://api.github.com/repos/spring-projects/spring-hateoas/issues?milestone={id}&state=closed";

	public static void main(String... args) throws Exception {

		RestTemplate template = new RestTemplate();
		String response = template.getForObject(URI_TEMPLATE, String.class, MILESTONE_ID);

		JsonPath titlePath = JsonPath.compile("$[*].title");
		JsonPath idPath = JsonPath.compile("$[*].number");

		JSONArray titles = titlePath.read(response);
		Iterator<Object> ids = ((JSONArray) idPath.read(response)).iterator();

		System.out.println("Milestone - " + JsonPath.read(response, "$[1].milestone.title"));

		for (Object title : titles) {

			String format = String.format("- #%s - %s", ids.next(), title);
			System.out.println(format.endsWith(".") ? format : format.concat("."));
		}
	}
}
