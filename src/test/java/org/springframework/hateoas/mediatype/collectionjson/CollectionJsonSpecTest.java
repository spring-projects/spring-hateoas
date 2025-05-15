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

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MappingTestUtils;
import org.springframework.hateoas.MappingTestUtils.ContextualMapper;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.support.Employee;

/**
 * Unit tests leveraging spec fragments of JSON. NOTE: Fields that don't map into Java property names (e.g.
 * {@literal full-name}) are altered in the JSON to work properly. Alternative is to have some sort of injectable
 * converter.
 *
 * @author Greg Turnquist
 */
class CollectionJsonSpecTest {

	ContextualMapper $ = MappingTestUtils.createMapper(it -> it.addModule(new CollectionJsonJacksonModule())
			.enable(SerializationFeature.INDENT_OUTPUT));

	/**
	 * @see http://amundsen.com/media-types/collection/examples/ - Section 1. Minimal Representation
	 * @throws IOException
	 */
	@Test
	void specPart1() throws IOException {

		$.assertDeserializesFile("spec-part1.json")
				.into(RepresentationModel.class)
				.matching(model -> {
					assertThat(model.getLinks()).hasSize(1);
					assertThat(model.getRequiredLink(IanaLinkRelations.SELF))
							.isEqualTo(Link.of("https://example.org/friends/"));
				});
	}

	/**
	 * @see http://amundsen.com/media-types/collection/examples/ - Section 2. Collection Representation
	 * @throws IOException
	 */
	@Test
	void specPart2() throws IOException {

		$.assertDeserializesFile("spec-part2.json")
				.intoCollectionEntityModel(Friend.class)
				.matching(resources -> {

					assertThat(resources.getLinks()).hasSize(2);
					assertThat(resources.getRequiredLink(IanaLinkRelations.SELF))
							.isEqualTo(Link.of("https://example.org/friends/"));
					assertThat(resources.getRequiredLink("feed")).isEqualTo(Link.of("https://example.org/friends/rss", "feed"));
					assertThat(resources.getContent()).hasSize(3);

					var friends = assertThat(resources.getContent());

					friends.element(0).satisfies(friend -> {

						assertThat(friend.getContent().getEmail()).isEqualTo("jdoe@example.org");
						assertThat(friend.getContent().getFullname()).isEqualTo("J. Doe");
						assertThat(friend.getRequiredLink(IanaLinkRelations.SELF))
								.isEqualTo(Link.of("https://example.org/friends/jdoe"));
						assertThat(friend.getRequiredLink("blog"))
								.isEqualTo(Link.of("https://examples.org/blogs/jdoe", "blog"));
						assertThat(friend.getRequiredLink("avatar"))
								.isEqualTo(Link.of("https://examples.org/images/jdoe", "avatar"));
					});

					friends.element(1).satisfies(friend -> {

						assertThat(friend.getContent().getEmail()).isEqualTo("msmith@example.org");
						assertThat(friend.getContent().getFullname()).isEqualTo("M. Smith");
						assertThat(friend.getRequiredLink(IanaLinkRelations.SELF.value()))
								.isEqualTo(Link.of("https://example.org/friends/msmith"));
						assertThat(friend.getRequiredLink("blog"))
								.isEqualTo(Link.of("https://examples.org/blogs/msmith", "blog"));
						assertThat(friend.getRequiredLink("avatar"))
								.isEqualTo(Link.of("https://examples.org/images/msmith", "avatar"));
					});

					friends.element(2).satisfies(friend -> {

						assertThat(friend.getContent().getEmail()).isEqualTo("rwilliams@example.org");
						assertThat(friend.getContent().getFullname()).isEqualTo("R. Williams");
						assertThat(friend.getRequiredLink(IanaLinkRelations.SELF.value()))
								.isEqualTo(Link.of("https://example.org/friends/rwilliams"));
						assertThat(friend.getRequiredLink("blog"))
								.isEqualTo(Link.of("https://examples.org/blogs/rwilliams", "blog"));
						assertThat(friend.getRequiredLink("avatar"))
								.isEqualTo(Link.of("https://examples.org/images/rwilliams", "avatar"));
					});
				});
	}

	/**
	 * @see http://amundsen.com/media-types/collection/examples/ - Section 3. Item Representation
	 * @throws IOException
	 */
	@Test
	void specPart3() throws IOException {

		$.assertDeserializesFile("spec-part3.json")
				.intoEntityModel(Friend.class)
				.matching(resource -> {

					assertThat(resource.getLinks()).hasSize(6);
					assertThat(resource.getRequiredLink(IanaLinkRelations.SELF))
							.isEqualTo(Link.of("https://example.org/friends/jdoe"));
					assertThat(resource.getRequiredLink("feed")).isEqualTo(Link.of("https://example.org/friends/rss", "feed"));
					assertThat(resource.getRequiredLink("queries"))
							.isEqualTo(Link.of("https://example.org/friends/?queries", "queries"));
					assertThat(resource.getRequiredLink("template"))
							.isEqualTo(Link.of("https://example.org/friends/?template", "template"));
					assertThat(resource.getRequiredLink("blog")).isEqualTo(Link.of("https://examples.org/blogs/jdoe", "blog"));
					assertThat(resource.getRequiredLink("avatar"))
							.isEqualTo(Link.of("https://examples.org/images/jdoe", "avatar"));

					assertThat(resource.getContent().getEmail()).isEqualTo("jdoe@example.org");
					assertThat(resource.getContent().getFullname()).isEqualTo("J. Doe");
				});
	}

	/**
	 * @see http://amundsen.com/media-types/collection/examples/ - Section 4. Queries Representation
	 * @throws IOException
	 */
	@Test
	void specPart4() throws IOException {

		$.assertDeserializesFile("spec-part4.json")
				.intoCollectionEntityModel(Friend.class)
				.matching(resources -> {

					assertThat(resources.getContent()).hasSize(0);
					assertThat(resources.getRequiredLink(IanaLinkRelations.SELF.value()))
							.isEqualTo(Link.of("https://example.org/friends/"));
				});
	}

	/**
	 * @see http://amundsen.com/media-types/collection/examples/ - Section 5. Template Representation
	 * @throws IOException
	 */
	@Test
	void specPart5() throws IOException {

		$.assertDeserializesFile("spec-part5.json")
				.intoCollectionEntityModel(Friend.class)
				.matching(model -> {

					assertThat(model.getContent()).hasSize(0);
					assertThat(model.getRequiredLink(IanaLinkRelations.SELF.value()))
							.isEqualTo(Link.of("https://example.org/friends/"));
				});
	}

	/**
	 * @see http://amundsen.com/media-types/collection/examples/ - Section 6. Error Representation
	 * @throws IOException
	 */
	@Test
	void specPart6() throws IOException {

		$.assertDeserializesFile("spec-part6.json")
				.intoCollectionEntityModel(Friend.class)
				.matching(model -> {

					assertThat(model.getContent()).hasSize(0);
					assertThat(model.getRequiredLink(IanaLinkRelations.SELF.value()))
							.isEqualTo(Link.of("https://example.org/friends/"));
				});
	}

	/**
	 * @see http://amundsen.com/media-types/collection/examples/ - Section 7. Write Representation
	 * @throws IOException
	 */
	@Test
	void specPart7() throws IOException {

		$.assertDeserializesFile("spec-part7-adjusted.json")
				.intoEntityModel(Employee.class)
				.matching(model -> {

					assertThat(model.getContent()).isEqualTo(new Employee("W. Chandry", "developer"));
					assertThat(model.getLinks()).isEmpty();
				});
	}

	@Data
	static class Friend {

		private String fullname;
		private String email;
	}
}
