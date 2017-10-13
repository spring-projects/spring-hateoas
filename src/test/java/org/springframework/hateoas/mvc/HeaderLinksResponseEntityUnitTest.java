/*
 * Copyright 2013 the original author or authors.
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
package org.springframework.hateoas.mvc;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.Test;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for {@link HeaderLinksResponseEntity}.
 * 
 * @author Oliver Gierke
 */
public class HeaderLinksResponseEntityUnitTest {

	static final Object CONTENT = new Object();
	static final Link LINK = new Link("href", "rel");

	Resource<Object> resource = new Resource<Object>(CONTENT, LINK);
	ResponseEntity<Resource<Object>> entity = new ResponseEntity<Resource<Object>>(resource, HttpStatus.OK);

	@Test
	public void movesRootResourceLinksToHeader() {

		HttpEntity<Resource<Object>> wrapper = HeaderLinksResponseEntity.wrap(entity);

		// No links in resource anymore
		assertThat(wrapper.getBody().getLinks()).isEmpty();

		// Link found in header
		List<String> linkHeader = wrapper.getHeaders().get("Link");
		assertThat(linkHeader).hasSize(1);

		Link link = Link.valueOf(linkHeader.get(0));
		assertThat(link).isEqualTo(LINK);
	}

	@Test
	public void defaultStatusCodeToOkForHttpEntities() {

		HttpEntity<Resource<Object>> entity = new HttpEntity<Resource<Object>>(resource);
		ResponseEntity<Resource<Object>> wrappedEntity = HeaderLinksResponseEntity.wrap(entity);

		assertThat(wrappedEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
}
