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
package org.springframework.hateoas.server.mvc;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.core.HeaderLinksResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for {@link HeaderLinksResponseEntity}.
 * 
 * @author Oliver Gierke
 */
class HeaderLinksResponseEntityUnitTest {

	static final Object CONTENT = new Object();
	static final Link LINK = new Link("href", "rel");

	EntityModel<Object> resource = new EntityModel<>(CONTENT, LINK);
	ResponseEntity<EntityModel<Object>> entity = new ResponseEntity<>(resource, HttpStatus.OK);

	@Test
	void movesRootResourceLinksToHeader() {

		HttpEntity<EntityModel<Object>> wrapper = HeaderLinksResponseEntity.wrap(entity);

		// No links in resource anymore
		assertThat(wrapper.getBody().getLinks()).isEmpty();

		// Link found in header
		List<String> linkHeader = wrapper.getHeaders().get("Link");
		assertThat(linkHeader).hasSize(1);

		Link link = Link.valueOf(linkHeader.get(0));
		assertThat(link).isEqualTo(LINK);
	}

	@Test
	void defaultStatusCodeToOkForHttpEntities() {

		HttpEntity<EntityModel<Object>> entity = new HttpEntity<>(resource);
		ResponseEntity<EntityModel<Object>> wrappedEntity = HeaderLinksResponseEntity.wrap(entity);

		assertThat(wrappedEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
}
