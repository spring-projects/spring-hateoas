/*
 * Copyright 2012-2024 the original author or authors.
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

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.*;
import org.springframework.hateoas.MappingTestUtils.ContextualMapper;
import org.springframework.hateoas.PagedModel.PageMetadata;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.mediatype.hal.HalConfiguration.RenderSingleLinks;
import org.springframework.hateoas.mediatype.hal.HalJacksonModule.HalHandlerInstantiator;
import org.springframework.hateoas.server.core.AnnotationLinkRelationProvider;
import org.springframework.hateoas.server.core.EmbeddedWrappers;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;

/**
 * Integration tests for Jackson 2 HAL integration.
 *
 * @author Alexander Baetz
 * @author Oliver Gierke
 * @author Greg Turnquist
 * @author Jeffrey Walraven
 */
class HalJacksonModuleIntegrationTest {

	static final String SINGLE_LINK_REFERENCE = "{\"_links\":{\"self\":{\"href\":\"localhost\"}}}";
	static final String LIST_LINK_REFERENCE = "{\"_links\":{\"self\":[{\"href\":\"localhost\"},{\"href\":\"localhost2\"}]}}";

	static final String SIMPLE_EMBEDDED_RESOURCE_REFERENCE = "{\"_embedded\":{\"content\":[\"first\",\"second\"]},\"_links\":{\"self\":{\"href\":\"localhost\"}}}";
	static final String SINGLE_EMBEDDED_RESOURCE_REFERENCE = "{\"_embedded\":{\"content\":[{\"text\":\"test1\",\"number\":1,\"_links\":{\"self\":{\"href\":\"localhost\"}}}]},\"_links\":{\"self\":{\"href\":\"localhost\"}}}";
	static final String LIST_EMBEDDED_RESOURCE_REFERENCE = "{\"_embedded\":{\"content\":[{\"text\":\"test1\",\"number\":1,\"_links\":{\"self\":{\"href\":\"localhost\"}}},{\"text\":\"test2\",\"number\":2,\"_links\":{\"self\":{\"href\":\"localhost\"}}}]},\"_links\":{\"self\":{\"href\":\"localhost\"}}}";

	static final String ANNOTATED_EMBEDDED_RESOURCE_REFERENCE = "{\"_embedded\":{\"pojos\":[{\"text\":\"test1\",\"number\":1,\"_links\":{\"self\":{\"href\":\"localhost\"}}}]},\"_links\":{\"self\":{\"href\":\"localhost\"}}}";
	static final String ANNOTATED_EMBEDDED_RESOURCES_REFERENCE = "{\"_embedded\":{\"pojos\":[{\"text\":\"test1\",\"number\":1,\"_links\":{\"self\":{\"href\":\"localhost\"}}},{\"text\":\"test2\",\"number\":2,\"_links\":{\"self\":{\"href\":\"localhost\"}}}]}}";

	static final String ANNOTATED_PAGED_RESOURCES = "{\"_embedded\":{\"pojos\":[{\"text\":\"test1\",\"number\":1,\"_links\":{\"self\":{\"href\":\"localhost\"}}},{\"text\":\"test2\",\"number\":2,\"_links\":{\"self\":{\"href\":\"localhost\"}}}]},\"_links\":{\"next\":{\"href\":\"foo\"},\"prev\":{\"href\":\"bar\"}},\"page\":{\"size\":2,\"totalElements\":4,\"totalPages\":2,\"number\":0}}";

	static final Links PAGINATION_LINKS = Links.of(Link.of("foo", IanaLinkRelations.NEXT.value()),
			Link.of("bar", IanaLinkRelations.PREV.value()));

	static final String CURIED_DOCUMENT = "{\"_links\":{\"self\":{\"href\":\"foo\"},\"foo:myrel\":{\"href\":\"bar\"},\"curies\":[{\"href\":\"http://localhost:8080/rels/{rel}\",\"name\":\"foo\",\"templated\":true}]}}";
	static final String MULTIPLE_CURIES_DOCUMENT = "{\"_links\":{\"default:myrel\":{\"href\":\"foo\"},\"curies\":[{\"href\":\"bar\",\"name\":\"foo\"},{\"href\":\"foo\",\"name\":\"bar\"}]}}";
	static final String SINGLE_NON_CURIE_LINK = "{\"_links\":{\"self\":{\"href\":\"foo\"}}}";
	static final String EMPTY_DOCUMENT = "{}";

	static final String LINK_TEMPLATE = "{\"_links\":{\"search\":{\"href\":\"/foo{?bar}\",\"templated\":true}}}";

	static final String LINK_WITH_TITLE = "{\"_links\":{\"ns:foobar\":{\"href\":\"target\",\"title\":\"Foobar's title!\"}}}";

	static final String SINGLE_WITH_ONE_EXTRA_ATTRIBUTES = "{\"_links\":{\"self\":{\"href\":\"localhost\",\"title\":\"the title\"}}}";
	static final String SINGLE_WITH_ALL_EXTRA_ATTRIBUTES = "{\"_links\":{\"self\":{\"href\":\"localhost\",\"hreflang\":\"en\",\"title\":\"the title\",\"type\":\"the type\",\"deprecation\":\"/customers/deprecated\"}}}";

	private JsonMapper mapper;
	private ContextualMapper $;

	@BeforeEach
	void setUpModule() {

		this.mapper = HalTestUtils.halMapper();
		this.$ = HalTestUtils.getMapper(new HalConfiguration());
	}

	/**
	 * @see #29
	 */
	@Test
	void rendersSingleLinkAsObject() throws Exception {

		$.assertSerializes(new RepresentationModel<>().add(Link.of("localhost")))
				.into(SINGLE_LINK_REFERENCE);
	}

	/**
	 * @see #100
	 */
	@Test
	void rendersAllExtraRFC8288Attributes() throws Exception {

		var model = new RepresentationModel<>()
				.add(Link.of("localhost", "self") //
						.withHreflang("en") //
						.withTitle("the title") //
						.withType("the type") //
						.withMedia("the media") //
						.withDeprecation("/customers/deprecated"));

		$.assertSerializes(model)
				.into(SINGLE_WITH_ALL_EXTRA_ATTRIBUTES);
	}

	/**
	 * HAL doesn't support "media" so it's removed from the "expected" link.
	 *
	 * @see #699
	 */
	@Test
	void deserializeAllExtraRFC8288Attributes() throws Exception {

		var model = new RepresentationModel<>()
				.add(Link.of("localhost", "self") //
						.withHreflang("en") //
						.withTitle("the title") //
						.withType("the type") //
						.withDeprecation("/customers/deprecated"));

		$.assertDeserializes(SINGLE_WITH_ALL_EXTRA_ATTRIBUTES)
				.into(model);
	}

	@Test
	void rendersWithOneExtraRFC8288Attribute() throws Exception {

		var model = new RepresentationModel<>()
				.add(Link.of("localhost", "self").withTitle("the title"));

		$.assertSerializes(model)
				.into(SINGLE_WITH_ONE_EXTRA_ATTRIBUTES)
				.andBack();
	}

	@Test
	void deserializeSingleLink() throws Exception {

		var model = new RepresentationModel<>()
				.add(Link.of("localhost"));

		$.assertDeserializes(SINGLE_LINK_REFERENCE)
				.into(model);
	}

	/**
	 * @see #29
	 */
	@Test
	void rendersMultipleLinkAsArray() throws Exception {

		RepresentationModel<?> model = new RepresentationModel<>()
				.add(Link.of("localhost"))
				.add(Link.of("localhost2"));

		$.assertSerializes(model)
				.into(LIST_LINK_REFERENCE)
				.andBack();
	}

	@Test
	void rendersSimpleResourcesAsEmbedded() throws Exception {

		var model = CollectionModel.of(List.of("first", "second"))
				.add(Link.of("localhost"));

		$.assertSerializes(model)
				.into(SIMPLE_EMBEDDED_RESOURCE_REFERENCE)
				.andBack(String.class);
	}

	@Test
	void rendersSingleResourceResourcesAsEmbedded() throws Exception {

		var model = CollectionModel.of(List.of(EntityModel.of(new SimplePojo("test1", 1), Link.of("localhost"))))
				.add(Link.of("localhost"));

		$.assertSerializes(model)
				.into(SINGLE_EMBEDDED_RESOURCE_REFERENCE)
				.andBack(EntityModel.class, SimplePojo.class);
	}

	@Test
	void rendersMultipleResourceResourcesAsEmbedded() throws Exception {

		var model = setupCollectionModel()
				.add(Link.of("localhost"));

		$.assertSerializes(model)
				.into(LIST_EMBEDDED_RESOURCE_REFERENCE)
				.andBack(EntityModel.class, SimplePojo.class);
	}

	/**
	 * @see #47, #60
	 */
	@Test
	void serializesAnnotatedResourceResourcesAsEmbedded() throws Exception {

		var resources = CollectionModel
				.of(List.of(EntityModel.of(new SimpleAnnotatedPojo("test1", 1), Link.of("localhost"))))
				.add(Link.of("localhost"));

		$.assertSerializes(resources)
				.into(ANNOTATED_EMBEDDED_RESOURCE_REFERENCE)
				.andBack(EntityModel.class, SimpleAnnotatedPojo.class);
	}

	/**
	 * @see #63
	 */
	@Test
	void serializesMultipleAnnotatedResourceResourcesAsEmbedded() throws Exception {

		$.assertSerializes(setupAnnotatedResources())
				.into(ANNOTATED_EMBEDDED_RESOURCES_REFERENCE)
				.andBack(EntityModel.class, SimpleAnnotatedPojo.class);
	}

	/**
	 * @see #63, #64
	 */
	@Test
	void serializesPagedResource() throws Exception {

		$.assertSerializes(setupAnnotatedPagedResources())
				.into(ANNOTATED_PAGED_RESOURCES)
				.andBack(EntityModel.class, SimpleAnnotatedPojo.class);
	}

	/**
	 * @see #125
	 */
	@Test
	void rendersCuriesCorrectly() throws Exception {

		var model = CollectionModel.of(Collections.emptySet(), Link.of("foo"),
				Link.of("bar", "myrel"));

		assertThat(getCuriedMapper().writeValueAsString(model)).isEqualTo(CURIED_DOCUMENT);
	}

	/**
	 * @see #125
	 */
	@Test
	void doesNotRenderCuriesIfNoLinkIsPresent() throws Exception {

		var model = CollectionModel.of(Collections.emptySet());

		assertThat(getCuriedMapper().writeValueAsString(model)).isEqualTo(EMPTY_DOCUMENT);
	}

	/**
	 * @see #125
	 */
	@Test
	void doesNotRenderCuriesIfNoCurieLinkIsPresent() throws Exception {

		var model = CollectionModel.of(Collections.emptySet())
				.add(Link.of("foo"));

		assertThat(getCuriedMapper().writeValueAsString(model)).isEqualTo(SINGLE_NON_CURIE_LINK);
	}

	/**
	 * @see #137
	 */
	@Test
	void rendersTemplate() throws Exception {

		var support = new RepresentationModel<>()
				.add(Link.of("/foo{?bar}", "search"));

		$.assertSerializes(support).into(LINK_TEMPLATE);
	}

	/**
	 * @see #142
	 */
	@Test
	void rendersMultipleCuries() throws Exception {

		var model = CollectionModel.of(Collections.emptySet())
				.add(Link.of("foo", "myrel"));

		var provider = new DefaultCurieProvider("default", UriTemplate.of("/doc{?rel}")) {

			@Override
			public Collection<? extends Object> getCurieInformation(Links links) {
				return Arrays.asList(new Curie("foo", "bar"), new Curie("bar", "foo"));
			}
		};

		assertThat(getCuriedMapper(provider).writeValueAsString(model)).isEqualTo(MULTIPLE_CURIES_DOCUMENT);
	}

	/**
	 * @see #286, #236
	 */
	@Test
	void rendersEmptyEmbeddedCollections() throws Exception {

		var wrappers = new EmbeddedWrappers(false);
		var model = CollectionModel.of(List.of(wrappers.emptyCollectionOf(SimpleAnnotatedPojo.class)));

		$.assertSerializes(model)
				.into("{\"_embedded\":{\"pojos\":[]}}");
	}

	/**
	 * @see #378
	 */
	@Test
	void rendersTitleIfMessageSourceResolvesNamespacedKey() throws Exception {
		verifyResolvedTitle("_links.ns:foobar.title");
	}

	/**
	 * @see #378
	 */
	@Test
	void rendersTitleIfMessageSourceResolvesLocalKey() throws Exception {
		verifyResolvedTitle("_links.foobar.title");
	}

	@Test
	void rendersSingleLinkAsArrayWhenConfigured() throws Exception {

		var model = new RepresentationModel<>()
				.add(Link.of("localhost").withSelfRel());

		HalTestUtils.getMapper(new HalConfiguration().withRenderSingleLinks(RenderSingleLinks.AS_ARRAY))
				.assertSerializes(model)
				.into("{\"_links\":{\"self\":[{\"href\":\"localhost\"}]}}");
	}

	/**
	 * @see #667
	 */
	@Test
	void handleTemplatedLinksOnDeserialization() throws IOException {

		var original = new RepresentationModel<>()
				.add(Link.of("/orders{?id}", "order"));

		$.assertSerializes(original)
				.into("{\"_links\":{\"order\":{\"href\":\"/orders{?id}\",\"templated\":true}}}")
				.andBack();
	}

	@Test // #811
	void rendersSpecificRelWithSingleLinkAsArrayIfConfigured() throws Exception {

		HalTestUtils.getMapper(new HalConfiguration().withRenderSingleLinksFor("foo", RenderSingleLinks.AS_ARRAY))
				.assertSerializes(new RepresentationModel<>().add(Link.of("/some-href", "foo")))
				.into("{\"_links\":{\"foo\":[{\"href\":\"/some-href\"}]}}");
	}

	@Test // #1019
	void doesNotRenderTitleForEmptyString() throws Exception {

		$.assertSerializes(new HalJacksonModule.HalLink(Link.of("/some-href", "foo"), ""))
				.into("{\"href\":\"/some-href\"}");
	}

	@Test // #1019
	void resolvesMissingHalLinkRelationToEmptyString() throws Exception {

		var relation = HalLinkRelation.of(LinkRelation.of("someRel"));

		var accessor = new MessageSourceAccessor(new StaticMessageSource());

		assertThatNoException().isThrownBy(() -> {
			assertThat(accessor.getMessage(relation)).isEqualTo("");
		});
	}

	@Test // #1132
	void forwardsPropertyNamingStrategyToNonIanaLinkRelations() {

		var model = CollectionModel.of(List.of(new SomeSample()))
				.add(Link.of("/foo", LinkRelation.of("someSample")))
				.add(Link.of("/foo/form", IanaLinkRelations.EDIT_FORM));

		var result = mapper.rebuild() //
				.propertyNamingStrategy(new SnakeCaseStrategy()) //
				.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
				.build()
				.writeValueAsString(model);

		Stream.of("$._embedded", "$._links") //
				.map(JsonPath::compile) //
				.map(it -> it.<Map<String, Object>> read(result)) //
				.forEach(it -> assertThat(it).containsKey("some_sample"));

		assertThat(JsonPath.compile("$._links").<Map<String, Object>> read(result)) //
				.containsKey(IanaLinkRelations.EDIT_FORM.value());
	}

	@Test // #1132
	void doesNotApplyPropertyNamingStrategyToLinkRelationsIfConfigurationOptsOut() throws Exception {

		var model = CollectionModel.of(Arrays.asList(new SomeSample()))
				.add(Link.of("/foo", LinkRelation.of("someSample")));

		var configuration = new HalConfiguration().withApplyPropertyNamingStrategy(false);
		var result = HalTestUtils.halMapper(configuration,
				it -> it.propertyNamingStrategy(new SnakeCaseStrategy()) //
						.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS))
				.writeValueAsString(model);

		Stream.of("$._embedded", "$._links") //
				.map(JsonPath::compile) //
				.map(it -> it.<Map<String, Object>> read(result)) //
				.forEach(it -> assertThat(it).containsKey("someSample"));
	}

	@Test // #1157, #1352
	void rendersMapContentCorrectly() throws Exception {

		var map = Map.of(
				"key", "value",
				"anotherKey", "anotherValue");

		var model = EntityModel.of(map, Link.of("foo", IanaLinkRelations.SELF));

		DocumentContext context = JsonPath.parse(mapper.writeValueAsString(model),
				Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS));

		assertThat(context.read("$.key", String.class)).isEqualTo("value");
		assertThat(context.read("$.anotherKey", String.class)).isEqualTo("anotherValue");
		assertThat(context.read("$.content", Object.class)).isNull();
	}

	@Test // #1157
	void deserializesEntityModelForMapCorrectly() throws Exception {

		$.assertDeserializes("{ \"key\" : \"value\" }")
				.into(new ParameterizedTypeReference<EntityModel<Map<String, Object>>>() {})
				.matching(result -> {
					assertThat(result.getContent()).containsEntry("key", "value");
				});
	}

	@Test // #1157
	void doesNotSetSuperflousPropertiesAsMapIfSpecialContentTypeIsRequested() throws Exception {

		$.assertDeserializes("{ \"name\" : \"Dave\", \"instrument\" : \"Guitar\" }")
				.intoEntityModel(SomeSample.class)
				.matching(result -> {
					assertThat(result.getContent().name).isEqualTo("Dave");
				});
	}

	@Test // #1399
	void rendersCurieInformationIfCuriedLinkIsGiven() throws Exception {

		var model = new RepresentationModel<>().add(Link.of("/href", LinkRelation.of("foo:bar")));
		var context = JsonPath.parse(getCuriedMapper().writeValueAsString(model));

		assertThat(context.read("$._links.curies", JSONArray.class)).isNotEmpty();
	}

	@Test // #1428
	void doesNotRenderCuriesIfNoneConfigured() throws Exception {

		var mapper = getCuriedMapper(new DefaultCurieProvider(Collections.emptyMap()));
		var model = new RepresentationModel<>().add(Link.of("/href", LinkRelation.of("foo:bar")));

		var context = JsonPath.parse(mapper.writeValueAsString(model));

		assertThatExceptionOfType(PathNotFoundException.class)
				.isThrownBy(() -> context.read("$.curies", JSONObject.class));
	}

	@Test // #1515
	void rendersLinksWhenMapEntrySortingIsEnabled() throws Exception {

		mapper.rebuild()
				.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
				.build()
				.writeValueAsString(new RepresentationModel<>().add(Link.of("/href")));
	}

	@Test // #1515
	void rendersEmbeddedKeysWhenMapEntrySortingIsEnabled() throws Exception {

		List<SimplePojo> embbededs = Arrays.asList(new SimplePojo(), new SimpleAnnotatedPojo());

		mapper.rebuild()
				.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
				.build()
				.writeValueAsString(CollectionModel.of(embbededs));
	}

	@Test // #1516
	void considersNamingBase() throws Exception {

		mapper.rebuild()
				.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
				.build()
				.writeValueAsString(new RepresentationModel<>().add(Link.of("/href", "fooBar")));
	}

	@Relation(collectionRelation = "someSample")
	static class SomeSample {
		@Nullable @JsonProperty String name;
	}

	private void verifyResolvedTitle(String resourceBundleKey) throws Exception {

		LocaleContextHolder.setLocale(Locale.US);

		var messageSource = new StaticMessageSource();
		messageSource.addMessage(resourceBundleKey, Locale.US, "Foobar's title!");

		var resource = new RepresentationModel<>().add(Link.of("target", "ns:foobar"));

		MappingTestUtils
				.createMapper(getCuriedMapper(CurieProvider.NONE, messageSource))
				.assertSerializes(resource)
				.into(LINK_WITH_TITLE);

	}

	private static CollectionModel<EntityModel<SimpleAnnotatedPojo>> setupAnnotatedPagedResources() {

		var content = List.of(
				EntityModel.of(new SimpleAnnotatedPojo("test1", 1), Link.of("localhost")),
				EntityModel.of(new SimpleAnnotatedPojo("test2", 2), Link.of("localhost")));

		return PagedModel.of(content, new PageMetadata(2, 0, 4), PAGINATION_LINKS);
	}

	private static CollectionModel<EntityModel<SimpleAnnotatedPojo>> setupAnnotatedResources() {

		var content = List.of(
				EntityModel.of(new SimpleAnnotatedPojo("test1", 1), Link.of("localhost")),
				EntityModel.of(new SimpleAnnotatedPojo("test2", 2), Link.of("localhost")));

		return CollectionModel.of(content);
	}

	private static CollectionModel<EntityModel<SimplePojo>> setupCollectionModel() {

		var content = List.of(
				EntityModel.of(new SimplePojo("test1", 1), Link.of("localhost")),
				EntityModel.of(new SimplePojo("test2", 2), Link.of("localhost")));

		return CollectionModel.of(content);
	}

	private JsonMapper getCuriedMapper() {
		return getCuriedMapper(new DefaultCurieProvider("foo", UriTemplate.of("http://localhost:8080/rels/{rel}")));
	}

	private JsonMapper getCuriedMapper(CurieProvider provider) {
		return getCuriedMapper(provider, null);
	}

	private JsonMapper getCuriedMapper(CurieProvider provider, @Nullable MessageSource messageSource) {

		var reslProvider = new AnnotationLinkRelationProvider();
		var instantiator = new HalHandlerInstantiator(reslProvider, provider, MessageResolver.of(messageSource));

		return MappingTestUtils
				.defaultMapper(it -> it.addModule(new HalJacksonModule()).handlerInstantiator(instantiator));
	}
}
