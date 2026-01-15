/*
 * Copyright 2020-2026 the original author or authors.
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
package org.springframework.hateoas.mediatype.hal;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.hateoas.IanaLinkRelations.*;
import static org.springframework.hateoas.MappingTestUtils.*;
import static org.springframework.hateoas.mediatype.hal.HalModelBuilder.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Value;
import net.minidev.json.JSONArray;
import tools.jackson.databind.SerializationFeature;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.MappingTestUtils;
import org.springframework.hateoas.MappingTestUtils.ContextualMapper;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.server.core.EvoInflectorLinkRelationProvider;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

/**
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
public class HalModelBuilderUnitTest {

	private static final Link JOHN_SMITH_SELF = Link.of("/people/john-smith");
	private static final Link ALAN_WATTS_SELF = Link.of("/people/alan-watts");

	private static final LinkRelation ILLUSTRATOR_REL = LinkRelation.of("illustrator");
	private static final LinkRelation AUTHOR_REL = LinkRelation.of("author");

	private ContextualMapper $;

	@BeforeEach
	void setUp() {

		var instantiator = new HalJacksonModule.HalHandlerInstantiator(
				new EvoInflectorLinkRelationProvider(), CurieProvider.NONE, MessageResolver.DEFAULTS_ONLY);

		this.$ = createMapper(it -> it.addModule(new HalJacksonModule())
				.enable(SerializationFeature.INDENT_OUTPUT)
				.handlerInstantiator(instantiator));
	}

	@Test // #864
	void embeddedSpecUsingHalModelBuilder() throws Exception {

		var model = halModel() //
				.embed(halModel() //
						.entity(new Author("Alan Watts", "January 6, 1915", "November 16, 1973")) //
						.link(ALAN_WATTS_SELF) //
						.build(), AUTHOR_REL)
				.embed(halModel() //
						.entity(new Author("John Smith", null, null)) //
						.link(JOHN_SMITH_SELF) //
						.build(), ILLUSTRATOR_REL)
				.link(Link.of("/books/the-way-of-zen")) //
				.link(Link.of("/people/alan-watts", AUTHOR_REL)) //
				.link(Link.of("/people/john-smith", ILLUSTRATOR_REL)) //
				.build();

		$.assertSerializes(model)
				.intoContentOf("hal-embedded-author-illustrator.json");
	}

	@Test // #864
	void previewForLinkRelationsUsingHalModelBuilder() throws Exception {

		var model = halModel() //
				.link("/books/the-way-of-zen", IanaLinkRelations.SELF) //
				.preview(halModel() //
						.entity(new Author("Alan Watts", "January 6, 1915", "November 16, 1973")) //
						.link(ALAN_WATTS_SELF) //
						.build())
				.forLink(Link.of("/people/alan-watts", AUTHOR_REL)) //
				.preview(halModel() //
						.entity(new Author("John Smith", null, null)) //
						.link(JOHN_SMITH_SELF) //
						.build()) //
				.forLink(Link.of("/people/john-smith", ILLUSTRATOR_REL)).build();

		$.assertSerializes(model)
				.intoContentOf("hal-embedded-author-illustrator.json");
	}

	@Test // #864
	void renderSingleItemUsingHalModelBuilder() throws Exception {

		var model = halModel() //
				.entity(new Author("Alan Watts", "January 6, 1915", "November 16, 1973")) //
				.link(ALAN_WATTS_SELF) //
				.build();

		$.assertSerializes(model)
				.intoContentOf("hal-single-item.json");
	}

	@Test // #864
	void renderSingleItemUsingDefaultModelBuilder() throws Exception {

		var model = halModel()//
				.entity(new Author("Alan Watts", "January 6, 1915", "November 16, 1973")) //
				.link(ALAN_WATTS_SELF) //
				.build();

		$.assertSerializes(model)
				.intoContentOf("hal-single-item.json");
	}

	@Test // #864
	void renderCollectionUsingDefaultModelBuilder() throws Exception {

		Link authorsLink = Link.of("http://localhost/authors", LinkRelation.of("authors"));

		var model = halModel() //
				.embed( //
						halModel() //
								.entity(new Author("Greg L. Turnquist", null, null)) //
								.link(Link.of("http://localhost/author/1")) //
								.link(authorsLink) //
								.build())
				.embed( //
						halModel() //
								.entity(new Author("Craig Walls", null, null)) //
								.link(Link.of("http://localhost/author/2")) //
								.link(authorsLink) //
								.build())
				.embed( //
						halModel() //
								.entity(new Author("Oliver Drotbohm", null, null)) //
								.link(Link.of("http://localhost/author/3")) //
								.link(authorsLink) //
								.build())
				.link(Link.of("http://localhost/authors")) //
				.build();

		$.assertSerializes(model)
				.intoContentOf("hal-embedded-collection.json");
	}

	@Test // #864
	void renderCollectionUsingHalModelBuilder() throws Exception {

		var model = halModel() //
				.embed( //
						halModelOf(new Author("Greg L. Turnquist", null, null))
								.link(Link.of("http://localhost/author/1")) //
								.link(Link.of("http://localhost/authors", LinkRelation.of("authors"))) //
								.build())
				.embed( //
						halModelOf(new Author("Craig Walls", null, null)) //
								.links(Arrays.asList(Link.of("http://localhost/author/2"), //
										Link.of("http://localhost/authors", LinkRelation.of("authors")))) //
								.build())
				.embed( //
						halModelOf(new Author("Oliver Drotbohm", null, null)) //
								.link(Link.of("http://localhost/author/3")) //
								.link(Link.of("http://localhost/authors", LinkRelation.of("authors"))) //
								.build())
				.link(Link.of("http://localhost/authors")) //
				.build();

		$.assertSerializes(model)
				.intoContentOf("hal-embedded-collection.json");
	}

	@Test
	void progressivelyAddingContentUsingHalModelBuilder() {

		var builder = halModel();

		$.assertSerializes(builder.build())
				.intoContentOf("hal-empty.json");

		builder //
				.entity(halModel() //
						.entity(new Author("Greg L. Turnquist", null, null)) //
						.link(Link.of("http://localhost/author/1")) //
						.link(Link.of("http://localhost/authors", LinkRelation.of("authors"))) //
						.build());

		$.assertSerializes(builder.build())
				.intoContentOf("hal-one-thing.json");

		builder //
				.embed(new Product("Alf alarm clock", 19.99), LinkRelation.of("product")).build();

		$.assertSerializes(builder.build())
				.intoContentOf("hal-two-things.json");
	}

	@Test // #193
	void renderDifferentlyTypedEntities() throws Exception {

		var model = emptyHalModel() //
				.embed(new Staff("Frodo Baggins", "ring bearer")) //
				.embed(new Staff("Bilbo Baggins", "burglar")) //
				.embed(new Product("ring of power", 999.99)) //
				.embed(new Product("Saruman's staff", 9.99)) //
				.link(ALAN_WATTS_SELF) //
				.build();

		$.assertSerializes(model)
				.intoContentOf("hal-multiple-types.json");
	}

	@Test // #193
	void renderExplicitAndImplicitLinkRelations() throws Exception {

		var staff1 = new Staff("Frodo Baggins", "ring bearer");
		var staff2 = new Staff("Bilbo Baggins", "burglar");

		var model = halModel() //
				.embed(staff1) //
				.embed(staff2) //
				.embed(new Product("ring of power", 999.99)) //
				.embed(new Product("Saruman's staff", 9.99)) //
				.link(ALAN_WATTS_SELF) //
				.embed(staff1, LinkRelation.of("ring bearers")) //
				.embed(staff2, LinkRelation.of("burglars")) //
				.link(Link.of("/people/frodo-baggins", LinkRelation.of("frodo"))) //
				.build();

		$.assertSerializes(model)
				.intoContentOf("hal-explicit-and-implicit-relations.json");
	}

	@Test // #175 #864
	void renderZoomProtocolUsingHalModelBuilder() {

		var products = new TreeMap<Integer, ZoomProduct>();

		products.put(998, new ZoomProduct("someValue", true, true));
		products.put(777, new ZoomProduct("someValue", true, false));
		products.put(444, new ZoomProduct("someValue", false, true));
		products.put(333, new ZoomProduct("someValue", false, true));
		products.put(222, new ZoomProduct("someValue", false, true));
		products.put(111, new ZoomProduct("someValue", false, true));
		products.put(555, new ZoomProduct("someValue", false, true));
		products.put(666, new ZoomProduct("someValue", false, true));

		var productCollectionModel = products.keySet().stream() //
				.map(id -> EntityModel.of(products.get(id), Link.of("http://localhost/products/{id}").expand(id))) //
				.toList();

		var favoriteProducts = LinkRelation.of("favorite products");
		var purchasedProducts = LinkRelation.of("purchased products");

		var builder = halModel();

		builder.link(Link.of("/products").withSelfRel());

		for (EntityModel<ZoomProduct> productEntityModel : productCollectionModel) {

			var content = productEntityModel.getContent();

			if (content.isFavorite()) {

				builder.embed(productEntityModel, favoriteProducts) //
						.link(productEntityModel.getRequiredLink(SELF).withRel(favoriteProducts));
			}

			if (content.isPurchased()) {

				builder.embed(productEntityModel, purchasedProducts) //
						.link(productEntityModel.getRequiredLink(SELF).withRel(purchasedProducts));
			}
		}

		$.assertSerializes(builder.build())
				.intoContentOf("zoom-hypermedia.json");
	}

	@Test // #864
	void addsTypedEmptyCollection() throws Exception {

		RepresentationModel<?> model = halModel() //
				.embed(Collections.emptyList(), Author.class) //
				.build();

		assertEmptyEmbed(model, "authors");
	}

	@Test // #864
	void addsEmptyCollectionForLinkRelation() throws Exception {

		var model = halModel() //
				.embed(Collections.emptyList(), LinkRelation.of("authors")) //
				.build();

		assertEmptyEmbed(model, "authors");
	}

	@Test // #864
	@SuppressWarnings("unchecked")
	void doesNotAddEmbeddForSimpleEmptyCollection() throws Exception {

		var model = halModel()
				.embed(Stream.of(new Product("iPad", 699.0)))
				.embed(Collections.emptyList())
				.build();

		DocumentContext context = JsonPath.parse($.writeObject(model));

		assertThat(context.read("$._embedded", Map.class)).containsOnlyKeys("products");
	}

	@Test // #1335
	void embedsStream() throws Exception {

		RepresentationModel<?> model = halModel().embed(Stream.of(new Product("iPad", 699.0))).build();

		DocumentContext context = JsonPath.parse($.writeObject(model));

		assertThat(context.read("$._embedded.products[0].name", String.class)).isEqualTo("iPad");
	}

	@Test // #1335
	void embedsEmptyStream() throws Exception {

		assertEmptyEmbed(halModel().embed(Stream.empty(), Product.class).build(), "products");
		assertEmptyEmbed(halModel().embed(Stream.empty(), LinkRelation.of("products")).build(), "products");
	}

	@Test // #1540
	void doesNotRenderAdditionalLinksBlockForCuriedEmbeds() {

		var mapper = MappingTestUtils
				.defaultMapper(it -> it.addModule(new HalJacksonModule())
						.handlerInstantiator(new HalJacksonModule.HalHandlerInstantiator( //
								new EvoInflectorLinkRelationProvider(), //
								new DefaultCurieProvider("foo", UriTemplate.of("http://localhost/foo/{rel}")), //
								MessageResolver.DEFAULTS_ONLY)));

		var model = halModel() //
				.embed(new Staff("Frodo Baggins", "ring bearer")) //
				.build();

		var writeValueAsString = mapper.writeValueAsString(model);

		assertThat(StringUtils.countOccurrencesOf(writeValueAsString, "_links")) //
				.isEqualTo(1);
	}

	private void assertEmptyEmbed(RepresentationModel<?> model, String name) throws Exception {

		$.assertSerializes(model).into(context -> {
			assertThat(context.read("$._embedded." + name, JSONArray.class).isEmpty()).isTrue();
		});
	}

	@Value
	@AllArgsConstructor
	static class Author {

		private String name;
		@Getter(onMethod = @__({ @JsonInclude(JsonInclude.Include.NON_NULL) })) private String born;
		@Getter(onMethod = @__({ @JsonInclude(JsonInclude.Include.NON_NULL) })) private String died;
	}

	@Value
	@AllArgsConstructor
	static class Staff {

		private String name;
		private String role;
	}

	@Value
	@AllArgsConstructor
	static class Product {

		private String name;
		private Double price;
	}

	@Data
	@AllArgsConstructor
	static class ZoomProduct {

		private String someProductProperty;
		@Getter(onMethod = @__({ @JsonIgnore })) private boolean favorite = false;
		@Getter(onMethod = @__({ @JsonIgnore })) private boolean purchased = false;
	}
}
