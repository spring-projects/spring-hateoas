/*
 * Copyright 2017-2020 the original author or authors.
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
package org.springframework.hateoas.mediatype.hal.forms;

import static org.assertj.core.api.Assertions.*;

import lombok.Getter;
import net.minidev.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.AbstractJackson2MarshallingIntegrationTest;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.mediatype.hal.CurieProvider;
import org.springframework.hateoas.mediatype.hal.DefaultCurieProvider;
import org.springframework.hateoas.mediatype.hal.Jackson2HalIntegrationTest;
import org.springframework.hateoas.mediatype.hal.SimpleAnnotatedPojo;
import org.springframework.hateoas.mediatype.hal.SimplePojo;
import org.springframework.hateoas.mediatype.hal.forms.Jackson2HalFormsModule.HalFormsHandlerInstantiator;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.hateoas.server.core.AnnotationLinkRelationProvider;
import org.springframework.hateoas.server.core.DelegatingLinkRelationProvider;
import org.springframework.hateoas.server.core.EmbeddedWrappers;
import org.springframework.hateoas.support.EmployeeResource;
import org.springframework.hateoas.support.MappingUtils;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

/**
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
class Jackson2HalFormsIntegrationTest extends AbstractJackson2MarshallingIntegrationTest {

	static final Links PAGINATION_LINKS = Links.of( //
			new Link("foo", IanaLinkRelations.NEXT), //
			new Link("bar", IanaLinkRelations.PREV) //
	);

	// MessageSource messageSource = mock(MessageSource.class);

	@BeforeEach
	void setUpModule() {

		// TestAffordances.enableMediaTypes(MediaTypes.HAL_FORMS_JSON);

		LinkRelationProvider provider = new DelegatingLinkRelationProvider(new AnnotationLinkRelationProvider(),
				Jackson2HalIntegrationTest.DefaultLinkRelationProvider.INSTANCE);

		mapper.registerModule(new Jackson2HalFormsModule());
		mapper.setHandlerInstantiator(new HalFormsHandlerInstantiator( //
				provider, CurieProvider.NONE, MessageResolver.DEFAULTS_ONLY, true, new HalFormsConfiguration()));
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
	}

	@Test
	void rendersSingleLinkAsObject() throws Exception {

		RepresentationModel<?> resourceSupport = new RepresentationModel<>();
		resourceSupport.add(new Link("localhost"));

		assertThat(write(resourceSupport))
				.isEqualTo(MappingUtils.read(new ClassPathResource("single-link-reference.json", getClass())));
	}

	@Test
	void deserializeSingleLink() throws Exception {

		RepresentationModel<?> expected = new RepresentationModel<>();
		expected.add(new Link("localhost"));

		assertThat(read(MappingUtils.read(new ClassPathResource("single-link-reference.json", getClass())),
				RepresentationModel.class)).isEqualTo(expected);
	}

	@Test
	void rendersMultipleLinkAsArray() throws Exception {

		RepresentationModel<?> resourceSupport = new RepresentationModel<>();
		resourceSupport.add(new Link("localhost"));
		resourceSupport.add(new Link("localhost2"));

		assertThat(write(resourceSupport))
				.isEqualTo(MappingUtils.read(new ClassPathResource("list-link-reference.json", getClass())));
	}

	@Test
	void deserializeMultipleLinks() throws Exception {

		RepresentationModel<?> expected = new RepresentationModel<>();
		expected.add(new Link("localhost"));
		expected.add(new Link("localhost2"));

		assertThat(read(MappingUtils.read(new ClassPathResource("list-link-reference.json", getClass())),
				RepresentationModel.class)).isEqualTo(expected);
	}

	@Test
	void rendersRepresentationModelWithTemplates() throws Exception {

		EmployeeResource resource = new EmployeeResource("Frodo Baggins");

		Link link = Affordances.of(new Link("/employees/1")) //
				.afford(HttpMethod.POST) //
				.withInputAndOutput(EmployeeResource.class) //
				.withName("foo") //
				.toLink();

		resource.add(link);

		assertThat(write(resource))
				.isEqualTo(MappingUtils.read(new ClassPathResource("employee-resource-support.json", getClass())));
	}

	@Test
	void rendersResource() throws Exception {

		EntityModel<SimplePojo> resource = new EntityModel<>(new SimplePojo("test1", 1), new Link("localhost"));

		assertThat(write(resource))
				.isEqualTo(MappingUtils.read(new ClassPathResource("simple-resource-unwrapped.json", getClass())));
	}

	@Test
	void deserializesResource() throws IOException {

		EntityModel<SimplePojo> expected = new EntityModel<>(new SimplePojo("test1", 1), new Link("localhost"));

		EntityModel<SimplePojo> result = mapper.readValue(
				MappingUtils.read(new ClassPathResource("simple-resource-unwrapped.json", getClass())),
				mapper.getTypeFactory().constructParametricType(EntityModel.class, SimplePojo.class));

		assertThat(result).isEqualTo(expected);
	}

	@Test
	void rendersSimpleResourcesAsEmbedded() throws Exception {

		List<String> content = new ArrayList<>();
		content.add("first");
		content.add("second");

		CollectionModel<String> resources = new CollectionModel<>(content);
		resources.add(new Link("localhost"));

		assertThat(write(resources))
				.isEqualTo(MappingUtils.read(new ClassPathResource("simple-embedded-resource-reference.json", getClass())));
	}

	@Test
	void deserializesSimpleResourcesAsEmbedded() throws Exception {

		List<String> content = new ArrayList<>();
		content.add("first");
		content.add("second");

		CollectionModel<String> expected = new CollectionModel<>(content);
		expected.add(new Link("localhost"));

		CollectionModel<String> result = mapper.readValue(
				MappingUtils.read(new ClassPathResource("simple-embedded-resource-reference.json", getClass())),
				mapper.getTypeFactory().constructParametricType(CollectionModel.class, String.class));

		assertThat(result).isEqualTo(expected);

	}

	@Test
	void rendersSingleResourceResourcesAsEmbedded() throws Exception {

		List<EntityModel<SimplePojo>> content = new ArrayList<>();
		content.add(new EntityModel<>(new SimplePojo("test1", 1), new Link("localhost")));

		CollectionModel<EntityModel<SimplePojo>> resources = new CollectionModel<>(content);
		resources.add(new Link("localhost"));

		assertThat(write(resources))
				.isEqualTo(MappingUtils.read(new ClassPathResource("single-embedded-resource-reference.json", getClass())));
	}

	@Test
	void deserializesSingleResourceResourcesAsEmbedded() throws Exception {

		List<EntityModel<SimplePojo>> content = new ArrayList<>();
		content.add(new EntityModel<>(new SimplePojo("test1", 1), new Link("localhost")));

		CollectionModel<EntityModel<SimplePojo>> expected = new CollectionModel<>(content);
		expected.add(new Link("localhost"));

		CollectionModel<EntityModel<SimplePojo>> result = mapper.readValue(
				MappingUtils.read(new ClassPathResource("single-embedded-resource-reference.json", getClass())),
				mapper.getTypeFactory().constructParametricType(CollectionModel.class,
						mapper.getTypeFactory().constructParametricType(EntityModel.class, SimplePojo.class)));

		assertThat(result).isEqualTo(expected);
	}

	@Test
	void rendersMultipleResourceResourcesAsEmbedded() throws Exception {

		CollectionModel<EntityModel<SimplePojo>> resources = setupResources();
		resources.add(new Link("localhost"));

		assertThat(write(resources))
				.isEqualTo(MappingUtils.read(new ClassPathResource("multiple-resource-resources.json", getClass())));
	}

	@Test
	void deserializesMultipleResourceResourcesAsEmbedded() throws Exception {

		CollectionModel<EntityModel<SimplePojo>> expected = setupResources();
		expected.add(new Link("localhost"));

		CollectionModel<EntityModel<SimplePojo>> result = mapper.readValue(
				MappingUtils.read(new ClassPathResource("multiple-resource-resources.json", getClass())),
				mapper.getTypeFactory().constructParametricType(CollectionModel.class,
						mapper.getTypeFactory().constructParametricType(EntityModel.class, SimplePojo.class)));

		assertThat(result).isEqualTo(expected);
	}

	@Test
	void serializesAnnotatedResourceResourcesAsEmbedded() throws Exception {

		List<EntityModel<SimpleAnnotatedPojo>> content = new ArrayList<>();
		content.add(new EntityModel<>(new SimpleAnnotatedPojo("test1", 1), new Link("localhost")));

		CollectionModel<EntityModel<SimpleAnnotatedPojo>> resources = new CollectionModel<>(content);
		resources.add(new Link("localhost"));

		assertThat(write(resources))
				.isEqualTo(MappingUtils.read(new ClassPathResource("annotated-resource-resources.json", getClass())));
	}

	@Test
	void deserializesAnnotatedResourceResourcesAsEmbedded() throws Exception {

		List<EntityModel<SimpleAnnotatedPojo>> content = new ArrayList<>();
		content.add(new EntityModel<>(new SimpleAnnotatedPojo("test1", 1), new Link("localhost")));

		CollectionModel<EntityModel<SimpleAnnotatedPojo>> expected = new CollectionModel<>(content);
		expected.add(new Link("localhost"));

		CollectionModel<EntityModel<SimpleAnnotatedPojo>> result = mapper.readValue(
				MappingUtils.read(new ClassPathResource("annotated-resource-resources.json", getClass())),
				mapper.getTypeFactory().constructParametricType(CollectionModel.class,
						mapper.getTypeFactory().constructParametricType(EntityModel.class, SimpleAnnotatedPojo.class)));

		assertThat(result).isEqualTo(expected);
	}

	@Test
	void serializesMultipleAnnotatedResourceResourcesAsEmbedded() throws Exception {
		assertThat(write(setupAnnotatedResources()))
				.isEqualTo(MappingUtils.read(new ClassPathResource("annotated-embedded-resources-reference.json", getClass())));
	}

	@Test
	void deserializesMultipleAnnotatedResourceResourcesAsEmbedded() throws Exception {

		CollectionModel<EntityModel<SimpleAnnotatedPojo>> result = mapper.readValue(
				MappingUtils.read(new ClassPathResource("annotated-embedded-resources-reference.json", getClass())),
				mapper.getTypeFactory().constructParametricType(CollectionModel.class,
						mapper.getTypeFactory().constructParametricType(EntityModel.class, SimpleAnnotatedPojo.class)));

		assertThat(result).isEqualTo(setupAnnotatedResources());
	}

	@Test
	void serializesPagedResource() throws Exception {
		assertThat(write(setupAnnotatedPagedResources()))
				.isEqualTo(MappingUtils.read(new ClassPathResource("annotated-paged-resources.json", getClass())));
	}

	@Test
	void deserializesPagedResource() throws Exception {
		PagedModel<EntityModel<SimpleAnnotatedPojo>> result = mapper.readValue(
				MappingUtils.read(new ClassPathResource("annotated-paged-resources.json", getClass())),
				mapper.getTypeFactory().constructParametricType(PagedModel.class,
						mapper.getTypeFactory().constructParametricType(EntityModel.class, SimpleAnnotatedPojo.class)));

		assertThat(result).isEqualTo(setupAnnotatedPagedResources());
	}

	@Test
	void rendersCuriesCorrectly() throws Exception {

		CollectionModel<Object> resources = new CollectionModel<>(Collections.emptySet(), new Link("foo"),
				new Link("bar", "myrel"));

		assertThat(getCuriedObjectMapper().writeValueAsString(resources))
				.isEqualTo(MappingUtils.read(new ClassPathResource("curied-document.json", getClass())));
	}

	@Test
	void doesNotRenderCuriesIfNoLinkIsPresent() throws Exception {

		CollectionModel<Object> resources = new CollectionModel<>(Collections.emptySet());
		assertThat(getCuriedObjectMapper().writeValueAsString(resources))
				.isEqualTo(MappingUtils.read(new ClassPathResource("empty-document.json", getClass())));
	}

	@Test
	void doesNotRenderCuriesIfNoCurieLinkIsPresent() throws Exception {

		CollectionModel<Object> resources = new CollectionModel<>(Collections.emptySet());
		resources.add(new Link("foo"));

		assertThat(getCuriedObjectMapper().writeValueAsString(resources))
				.isEqualTo(MappingUtils.read(new ClassPathResource("single-non-curie-document.json", getClass())));
	}

	@Test
	void rendersTemplate() throws Exception {

		RepresentationModel<?> support = new RepresentationModel<>();
		support.add(new Link("/foo{?bar}", "search"));

		assertThat(write(support)).isEqualTo(MappingUtils.read(new ClassPathResource("link-template.json", getClass())));
	}

	@Test
	void rendersMultipleCuries() throws Exception {

		CollectionModel<Object> resources = new CollectionModel<>(Collections.emptySet());
		resources.add(new Link("foo", "myrel"));

		CurieProvider provider = new DefaultCurieProvider("default", UriTemplate.of("/doc{?rel}")) {
			@Override
			public Collection<?> getCurieInformation(Links links) {
				return Arrays.asList(new Curie("foo", "bar"), new Curie("bar", "foo"));
			}
		};

		assertThat(getCuriedObjectMapper(provider).writeValueAsString(resources))
				.isEqualTo(MappingUtils.read(new ClassPathResource("multiple-curies-document.json", getClass())));
	}

	@Test
	void rendersEmptyEmbeddedCollections() throws Exception {

		EmbeddedWrappers wrappers = new EmbeddedWrappers(false);

		List<Object> values = new ArrayList<>();
		values.add(wrappers.emptyCollectionOf(SimpleAnnotatedPojo.class));

		CollectionModel<Object> resources = new CollectionModel<>(values);

		assertThat(write(resources))
				.isEqualTo(MappingUtils.read(new ClassPathResource("empty-embedded-pojos.json", getClass())));
	}

	@Test
	void rendersTitleIfMessageSourceResolvesNamespacedKey() throws Exception {
		verifyResolvedTitle("_links.ns:foobar.title");
	}

	@Test
	void rendersTitleIfMessageSourceResolvesLocalKey() throws Exception {
		verifyResolvedTitle("_links.foobar.title");
	}

	/**
	 * @see #667
	 */
	@Test
	void handleTemplatedLinksOnDeserialization() throws IOException {

		RepresentationModel<?> original = new RepresentationModel<>();
		original.add(new Link("/orders{?id}", "order"));

		String serialized = mapper.writeValueAsString(original);

		String expected = "{\n  \"_links\" : {\n    \"order\" : {\n      \"href\" : \"/orders{?id}\",\n      \"templated\" : true\n    }\n  }\n}";

		assertThat(serialized).isEqualTo(expected);

		RepresentationModel<?> deserialized = mapper.readValue(serialized, RepresentationModel.class);

		assertThat(deserialized).isEqualTo(original);
	}

	@ParameterizedTest // #979
	@ValueSource(strings = { "firstname._prompt", //
			"HalFormsPayload.firstname._prompt", //
			"org.springframework.hateoas.mediatype.hal.forms.Jackson2HalFormsIntegrationTest$HalFormsPayload.firstname._prompt" })
	void usesResourceBundleToCreatePropertyPrompts(String key) {

		StaticMessageSource source = new StaticMessageSource();
		source.addMessage(key, Locale.US, "Vorname");

		Link link = Affordances.of(new Link("some:link")) //
				.afford(HttpMethod.POST) //
				.withInput(HalFormsPayload.class) //
				.withOutput(Object.class) //
				.withName("sample") //
				.toLink();

		EntityModel<HalFormsPayload> model = new EntityModel<>(new HalFormsPayload(), link);
		ObjectMapper mapper = getCuriedObjectMapper(CurieProvider.NONE, source);

		assertThatCode(() -> {

			String promptString = JsonPath.compile("$._templates.default.properties[0].prompt") //
					.read(mapper.writeValueAsString(model));

			assertThat(promptString).isEqualTo("Vorname");

		}).doesNotThrowAnyException();
	}

	@ParameterizedTest // #849
	@ValueSource(strings = { //
			"_templates.postHalFormsPayload.title", //
			"HalFormsPayload._templates.postHalFormsPayload.title", //
			"org.springframework.hateoas.mediatype.hal.forms.Jackson2HalFormsIntegrationTest$HalFormsPayload._templates.postHalFormsPayload.title", //
			"_templates.default.title", //
			"HalFormsPayload._templates.default.title", //
			"org.springframework.hateoas.mediatype.hal.forms.Jackson2HalFormsIntegrationTest$HalFormsPayload._templates.default.title" })
	void usesResourceBundleToCreateTemplateTitle(String key) {

		StaticMessageSource source = new StaticMessageSource();
		source.addMessage(key, Locale.US, "Template title");

		Link link = Affordances.of(new Link("some:link")) //
				.afford(HttpMethod.POST) //
				.withInput(HalFormsPayload.class) //
				.toLink();

		EntityModel<HalFormsPayload> model = new EntityModel<>(new HalFormsPayload(), link);
		ObjectMapper mapper = getCuriedObjectMapper(CurieProvider.NONE, source);

		assertThatCode(() -> {

			String promptString = JsonPath.compile("$._templates.default.title") //
					.read(mapper.writeValueAsString(model));

			assertThat(promptString).isEqualTo("Template title");

		}).doesNotThrowAnyException();
	}

	@Test
	void doesNotRenderPromptPropertyIfEmpty() throws Exception {

		HalFormsProperty property = HalFormsProperty.named("someName");

		assertThatPathDoesNotExist(property.withPrompt(""), "$.prompt");
		assertValueForPath(property.withPrompt("Some prompt"), "$.prompt", "Some prompt");
	}

	@Test
	void doesNotRenderRequiredPropertyIfFalse() throws Exception {

		HalFormsProperty property = HalFormsProperty.named("someName");

		assertThatPathDoesNotExist(property, "$.required");
		assertValueForPath(property.withRequired(true), ".required", true);
	}

	@Test
	void considersJsr303AnnotationsForTemplates() throws Exception {

		Link link = Affordances.of(new Link("localhost:8080")) //
				.afford(HttpMethod.POST) //
				.withInput(Jsr303Sample.class) //
				.toLink();

		EntityModel<Jsr303Sample> model = new EntityModel<>(new Jsr303Sample(), link);

		assertValueForPath(model, "$._templates.default.properties[0].readOnly", true);
		assertValueForPath(model, "$._templates.default.properties[0].regex", "[\\w\\s]");
		assertValueForPath(model, "$._templates.default.properties[0].required", true);
	}

	private void assertThatPathDoesNotExist(Object toMarshall, String path) throws Exception {

		ObjectMapper mapper = getCuriedObjectMapper();
		String json = mapper.writeValueAsString(toMarshall);

		assertThatExceptionOfType(PathNotFoundException.class) //
				.isThrownBy(() -> JsonPath.compile(path).read(json));
	}

	private void assertValueForPath(Object toMarshall, String path, Object expected) throws Exception {

		ObjectMapper mapper = getCuriedObjectMapper();
		String json = mapper.writeValueAsString(toMarshall);

		Object actual = JsonPath.compile(path).read(json);

		Object value = JSONArray.class.isInstance(actual) //
				? JSONArray.class.cast(actual).get(0) //
				: actual;

		assertThat(value).isEqualTo(expected);
	}

	private void verifyResolvedTitle(String resourceBundleKey) throws Exception {

		LocaleContextHolder.setLocale(Locale.US);

		StaticMessageSource messageSource = new StaticMessageSource();
		messageSource.addMessage(resourceBundleKey, Locale.US, "Foobar's title!");

		ObjectMapper objectMapper = getCuriedObjectMapper(CurieProvider.NONE, messageSource);

		RepresentationModel<?> resource = new RepresentationModel<>();
		resource.add(new Link("target", "ns:foobar"));

		assertThat(objectMapper.writeValueAsString(resource))
				.isEqualTo(MappingUtils.read(new ClassPathResource("link-with-title.json", getClass())));
	}

	private static CollectionModel<EntityModel<SimplePojo>> setupResources() {

		List<EntityModel<SimplePojo>> content = new ArrayList<>();
		content.add(new EntityModel<>(new SimplePojo("test1", 1), new Link("localhost")));
		content.add(new EntityModel<>(new SimplePojo("test2", 2), new Link("localhost")));

		return new CollectionModel<>(content);
	}

	private static CollectionModel<EntityModel<SimpleAnnotatedPojo>> setupAnnotatedResources() {

		List<EntityModel<SimpleAnnotatedPojo>> content = new ArrayList<>();
		content.add(new EntityModel<>(new SimpleAnnotatedPojo("test1", 1), new Link("localhost")));
		content.add(new EntityModel<>(new SimpleAnnotatedPojo("test2", 2), new Link("localhost")));

		return new CollectionModel<>(content);
	}

	private static CollectionModel<EntityModel<SimpleAnnotatedPojo>> setupAnnotatedPagedResources() {

		List<EntityModel<SimpleAnnotatedPojo>> content = new ArrayList<>();
		content.add(new EntityModel<>(new SimpleAnnotatedPojo("test1", 1), new Link("localhost")));
		content.add(new EntityModel<>(new SimpleAnnotatedPojo("test2", 2), new Link("localhost")));

		return new PagedModel<>(content, new PagedModel.PageMetadata(2, 0, 4), PAGINATION_LINKS);
	}

	private ObjectMapper getCuriedObjectMapper() {

		return getCuriedObjectMapper(new DefaultCurieProvider("foo", UriTemplate.of("http://localhost:8080/rels/{rel}")));
	}

	private ObjectMapper getCuriedObjectMapper(CurieProvider provider) {
		return getCuriedObjectMapper(provider, null);
	}

	private ObjectMapper getCuriedObjectMapper(CurieProvider provider, @Nullable MessageSource messageSource) {

		ObjectMapper mapper = new ObjectMapper();

		mapper.registerModule(new Jackson2HalFormsModule());
		mapper.setHandlerInstantiator(new HalFormsHandlerInstantiator(new AnnotationLinkRelationProvider(), provider,
				MessageResolver.of(messageSource), true, new HalFormsConfiguration()));
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		mapper.setSerializationInclusion(Include.NON_NULL);

		return mapper;
	}

	public static class HalFormsPayload {
		private @Getter String firstname;
	}

	public static class Jsr303Sample {

		private String firstname;

		/**
		 * @return the firstname
		 */
		@NotNull
		@Pattern(regexp = "[\\w\\s]")
		public String getFirstname() {
			return firstname;
		}
	}
}
