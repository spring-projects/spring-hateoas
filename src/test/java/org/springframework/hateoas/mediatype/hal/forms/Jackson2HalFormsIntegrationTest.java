/*
 * Copyright 2017-2021 the original author or authors.
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
import java.util.function.Consumer;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.MappingTestUtils;
import org.springframework.hateoas.MappingTestUtils.ContextualMapper;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.config.HateoasConfiguration;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.mediatype.hal.CurieProvider;
import org.springframework.hateoas.mediatype.hal.DefaultCurieProvider;
import org.springframework.hateoas.mediatype.hal.HalConfiguration;
import org.springframework.hateoas.mediatype.hal.HalTestUtils;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.hateoas.mediatype.hal.SimpleAnnotatedPojo;
import org.springframework.hateoas.mediatype.hal.SimplePojo;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsOptions.Inline;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsOptions.Remote;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.hateoas.server.core.AnnotationLinkRelationProvider;
import org.springframework.hateoas.server.core.DelegatingLinkRelationProvider;
import org.springframework.hateoas.server.core.EmbeddedWrappers;
import org.springframework.hateoas.support.EmployeeResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

/**
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
class Jackson2HalFormsIntegrationTest {

	static final Links PAGINATION_LINKS = Links.of( //
			Link.of("foo", IanaLinkRelations.NEXT), //
			Link.of("bar", IanaLinkRelations.PREV) //
	);

	final LinkRelationProvider provider = new DelegatingLinkRelationProvider(new AnnotationLinkRelationProvider(),
			HalTestUtils.DefaultLinkRelationProvider.INSTANCE);
	final Consumer<ObjectMapper> configurer = it -> {

		it.registerModule(new Jackson2HalFormsModule());
		it.configure(SerializationFeature.INDENT_OUTPUT, true);
	};

	final ContextualMapper mapper = MappingTestUtils.createMapper(Jackson2HalFormsIntegrationTest.class,
			configurer.andThen(it -> {

				GenericApplicationContext context = new AnnotationConfigApplicationContext(
						HalFormsMediaTypeConfiguration.class, HateoasConfiguration.class);

				it.setHandlerInstantiator(new Jackson2HalModule.HalHandlerInstantiator(provider, CurieProvider.NONE,
						MessageResolver.DEFAULTS_ONLY, new HalConfiguration(), context.getAutowireCapableBeanFactory()));
			}));

	@BeforeEach
	void setUpModule() {
		LocaleContextHolder.setLocale(Locale.US);
	}

	@Test
	void rendersSingleLinkAsObject() throws Exception {

		RepresentationModel<?> resourceSupport = new RepresentationModel<>();
		resourceSupport.add(Link.of("localhost"));

		assertThat(mapper.writeObject(resourceSupport))
				.isEqualTo(mapper.readFileContent("single-link-reference.json"));
	}

	@Test
	void deserializeSingleLink() throws Exception {

		RepresentationModel<?> expected = new RepresentationModel<>();
		expected.add(Link.of("localhost"));

		assertThat(mapper.readFile("single-link-reference.json")).isEqualTo(expected);
	}

	@Test
	void rendersMultipleLinkAsArray() throws Exception {

		RepresentationModel<?> resourceSupport = new RepresentationModel<>();
		resourceSupport.add(Link.of("localhost"));
		resourceSupport.add(Link.of("localhost2"));

		assertThat(mapper.writeObject(resourceSupport))
				.isEqualTo(mapper.readFileContent("list-link-reference.json"));
	}

	@Test
	void deserializeMultipleLinks() throws Exception {

		RepresentationModel<?> expected = new RepresentationModel<>();
		expected.add(Link.of("localhost"));
		expected.add(Link.of("localhost2"));

		assertThat(mapper.readFile("list-link-reference.json", RepresentationModel.class)).isEqualTo(expected);
	}

	@Test
	void rendersRepresentationModelWithTemplates() throws Exception {

		EmployeeResource resource = new EmployeeResource("Frodo Baggins");

		Link link = Affordances.of(Link.of("/employees/1")) //
				.afford(HttpMethod.POST) //
				.withInputAndOutput(EmployeeResource.class) //
				.withName("foo") //
				.toLink();

		resource.add(link);

		assertThat(mapper.writeObject(resource))
				.isEqualTo(mapper.readFileContent("employee-resource-support.json"));
	}

	@Test
	void rendersResource() throws Exception {

		EntityModel<SimplePojo> resource = EntityModel.of(new SimplePojo("test1", 1), Link.of("localhost"));

		assertThat(mapper.writeObject(resource))
				.isEqualTo(mapper.readFileContent("simple-resource-unwrapped.json"));
	}

	@Test
	void deserializesResource() throws IOException {

		EntityModel<SimplePojo> result = mapper.readFile("simple-resource-unwrapped.json", EntityModel.class,
				SimplePojo.class);

		assertThat(result).isEqualTo(EntityModel.of(new SimplePojo("test1", 1), Link.of("localhost")));
	}

	@Test
	void rendersSimpleResourcesAsEmbedded() throws Exception {

		List<String> content = new ArrayList<>();
		content.add("first");
		content.add("second");

		CollectionModel<String> resources = CollectionModel.of(content);
		resources.add(Link.of("localhost"));

		assertThat(mapper.writeObject(resources))
				.isEqualTo(mapper.readFileContent("simple-embedded-resource-reference.json"));
	}

	@Test
	void deserializesSimpleResourcesAsEmbedded() throws Exception {

		List<String> content = new ArrayList<>();
		content.add("first");
		content.add("second");

		CollectionModel<String> expected = CollectionModel.of(content);
		expected.add(Link.of("localhost"));

		CollectionModel<String> result = mapper.readFile("simple-embedded-resource-reference.json", CollectionModel.class,
				String.class);

		assertThat(result).isEqualTo(expected);

	}

	@Test
	void rendersSingleResourceResourcesAsEmbedded() throws Exception {

		List<EntityModel<SimplePojo>> content = new ArrayList<>();
		content.add(EntityModel.of(new SimplePojo("test1", 1), Link.of("localhost")));

		CollectionModel<EntityModel<SimplePojo>> resources = CollectionModel.of(content);
		resources.add(Link.of("localhost"));

		assertThat(mapper.writeObject(resources))
				.isEqualTo(mapper.readFileContent("single-embedded-resource-reference.json"));
	}

	@Test
	void deserializesSingleResourceResourcesAsEmbedded() throws Exception {

		List<EntityModel<SimplePojo>> content = new ArrayList<>();
		content.add(EntityModel.of(new SimplePojo("test1", 1), Link.of("localhost")));

		CollectionModel<EntityModel<SimplePojo>> expected = CollectionModel.of(content);
		expected.add(Link.of("localhost"));

		CollectionModel<EntityModel<SimplePojo>> result = mapper.readFile("single-embedded-resource-reference.json",
				CollectionModel.class, EntityModel.class, SimplePojo.class);

		assertThat(result).isEqualTo(expected);
	}

	@Test
	void rendersMultipleResourceResourcesAsEmbedded() throws Exception {

		CollectionModel<EntityModel<SimplePojo>> resources = setupResources();
		resources.add(Link.of("localhost"));

		assertThat(mapper.writeObject(resources))
				.isEqualTo(mapper.readFileContent("multiple-resource-resources.json"));
	}

	@Test
	void deserializesMultipleResourceResourcesAsEmbedded() throws Exception {

		CollectionModel<EntityModel<SimplePojo>> expected = setupResources();
		expected.add(Link.of("localhost"));

		CollectionModel<EntityModel<SimplePojo>> result = mapper.readFile("multiple-resource-resources.json",
				CollectionModel.class, EntityModel.class, SimplePojo.class);

		assertThat(result).isEqualTo(expected);
	}

	@Test
	void serializesAnnotatedResourceResourcesAsEmbedded() throws Exception {

		List<EntityModel<SimpleAnnotatedPojo>> content = new ArrayList<>();
		content.add(EntityModel.of(new SimpleAnnotatedPojo("test1", 1), Link.of("localhost")));

		CollectionModel<EntityModel<SimpleAnnotatedPojo>> resources = CollectionModel.of(content);
		resources.add(Link.of("localhost"));

		assertThat(mapper.writeObject(resources)).isEqualTo(mapper.readFileContent("annotated-resource-resources.json"));
	}

	@Test
	void deserializesAnnotatedResourceResourcesAsEmbedded() throws Exception {

		List<EntityModel<SimpleAnnotatedPojo>> content = new ArrayList<>();
		content.add(EntityModel.of(new SimpleAnnotatedPojo("test1", 1), Link.of("localhost")));

		CollectionModel<EntityModel<SimpleAnnotatedPojo>> expected = CollectionModel.of(content);
		expected.add(Link.of("localhost"));

		CollectionModel<EntityModel<SimpleAnnotatedPojo>> result = mapper.readFile("annotated-resource-resources.json",
				CollectionModel.class, EntityModel.class, SimpleAnnotatedPojo.class);

		assertThat(result).isEqualTo(expected);
	}

	@Test
	void serializesMultipleAnnotatedResourceResourcesAsEmbedded() throws Exception {
		assertThat(mapper.writeObject(setupAnnotatedResources()))
				.isEqualTo(mapper.readFileContent("annotated-embedded-resources-reference.json"));
	}

	@Test
	void deserializesMultipleAnnotatedResourceResourcesAsEmbedded() throws Exception {

		mapper.readFile("annotated-embedded-resources-reference.json",
				CollectionModel.class, EntityModel.class, SimpleAnnotatedPojo.class);

		CollectionModel<EntityModel<SimpleAnnotatedPojo>> result = mapper.readFile(
				"annotated-embedded-resources-reference.json", CollectionModel.class, EntityModel.class,
				SimpleAnnotatedPojo.class);

		assertThat(result).isEqualTo(setupAnnotatedResources());
	}

	@Test
	void serializesPagedResource() throws Exception {

		assertThat(mapper.writeObject(setupAnnotatedPagedResources()))
				.isEqualTo(mapper.readFileContent("annotated-paged-resources.json"));
	}

	@Test
	void deserializesPagedResource() throws Exception {

		PagedModel<EntityModel<SimpleAnnotatedPojo>> result = mapper.readFile("annotated-paged-resources.json",
				PagedModel.class, EntityModel.class, SimpleAnnotatedPojo.class);

		assertThat(result).isEqualTo(setupAnnotatedPagedResources());
	}

	@Test
	void rendersCuriesCorrectly() throws Exception {

		CollectionModel<Object> resources = CollectionModel.of(Collections.emptySet(), Link.of("foo"),
				Link.of("bar", "myrel"));

		assertThat(getCuriedObjectMapper().writeObject(resources))
				.isEqualTo(mapper.readFileContent("curied-document.json"));
	}

	@Test
	void doesNotRenderCuriesIfNoLinkIsPresent() throws Exception {

		CollectionModel<Object> resources = CollectionModel.of(Collections.emptySet());
		assertThat(getCuriedObjectMapper().writeObject(resources))
				.isEqualTo(mapper.readFileContent("empty-document.json"));
	}

	@Test
	void doesNotRenderCuriesIfNoCurieLinkIsPresent() throws Exception {

		CollectionModel<Object> resources = CollectionModel.of(Collections.emptySet());
		resources.add(Link.of("foo"));

		assertThat(getCuriedObjectMapper().writeObject(resources))
				.isEqualTo(mapper.readFileContent("single-non-curie-document.json"));
	}

	@Test
	void rendersTemplate() throws Exception {

		RepresentationModel<?> support = new RepresentationModel<>();
		support.add(Link.of("/foo{?bar}", "search"));

		assertThat(mapper.writeObject(support)).isEqualTo(mapper.readFileContent("link-template.json"));
	}

	@Test
	void rendersMultipleCuries() throws Exception {

		CollectionModel<Object> resources = CollectionModel.of(Collections.emptySet());
		resources.add(Link.of("foo", "myrel"));

		CurieProvider provider = new DefaultCurieProvider("default", UriTemplate.of("/doc{?rel}")) {
			@Override
			public Collection<?> getCurieInformation(Links links) {
				return Arrays.asList(new Curie("foo", "bar"), new Curie("bar", "foo"));
			}
		};

		assertThat(getCuriedObjectMapper(provider).writeObject(resources))
				.isEqualTo(mapper.readFileContent("multiple-curies-document.json"));
	}

	@Test
	void rendersEmptyEmbeddedCollections() throws Exception {

		EmbeddedWrappers wrappers = new EmbeddedWrappers(false);

		List<Object> values = new ArrayList<>();
		values.add(wrappers.emptyCollectionOf(SimpleAnnotatedPojo.class));

		CollectionModel<Object> resources = CollectionModel.of(values);

		assertThat(mapper.writeObject(resources)).isEqualTo(mapper.readFileContent("empty-embedded-pojos.json"));
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
		original.add(Link.of("/orders{?id}", "order"));

		String serialized = mapper.writeObject(original);

		String expected = "{\n  \"_links\" : {\n    \"order\" : {\n      \"href\" : \"/orders{?id}\",\n      \"templated\" : true\n    }\n  }\n}";

		assertThat(serialized).isEqualTo(expected);

		RepresentationModel<?> deserialized = mapper.readObject(serialized);

		assertThat(deserialized).isEqualTo(original);
	}

	@ParameterizedTest // #979
	@ValueSource(strings = { "firstname._prompt", //
			"HalFormsPayload.firstname._prompt", //
			"org.springframework.hateoas.mediatype.hal.forms.Jackson2HalFormsIntegrationTest$HalFormsPayload.firstname._prompt" })
	void usesResourceBundleToCreatePropertyPrompts(String key) {

		StaticMessageSource source = new StaticMessageSource();
		source.addMessage(key, Locale.US, "Vorname");

		Link link = Affordances.of(Link.of("some:link")) //
				.afford(HttpMethod.POST) //
				.withInput(HalFormsPayload.class) //
				.withOutput(Object.class) //
				.withName("sample") //
				.toLink();

		EntityModel<HalFormsPayload> model = EntityModel.of(new HalFormsPayload(), link);
		ContextualMapper mapper = getCuriedObjectMapper(CurieProvider.NONE, source);

		assertThatCode(() -> {

			String promptString = JsonPath.compile("$._templates.default.properties[0].prompt") //
					.read(mapper.writeObject(model));

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

		Link link = Affordances.of(Link.of("some:link")) //
				.afford(HttpMethod.POST) //
				.withInput(HalFormsPayload.class) //
				.toLink();

		EntityModel<HalFormsPayload> model = EntityModel.of(new HalFormsPayload(), link);
		ContextualMapper mapper = getCuriedObjectMapper(CurieProvider.NONE, source);

		assertThatCode(() -> {

			String promptString = JsonPath.compile("$._templates.default.title") //
					.read(mapper.writeObject(model));

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

		Link link = Affordances.of(Link.of("localhost:8080")) //
				.afford(HttpMethod.POST) //
				.withInput(Jsr303Sample.class) //
				.toLink();

		EntityModel<Jsr303Sample> model = EntityModel.of(new Jsr303Sample(), link);

		assertValueForPath(model, "$._templates.default.properties[0].readOnly", true);
		assertValueForPath(model, "$._templates.default.properties[0].regex", "[\\w\\s]");
		assertValueForPath(model, "$._templates.default.properties[0].required", true);
	}

	@Test // #968
	void considerJsonUnwrapped() throws Exception {

		UnwrappedExample unwrappedExample = new UnwrappedExample();

		unwrappedExample.element = new UnwrappedExampleElement();
		unwrappedExample.element.firstname = "john";

		assertValueForPath(unwrappedExample, "$.firstname", "john");
	}

	@ParameterizedTest // #1438
	@ValueSource(strings = { "firstname._placeholder", //
			"HalFormsPayload.firstname._placeholder", //
			"org.springframework.hateoas.mediatype.hal.forms.Jackson2HalFormsIntegrationTest$HalFormsPayload.firstname._placeholder" })
	void usesResourceBundleToCreatePropertyPlaceholder(String key) {

		StaticMessageSource source = new StaticMessageSource();
		source.addMessage(key, Locale.US, "Property placeholder");

		Link link = Affordances.of(Link.of("some:link")) //
				.afford(HttpMethod.POST) //
				.withInput(HalFormsPayload.class) //
				.toLink();

		EntityModel<HalFormsPayload> model = EntityModel.of(new HalFormsPayload(), link);
		ContextualMapper mapper = getCuriedObjectMapper(CurieProvider.NONE, source);

		assertThatCode(() -> {

			String promptString = JsonPath.compile("$._templates.default.properties[0].placeholder") //
					.read(mapper.writeObject(model));

			assertThat(promptString).isEqualTo("Property placeholder");

		}).doesNotThrowAnyException();
	}

	@Test // #1483
	void rendersPromptedOptionsValues() throws Exception {

		Inline inline = HalFormsOptions.inline(HalFormsPromptedValue.ofI18ned("some.prompt", "myValue"));

		StaticMessageSource source = new StaticMessageSource();
		source.addMessage("some.prompt", Locale.US, "My Prompt");

		ContextualMapper mapper = getCuriedObjectMapper(CurieProvider.NONE, source);

		assertThat(JsonPath.parse(mapper.writeObject(inline)).read("$.inline[0].prompt", String.class))
				.isEqualTo("My Prompt");
	}

	@Test // #1483
	void rendersRemoteOptions() {

		Link link = Link.of("/foo{?bar}").withType(MediaType.APPLICATION_JSON_VALUE);

		Remote remote = HalFormsOptions.remote(link);

		DocumentContext result = JsonPath.parse(getCuriedObjectMapper().writeObject(remote));

		assertThat(result.read("$.link.href", String.class)).isEqualTo("/foo{?bar}");
		assertThat(result.read("$.link.type", String.class)).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
		assertThat(result.read("$.link.templated", boolean.class)).isTrue();
	}

	private void assertThatPathDoesNotExist(Object toMarshall, String path) throws Exception {

		String json = getCuriedObjectMapper().writeObject(toMarshall);

		assertThatExceptionOfType(PathNotFoundException.class) //
				.isThrownBy(() -> JsonPath.compile(path).read(json));
	}

	private void assertValueForPath(Object toMarshall, String path, Object expected) throws Exception {

		String json = getCuriedObjectMapper().writeObject(toMarshall);

		Object actual = JsonPath.compile(path).read(json);

		Object value = JSONArray.class.isInstance(actual) //
				? JSONArray.class.cast(actual).get(0) //
				: actual;

		assertThat(value).isEqualTo(expected);
	}

	private void verifyResolvedTitle(String resourceBundleKey) throws Exception {

		StaticMessageSource messageSource = new StaticMessageSource();
		messageSource.addMessage(resourceBundleKey, Locale.US, "Foobar's title!");

		ContextualMapper objectMapper = getCuriedObjectMapper(CurieProvider.NONE, messageSource);

		RepresentationModel<?> resource = new RepresentationModel<>();
		resource.add(Link.of("target", "ns:foobar"));

		assertThat(objectMapper.writeObject(resource))
				.isEqualTo(objectMapper.readFileContent("link-with-title.json"));
	}

	private static CollectionModel<EntityModel<SimplePojo>> setupResources() {

		List<EntityModel<SimplePojo>> content = new ArrayList<>();
		content.add(EntityModel.of(new SimplePojo("test1", 1), Link.of("localhost")));
		content.add(EntityModel.of(new SimplePojo("test2", 2), Link.of("localhost")));

		return CollectionModel.of(content);
	}

	private static CollectionModel<EntityModel<SimpleAnnotatedPojo>> setupAnnotatedResources() {

		List<EntityModel<SimpleAnnotatedPojo>> content = new ArrayList<>();
		content.add(EntityModel.of(new SimpleAnnotatedPojo("test1", 1), Link.of("localhost")));
		content.add(EntityModel.of(new SimpleAnnotatedPojo("test2", 2), Link.of("localhost")));

		return CollectionModel.of(content);
	}

	private static CollectionModel<EntityModel<SimpleAnnotatedPojo>> setupAnnotatedPagedResources() {

		List<EntityModel<SimpleAnnotatedPojo>> content = new ArrayList<>();
		content.add(EntityModel.of(new SimpleAnnotatedPojo("test1", 1), Link.of("localhost")));
		content.add(EntityModel.of(new SimpleAnnotatedPojo("test2", 2), Link.of("localhost")));

		return PagedModel.of(content, new PagedModel.PageMetadata(2, 0, 4), PAGINATION_LINKS);
	}

	private ContextualMapper getCuriedObjectMapper() {
		return getCuriedObjectMapper(new DefaultCurieProvider("foo", UriTemplate.of("http://localhost:8080/rels/{rel}")));
	}

	private ContextualMapper getCuriedObjectMapper(CurieProvider provider) {
		return getCuriedObjectMapper(provider, null);
	}

	private ContextualMapper getCuriedObjectMapper(CurieProvider provider, @Nullable MessageSource messageSource) {

		MessageResolver resolver = MessageResolver.of(messageSource);

		DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
		HalFormsTemplateBuilder builder = new HalFormsTemplateBuilder(new HalFormsConfiguration(), resolver);
		factory.registerSingleton("foobar", new HalFormsTemplatePropertyWriter(builder));

		return MappingTestUtils.createMapper(Jackson2HalFormsIntegrationTest.class, configurer.andThen(it -> {
			it.setHandlerInstantiator(new Jackson2HalModule.HalHandlerInstantiator(this.provider, provider,
					resolver, new HalConfiguration(), factory));
		}));
	}

	@JsonAutoDetect(getterVisibility = Visibility.PUBLIC_ONLY)
	public static class HalFormsPayload {
		private @Getter String firstname;
	}

	@JsonAutoDetect(getterVisibility = Visibility.PUBLIC_ONLY)
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

	public static class UnwrappedExample extends RepresentationModel<UnwrappedExample> {

		private UnwrappedExampleElement element;

		@JsonUnwrapped
		public UnwrappedExampleElement getElement() {
			return element;
		}
	}

	@JsonAutoDetect(getterVisibility = Visibility.PUBLIC_ONLY)
	public static class UnwrappedExampleElement {
		private @Getter String firstname;
	}
}
