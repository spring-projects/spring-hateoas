/*
 * Copyright 2017-2024 the original author or authors.
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

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper.Builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.UnaryOperator;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;
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
import org.springframework.hateoas.mediatype.hal.HalJacksonModule;
import org.springframework.hateoas.mediatype.hal.HalTestUtils;
import org.springframework.hateoas.mediatype.hal.SimpleAnnotatedPojo;
import org.springframework.hateoas.mediatype.hal.SimplePojo;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsOptions.Remote;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.hateoas.server.core.AnnotationLinkRelationProvider;
import org.springframework.hateoas.server.core.DelegatingLinkRelationProvider;
import org.springframework.hateoas.server.core.EmbeddedWrappers;
import org.springframework.hateoas.support.EmployeeModel;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

/**
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 * @author Réda Housni Alaoui
 */
public class HalFormsJacksonModuleIntegrationTest {

	static final Links PAGINATION_LINKS = Links.of( //
			Link.of("foo", IanaLinkRelations.NEXT), //
			Link.of("bar", IanaLinkRelations.PREV) //
	);

	final LinkRelationProvider provider = new DelegatingLinkRelationProvider(new AnnotationLinkRelationProvider(),
			HalTestUtils.DefaultLinkRelationProvider.INSTANCE);

	final UnaryOperator<Builder> configurer = it -> it.addModule(new HalFormsJacksonModule())
			.enable(SerializationFeature.INDENT_OUTPUT);

	final ContextualMapper $ = MappingTestUtils.createMapper(configurer.andThen(it -> {

		@SuppressWarnings("resource")
		var context = new AnnotationConfigApplicationContext(HalFormsMediaTypeConfiguration.class,
				HateoasConfiguration.class);
		var instantiator = new HalJacksonModule.HalHandlerInstantiator(provider, CurieProvider.NONE,
				MessageResolver.DEFAULTS_ONLY, new HalConfiguration(), context.getAutowireCapableBeanFactory());

		return it.handlerInstantiator(instantiator);
	}));

	@BeforeEach
	void setUpModule() {
		LocaleContextHolder.setLocale(Locale.US);
	}

	@Test
	void rendersSingleLinkAsObject() {

		var model = new RepresentationModel<>()
				.add(Link.of("localhost"));

		$.assertSerializes(model)
				.intoContentOf("single-link-reference.json")
				.andBack();
	}

	@Test
	void rendersMultipleLinkAsArray() {

		RepresentationModel<?> model = new RepresentationModel<>()
				.add(Link.of("localhost"))
				.add(Link.of("localhost2"));

		$.assertSerializes(model)
				.intoContentOf("list-link-reference.json")
				.andBack();
		;
	}

	@Test
	void rendersRepresentationModelWithTemplates() {

		var link = Affordances.of(Link.of("/employees/1")) //
				.afford(HttpMethod.POST) //
				.withInputAndOutput(EmployeeModel.class) //
				.withName("foo") //
				.toLink();

		$.assertSerializes(new EmployeeModel("Frodo Baggins").add(link))
				.intoContentOf("employee-resource-support.json");
	}

	@Test
	void rendersResource() {

		$.assertSerializes(EntityModel.of(new SimplePojo("test1", 1), Link.of("localhost")))
				.intoContentOf("simple-resource-unwrapped.json")
				.andBack(SimplePojo.class);
	}

	@Test
	void rendersSimpleResourcesAsEmbedded() {

		var model = CollectionModel.of(List.of("first", "second"))
				.add(Link.of("localhost"));

		$.assertSerializes(model)
				.intoContentOf("simple-embedded-resource-reference.json")
				.andBack();
	}

	@Test
	void rendersSingleResourceResourcesAsEmbedded() {

		var content = List.of(EntityModel.of(new SimplePojo("test1", 1), Link.of("localhost")));

		$.assertSerializes(CollectionModel.of(content).add(Link.of("localhost")))
				.intoContentOf("single-embedded-resource-reference.json")
				.andBack(EntityModel.class, SimplePojo.class);
	}

	@Test
	void rendersMultipleResourceResourcesAsEmbedded() {

		$.assertSerializes(setupResources().add(Link.of("localhost")))
				.intoContentOf("multiple-resource-resources.json")
				.andBack(EntityModel.class, SimplePojo.class);
	}

	@Test
	void serializesAnnotatedResourceResourcesAsEmbedded() {

		var content = List.of(EntityModel.of(new SimpleAnnotatedPojo("test1", 1), Link.of("localhost")));

		$.assertSerializes(CollectionModel.of(content).add(Link.of("localhost")))
				.intoContentOf("annotated-resource-resources.json")
				.andBack(EntityModel.class, SimpleAnnotatedPojo.class);
	}

	@Test
	void serializesMultipleAnnotatedResourceResourcesAsEmbedded() {

		$.assertSerializes(setupAnnotatedResources())
				.intoContentOf("annotated-embedded-resources-reference.json")
				.andBack(EntityModel.class, SimpleAnnotatedPojo.class);
	}

	@Test
	void serializesPagedResource() {

		$.assertSerializes(setupAnnotatedPagedResources())
				.intoContentOf("annotated-paged-resources.json")
				.andBack(EntityModel.class, SimpleAnnotatedPojo.class);
	}

	@Test
	void rendersCuriesCorrectly() {

		getCuriedMapper()
				.assertSerializes(CollectionModel.empty().add(Link.of("foo"), Link.of("bar", "myrel")))
				.intoContentOf("curied-document.json");
	}

	@Test
	void doesNotRenderCuriesIfNoLinkIsPresent() throws Exception {

		getCuriedMapper()
				.assertSerializes(CollectionModel.empty())
				.intoContentOf("empty-document.json");
	}

	@Test
	void doesNotRenderCuriesIfNoCurieLinkIsPresent() throws Exception {

		getCuriedMapper()
				.assertSerializes(CollectionModel.empty().add(Link.of("foo")))
				.intoContentOf("single-non-curie-document.json");
	}

	@Test
	void rendersTemplate() throws Exception {

		var model = new RepresentationModel<>()
				.add(Link.of("/foo{?bar}", "search"));

		$.assertSerializes(model)
				.intoContentOf("link-template.json");
	}

	@Test
	void rendersMultipleCuries() {

		var provider = new DefaultCurieProvider("default", UriTemplate.of("/doc{?rel}")) {

			@Override
			public Collection<?> getCurieInformation(Links links) {
				return Arrays.asList(new Curie("foo", "bar"), new Curie("bar", "foo"));
			}
		};

		getCuriedMapper(provider)
				.assertSerializes(CollectionModel.empty().add(Link.of("foo", "myrel")))
				.intoContentOf("multiple-curies-document.json");
	}

	@Test
	void rendersEmptyEmbeddedCollections() {

		var wrappers = new EmbeddedWrappers(false);
		var resources = CollectionModel.of(List.of(wrappers.emptyCollectionOf(SimpleAnnotatedPojo.class)));

		$.assertSerializes(resources)
				.intoContentOf("empty-embedded-pojos.json");
	}

	@Test
	void rendersTitleIfMessageSourceResolvesNamespacedKey() {
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

		var model = new RepresentationModel<>()
				.add(Link.of("/orders{?id}", "order"));

		$.assertSerializes(model)
				.into("""
							{
								"_links" : {
									"order" : {
										"href" : "/orders{?id}",
										"templated" : true
									}
								}
							}"
						""")
				.andBack();
	}

	@ParameterizedTest // #979
	@ValueSource(strings = { "firstname._prompt", //
			"HalFormsPayload.firstname._prompt", //
			"org.springframework.hateoas.mediatype.hal.forms.HalFormsJacksonModuleIntegrationTest$HalFormsPayload.firstname._prompt" })
	void usesResourceBundleToCreatePropertyPrompts(String key) {

		var source = new StaticMessageSource();
		source.addMessage(key, Locale.US, "Vorname");

		var link = Affordances.of(Link.of("some:link")) //
				.afford(HttpMethod.POST) //
				.withInput(HalFormsPayload.class) //
				.withOutput(Object.class) //
				.withName("sample") //
				.toLink();

		getCuriedMapper(CurieProvider.NONE, source)
				.assertSerializes(EntityModel.of(new HalFormsPayload(), link))
				.into(context -> {
					assertThatNoException().isThrownBy(() -> {
						assertThat(context.read("$._templates.sample.properties[0].prompt", String.class)).isEqualTo("Vorname");
					});
				});
	}

	@ParameterizedTest // #849
	@ValueSource(strings = { //
			"_templates.postHalFormsPayload.title", //
			"HalFormsPayload._templates.postHalFormsPayload.title", //
			"org.springframework.hateoas.mediatype.hal.forms.HalFormsJacksonModuleIntegrationTest$HalFormsPayload._templates.postHalFormsPayload.title", //
			"_templates.default.title", //
			"HalFormsPayload._templates.default.title", //
			"org.springframework.hateoas.mediatype.hal.forms.HalFormsJacksonModuleIntegrationTest$HalFormsPayload._templates.default.title" })
	void usesResourceBundleToCreateTemplateTitle(String key) {

		var source = new StaticMessageSource();
		source.addMessage(key, Locale.US, "Template title");

		var link = Affordances.of(Link.of("some:link")) //
				.afford(HttpMethod.POST) //
				.withInput(HalFormsPayload.class) //
				.toLink();

		var model = EntityModel.of(new HalFormsPayload(), link);
		var mapper = getCuriedMapper(CurieProvider.NONE, source);

		assertThatCode(() -> {

			var promptString = JsonPath.compile("$._templates.postHalFormsPayload.title") //
					.read(mapper.writeObject(model));

			assertThat(promptString).isEqualTo("Template title");

		}).doesNotThrowAnyException();
	}

	@Test
	void doesNotRenderPromptPropertyIfEmpty() throws Exception {

		var property = HalFormsProperty.named("someName");

		assertThatPathDoesNotExist(property.withPrompt(""), "$.prompt");
		assertValueForPath(property.withPrompt("Some prompt"), "$.prompt", "Some prompt");
	}

	@Test
	void doesNotRenderRequiredPropertyIfFalse() throws Exception {

		var property = HalFormsProperty.named("someName");

		assertThatPathDoesNotExist(property, "$.required");
		assertValueForPath(property.withRequired(true), ".required", true);
	}

	@Test
	void considersJsr303AnnotationsForTemplates() throws Exception {

		var link = Affordances.of(Link.of("localhost:8080")) //
				.afford(HttpMethod.POST) //
				.withInput(Jsr303Sample.class) //
				.toLink();

		var model = EntityModel.of(new Jsr303Sample(), link);

		assertValueForPath(model, "$._templates.postJsr303Sample.properties[0].readOnly", true);
		assertValueForPath(model, "$._templates.postJsr303Sample.properties[0].regex", "[\\w\\s]");
		assertValueForPath(model, "$._templates.postJsr303Sample.properties[0].required", true);
	}

	@Test // #968
	void considerJsonUnwrapped() throws Exception {

		var unwrappedExample = new UnwrappedExample();

		unwrappedExample.element = new UnwrappedExampleElement();
		unwrappedExample.element.firstname = "john";

		assertValueForPath(unwrappedExample, "$.firstname", "john");
	}

	@ParameterizedTest // #1438
	@ValueSource(strings = { "firstname._placeholder", //
			"HalFormsPayload.firstname._placeholder", //
			"org.springframework.hateoas.mediatype.hal.forms.HalFormsJacksonModuleIntegrationTest$HalFormsPayload.firstname._placeholder" })
	void usesResourceBundleToCreatePropertyPlaceholder(String key) {

		StaticMessageSource source = new StaticMessageSource();
		source.addMessage(key, Locale.US, "Property placeholder");

		Link link = Affordances.of(Link.of("some:link")) //
				.afford(HttpMethod.POST) //
				.withInput(HalFormsPayload.class) //
				.toLink();

		getCuriedMapper(CurieProvider.NONE, source)
				.assertSerializes(EntityModel.of(new HalFormsPayload(), link))
				.into(result -> {
					assertThat(result.read("$._templates.postHalFormsPayload.properties[0].placeholder", String.class))
							.isEqualTo("Property placeholder");
				});

	}

	@Test // #1483
	void rendersPromptedOptionsValues() throws Exception {

		var inline = HalFormsOptions.inline(HalFormsPromptedValue.ofI18ned("some.prompt", "myValue"));

		var source = new StaticMessageSource();
		source.addMessage("some.prompt", Locale.US, "My Prompt");

		getCuriedMapper(CurieProvider.NONE, source)
				.assertSerializes(inline)
				.into(result -> {
					assertThat(result.read("$.inline[0].prompt", String.class)).isEqualTo("My Prompt");
				});
	}

	@Test // #1483
	void rendersRemoteOptions() {

		Link link = Link.of("/foo{?bar}").withType(MediaType.APPLICATION_JSON_VALUE);

		Remote remote = HalFormsOptions.remote(link);

		getCuriedMapper()
				.assertSerializes(remote)
				.into(result -> {
					assertThat(result.read("$.link.href", String.class)).isEqualTo("/foo{?bar}");
					assertThat(result.read("$.link.type", String.class)).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
					assertThat(result.read("$.link.templated", boolean.class)).isTrue();
				});

	}

	@Test // #2257
	void rendersFullInlineOptions() {

		var options = HalFormsOptions.inline(Map.of("my-prompt-field", "foo", "my-value-field", "bar"))
				.withPromptField("my-prompt-field")
				.withValueField("my-value-field")
				.withMinItems(2L)
				.withMaxItems(3L)
				.withSelectedValues(List.of("bar", "baz"));

		getCuriedMapper()
				.assertSerializes(options)
				.into(result -> {

					assertThat(result.read("$.inline[0].my-prompt-field", String.class)).isEqualTo("foo");
					assertThat(result.read("$.inline[0].my-value-field", String.class)).isEqualTo("bar");
					assertThat(result.read("$.promptField", String.class)).isEqualTo("my-prompt-field");
					assertThat(result.read("$.valueField", String.class)).isEqualTo("my-value-field");
					assertThat(result.read("$.minItems", Long.class)).isEqualTo(2L);
					assertThat(result.read("$.maxItems", Long.class)).isEqualTo(3L);
					assertThat(result.read("$.selectedValues", List.class)).isEqualTo(List.of("bar", "baz"));
				});
	}

	@Test // #2257
	void rendersFullRemoteOptions() {

		var link = Link.of("/foo{?bar}").withType(MediaType.APPLICATION_JSON_VALUE);

		var options = HalFormsOptions.remote(link)
				.withPromptField("my-prompt-field")
				.withValueField("my-value-field")
				.withMinItems(2L)
				.withMaxItems(3L)
				.withSelectedValues(List.of("bar", "baz"));

		getCuriedMapper()
				.assertSerializes(options)
				.into(result -> {

					assertThat(result.read("$.link.href", String.class)).isEqualTo("/foo{?bar}");
					assertThat(result.read("$.link.type", String.class)).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
					assertThat(result.read("$.link.templated", boolean.class)).isTrue();
					assertThat(result.read("$.promptField", String.class)).isEqualTo("my-prompt-field");
					assertThat(result.read("$.valueField", String.class)).isEqualTo("my-value-field");
					assertThat(result.read("$.minItems", Long.class)).isEqualTo(2L);
					assertThat(result.read("$.maxItems", Long.class)).isEqualTo(3L);
					assertThat(result.read("$.selectedValues", List.class)).isEqualTo(List.of("bar", "baz"));
				});

	}

	private void assertThatPathDoesNotExist(Object toMarshall, String path) {

		var json = getCuriedMapper().writeObject(toMarshall);

		assertThatExceptionOfType(PathNotFoundException.class) //
				.isThrownBy(() -> JsonPath.compile(path).read(json));
	}

	private void assertValueForPath(Object toMarshall, String path, Object expected) throws Exception {

		var json = getCuriedMapper().writeObject(toMarshall);
		var actual = JsonPath.compile(path).read(json);

		var value = List.class.isInstance(actual) //
				? List.class.cast(actual).get(0) //
				: actual;

		assertThat(value).isEqualTo(expected);
	}

	private void verifyResolvedTitle(String resourceBundleKey) {

		var messageSource = new StaticMessageSource();
		messageSource.addMessage(resourceBundleKey, Locale.US, "Foobar's title!");

		var $ = getCuriedMapper(CurieProvider.NONE, messageSource);

		var resource = new RepresentationModel<>()
				.add(Link.of("target", "ns:foobar"));

		$.assertSerializes(resource)
				.intoContentOf("link-with-title.json");
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

	private ContextualMapper getCuriedMapper() {
		return getCuriedMapper(new DefaultCurieProvider("foo", UriTemplate.of("http://localhost:8080/rels/{rel}")));
	}

	private ContextualMapper getCuriedMapper(CurieProvider provider) {
		return getCuriedMapper(provider, null);
	}

	private ContextualMapper getCuriedMapper(CurieProvider provider, @Nullable MessageSource messageSource) {

		MessageResolver resolver = MessageResolver.of(messageSource);

		DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
		HalFormsTemplateBuilder builder = new HalFormsTemplateBuilder(new HalFormsConfiguration(), resolver);
		factory.registerSingleton("foobar", new HalFormsTemplatePropertyWriter(builder));

		return MappingTestUtils.createMapper(configurer.andThen(it -> {
			return it.handlerInstantiator(new HalJacksonModule.HalHandlerInstantiator(this.provider, provider,
					resolver, new HalConfiguration(), factory));
		}));
	}

	@JsonAutoDetect(getterVisibility = Visibility.PUBLIC_ONLY)
	public static class HalFormsPayload {
		private @Nullable @Getter String firstname;
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
