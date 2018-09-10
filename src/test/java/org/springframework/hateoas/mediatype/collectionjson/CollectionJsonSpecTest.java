/*
 * Copyright 2018 the original author or authors.
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
package org.springframework.hateoas.mediatype.collectionjson;

import static org.assertj.core.api.Assertions.*;

import lombok.Data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.support.MappingUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Unit tests leveraging spec fragments of JSON.
 *
 * NOTE: Fields that don't map into Java property names (e.g. {@literal full-name}) are altered in the JSON to work properly.
 * Alternative is to have some sort of injectable converter.
 * 
 * @author Greg Turnquist
 */
public class CollectionJsonSpecTest {

	ObjectMapper mapper;

	@Before
	public void setUp() {

		mapper = new ObjectMapper();
		mapper.registerModule(new Jackson2CollectionJsonModule());
		mapper.setHandlerInstantiator(new Jackson2CollectionJsonModule.CollectionJsonHandlerInstantiator(null));
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
	}

	/**
	 * @see http://amundsen.com/media-types/collection/examples/ - Section 1. Minimal Representation
	 * @throws IOException
	 */
	@Test
	public void specPart1() throws IOException {

		String specBasedJson = MappingUtils.read(new ClassPathResource("spec-part1.json", getClass()));

		ResourceSupport resource = mapper.readValue(specBasedJson, ResourceSupport.class);

		assertThat(resource.getLinks()).hasSize(1);
		assertThat(resource.getRequiredLink(Link.REL_SELF)).isEqualTo(new Link("http://example.org/friends/"));
	}

	/**
	 * @see http://amundsen.com/media-types/collection/examples/ - Section 2. Collection Representation
	 * @throws IOException
	 */
	@Test
	public void specPart2() throws IOException {
		
		String specBasedJson = MappingUtils.read(new ClassPathResource("spec-part2.json", getClass()));

		Resources<Resource<Friend>> resources = mapper.readValue(specBasedJson,
			mapper.getTypeFactory().constructParametricType(Resources.class,
				mapper.getTypeFactory().constructParametricType(Resource.class, Friend.class)));

		assertThat(resources.getLinks()).hasSize(2);
		assertThat(resources.getRequiredLink(Link.REL_SELF)).isEqualTo(new Link("http://example.org/friends/"));
		assertThat(resources.getRequiredLink("feed")).isEqualTo(new Link("http://example.org/friends/rss", "feed"));
		assertThat(resources.getContent()).hasSize(3);

		List<Resource<Friend>> friends = new ArrayList<>(resources.getContent());

		assertThat(friends.get(0).getContent().getEmail()).isEqualTo("jdoe@example.org");
		assertThat(friends.get(0).getContent().getFullname()).isEqualTo("J. Doe");
		assertThat(friends.get(0).getRequiredLink(Link.REL_SELF)).isEqualTo(new Link("http://example.org/friends/jdoe"));
		assertThat(friends.get(0).getRequiredLink("blog")).isEqualTo(new Link("http://examples.org/blogs/jdoe", "blog"));
		assertThat(friends.get(0).getRequiredLink("avatar")).isEqualTo(new Link("http://examples.org/images/jdoe", "avatar"));

		assertThat(friends.get(1).getContent().getEmail()).isEqualTo("msmith@example.org");
		assertThat(friends.get(1).getContent().getFullname()).isEqualTo("M. Smith");
		assertThat(friends.get(1).getRequiredLink(Link.REL_SELF)).isEqualTo(new Link("http://example.org/friends/msmith"));
		assertThat(friends.get(1).getRequiredLink("blog")).isEqualTo(new Link("http://examples.org/blogs/msmith", "blog"));
		assertThat(friends.get(1).getRequiredLink("avatar")).isEqualTo(new Link("http://examples.org/images/msmith", "avatar"));

		assertThat(friends.get(2).getContent().getEmail()).isEqualTo("rwilliams@example.org");
		assertThat(friends.get(2).getContent().getFullname()).isEqualTo("R. Williams");
		assertThat(friends.get(2).getRequiredLink(Link.REL_SELF)).isEqualTo(new Link("http://example.org/friends/rwilliams"));
		assertThat(friends.get(2).getRequiredLink("blog")).isEqualTo(new Link("http://examples.org/blogs/rwilliams", "blog"));
		assertThat(friends.get(2).getRequiredLink("avatar")).isEqualTo(new Link("http://examples.org/images/rwilliams", "avatar"));
	}

	/**
	 * @see http://amundsen.com/media-types/collection/examples/ - Section 3. Item Representation
	 * @throws IOException
	 */
	@Test
	public void specPart3() throws IOException {

		String specBasedJson = MappingUtils.read(new ClassPathResource("spec-part3.json", getClass()));

		Resource<Friend> resource = mapper.readValue(specBasedJson,
				mapper.getTypeFactory().constructParametricType(Resource.class, Friend.class));

		assertThat(resource.getLinks()).hasSize(6);
		assertThat(resource.getRequiredLink(Link.REL_SELF)).isEqualTo(new Link("http://example.org/friends/jdoe"));
		assertThat(resource.getRequiredLink("feed")).isEqualTo(new Link("http://example.org/friends/rss", "feed"));
		assertThat(resource.getRequiredLink("queries")).isEqualTo(new Link("http://example.org/friends/?queries", "queries"));
		assertThat(resource.getRequiredLink("template")).isEqualTo(new Link("http://example.org/friends/?template", "template"));
		assertThat(resource.getRequiredLink("blog")).isEqualTo(new Link("http://examples.org/blogs/jdoe", "blog"));
		assertThat(resource.getRequiredLink("avatar")).isEqualTo(new Link("http://examples.org/images/jdoe", "avatar"));

		assertThat(resource.getContent().getEmail()).isEqualTo("jdoe@example.org");
		assertThat(resource.getContent().getFullname()).isEqualTo("J. Doe");
	}

	/**
	 * @see http://amundsen.com/media-types/collection/examples/ - Section 4. Queries Representation
	 * @throws IOException
	 */
	@Test
	public void specPart4() throws IOException {

		String specBasedJson = MappingUtils.read(new ClassPathResource("spec-part4.json", getClass()));

		Resources<Resource<Friend>> resources = mapper.readValue(specBasedJson,
			mapper.getTypeFactory().constructParametricType(Resources.class,
				mapper.getTypeFactory().constructParametricType(Resource.class, Friend.class)));

		assertThat(resources.getContent()).hasSize(0);
		assertThat(resources.getRequiredLink(Link.REL_SELF)).isEqualTo(new Link("http://example.org/friends/"));
	}
	/**
	 * @see http://amundsen.com/media-types/collection/examples/ - Section 5. Template Representation
	 * @throws IOException
	 */
	@Test
	public void specPart5() throws IOException {

		String specBasedJson = MappingUtils.read(new ClassPathResource("spec-part5.json", getClass()));

		Resources<Resource<Friend>> resources = mapper.readValue(specBasedJson,
			mapper.getTypeFactory().constructParametricType(Resources.class,
				mapper.getTypeFactory().constructParametricType(Resource.class, Friend.class)));
		
		assertThat(resources.getContent()).hasSize(0);
		assertThat(resources.getRequiredLink(Link.REL_SELF)).isEqualTo(new Link("http://example.org/friends/"));
	}
	/**
	 * @see http://amundsen.com/media-types/collection/examples/ - Section 6. Error Representation
	 * @throws IOException
	 */
	@Test
	public void specPart6() throws IOException {

		String specBasedJson = MappingUtils.read(new ClassPathResource("spec-part6.json", getClass()));

		Resources<Resource<Friend>> resources = mapper.readValue(specBasedJson,
			mapper.getTypeFactory().constructParametricType(Resources.class,
				mapper.getTypeFactory().constructParametricType(Resource.class, Friend.class)));

		assertThat(resources.getContent()).hasSize(0);
		assertThat(resources.getRequiredLink(Link.REL_SELF)).isEqualTo(new Link("http://example.org/friends/"));
	}
	/**
	 * @see http://amundsen.com/media-types/collection/examples/ - Section 7. Write Representation
	 * @throws IOException
	 */
	@Test
	public void specPart7() throws IOException {

		String specBasedJson = MappingUtils.read(new ClassPathResource("spec-part7.json", getClass()));

		// TODO: Come up with a way to verify this JSON spec can be used to create a new resource.
	}

	@Data
	static class Friend {

		private String fullname;
		private String email;
	}
}
