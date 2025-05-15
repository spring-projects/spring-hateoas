/*
 * Copyright 2018-2024 the original author or authors.
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
package org.springframework.hateoas.mediatype.collectionjson;

import static org.assertj.core.api.Assertions.*;

import lombok.Data;
import tools.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MappingTestUtils;
import org.springframework.hateoas.MappingTestUtils.ContextualMapper;
import org.springframework.hateoas.support.Employee;

/**
 * Unit tests leveraging spec fragments of JSON. NOTE: Fields that don't map into Java property names (e.g.
 * {@literal full-name}) are altered in the JSON to work properly. Alternative is to have some sort of injectable
 * converter.
 *
 * @author Greg Turnquist
 */
class CollectionJsonSpecTest {

	ContextualMapper contextual = MappingTestUtils.createMapper(getClass(),
			it -> it.addModule(new Jackson2CollectionJsonModule())
					.enable(SerializationFeature.INDENT_OUTPUT));

	/**
	 * @see http://amundsen.com/media-types/collection/examples/ - Section 1. Minimal Representation
	 * @throws IOException
	 */
	@Test
	void specPart1() throws IOException {

		var resource = contextual.readFile("spec-part1.json");

		assertThat(resource.getLinks()).hasSize(1);
		assertThat(resource.getRequiredLink(IanaLinkRelations.SELF)).isEqualTo(Link.of("https://example.org/friends/"));
	}

	/**
	 * @see http://amundsen.com/media-types/collection/examples/ - Section 2. Collection Representation
	 * @throws IOException
	 */
	@Test
	void specPart2() throws IOException {

		var resources = contextual.readEntityCollectionModel("spec-part2.json",
				Friend.class);

		assertThat(resources.getLinks()).hasSize(2);
		assertThat(resources.getRequiredLink(IanaLinkRelations.SELF)).isEqualTo(Link.of("https://example.org/friends/"));
		assertThat(resources.getRequiredLink("feed")).isEqualTo(Link.of("https://example.org/friends/rss", "feed"));
		assertThat(resources.getContent()).hasSize(3);

		var friends = new ArrayList<>(resources.getContent());

		assertThat(friends.get(0).getContent().getEmail()).isEqualTo("jdoe@example.org");
		assertThat(friends.get(0).getContent().getFullname()).isEqualTo("J. Doe");
		assertThat(friends.get(0).getRequiredLink(IanaLinkRelations.SELF))
				.isEqualTo(Link.of("https://example.org/friends/jdoe"));
		assertThat(friends.get(0).getRequiredLink("blog")).isEqualTo(Link.of("https://examples.org/blogs/jdoe", "blog"));
		assertThat(friends.get(0).getRequiredLink("avatar"))
				.isEqualTo(Link.of("https://examples.org/images/jdoe", "avatar"));

		assertThat(friends.get(1).getContent().getEmail()).isEqualTo("msmith@example.org");
		assertThat(friends.get(1).getContent().getFullname()).isEqualTo("M. Smith");
		assertThat(friends.get(1).getRequiredLink(IanaLinkRelations.SELF.value()))
				.isEqualTo(Link.of("https://example.org/friends/msmith"));
		assertThat(friends.get(1).getRequiredLink("blog")).isEqualTo(Link.of("https://examples.org/blogs/msmith", "blog"));
		assertThat(friends.get(1).getRequiredLink("avatar"))
				.isEqualTo(Link.of("https://examples.org/images/msmith", "avatar"));

		assertThat(friends.get(2).getContent().getEmail()).isEqualTo("rwilliams@example.org");
		assertThat(friends.get(2).getContent().getFullname()).isEqualTo("R. Williams");
		assertThat(friends.get(2).getRequiredLink(IanaLinkRelations.SELF.value()))
				.isEqualTo(Link.of("https://example.org/friends/rwilliams"));
		assertThat(friends.get(2).getRequiredLink("blog"))
				.isEqualTo(Link.of("https://examples.org/blogs/rwilliams", "blog"));
		assertThat(friends.get(2).getRequiredLink("avatar"))
				.isEqualTo(Link.of("https://examples.org/images/rwilliams", "avatar"));
	}

	/**
	 * @see http://amundsen.com/media-types/collection/examples/ - Section 3. Item Representation
	 * @throws IOException
	 */
	@Test
	void specPart3() throws IOException {

		var resource = contextual.readEntityModel("spec-part3.json", Friend.class);

		assertThat(resource.getLinks()).hasSize(6);
		assertThat(resource.getRequiredLink(IanaLinkRelations.SELF)).isEqualTo(Link.of("https://example.org/friends/jdoe"));
		assertThat(resource.getRequiredLink("feed")).isEqualTo(Link.of("https://example.org/friends/rss", "feed"));
		assertThat(resource.getRequiredLink("queries"))
				.isEqualTo(Link.of("https://example.org/friends/?queries", "queries"));
		assertThat(resource.getRequiredLink("template"))
				.isEqualTo(Link.of("https://example.org/friends/?template", "template"));
		assertThat(resource.getRequiredLink("blog")).isEqualTo(Link.of("https://examples.org/blogs/jdoe", "blog"));
		assertThat(resource.getRequiredLink("avatar")).isEqualTo(Link.of("https://examples.org/images/jdoe", "avatar"));

		assertThat(resource.getContent().getEmail()).isEqualTo("jdoe@example.org");
		assertThat(resource.getContent().getFullname()).isEqualTo("J. Doe");
	}

	/**
	 * @see http://amundsen.com/media-types/collection/examples/ - Section 4. Queries Representation
	 * @throws IOException
	 */
	@Test
	void specPart4() throws IOException {

		var resources = contextual.readEntityCollectionModel("spec-part4.json", Friend.class);

		assertThat(resources.getContent()).hasSize(0);
		assertThat(resources.getRequiredLink(IanaLinkRelations.SELF.value()))
				.isEqualTo(Link.of("https://example.org/friends/"));
	}

	/**
	 * @see http://amundsen.com/media-types/collection/examples/ - Section 5. Template Representation
	 * @throws IOException
	 */
	@Test
	void specPart5() throws IOException {

		var resources = contextual.readEntityCollectionModel("spec-part5.json", Friend.class);

		assertThat(resources.getContent()).hasSize(0);
		assertThat(resources.getRequiredLink(IanaLinkRelations.SELF.value()))
				.isEqualTo(Link.of("https://example.org/friends/"));
	}

	/**
	 * @see http://amundsen.com/media-types/collection/examples/ - Section 6. Error Representation
	 * @throws IOException
	 */
	@Test
	void specPart6() throws IOException {

		var resources = contextual.readEntityCollectionModel("spec-part6.json", Friend.class);

		assertThat(resources.getContent()).hasSize(0);
		assertThat(resources.getRequiredLink(IanaLinkRelations.SELF.value()))
				.isEqualTo(Link.of("https://example.org/friends/"));
	}

	/**
	 * @see http://amundsen.com/media-types/collection/examples/ - Section 7. Write Representation
	 * @throws IOException
	 */
	@Test
	void specPart7() throws IOException {

		var resource = contextual.readEntityModel("spec-part7-adjusted.json", Employee.class);

		assertThat(resource.getContent()).isEqualTo(new Employee("W. Chandry", "developer"));
		assertThat(resource.getLinks()).isEmpty();
	}

	@Data
	static class Friend {

		private String fullname;
		private String email;
	}
}
