/*
 * Copyright 2017-2019 the original author or authors.
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
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.AbstractJackson2MarshallingIntegrationTest;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.UriTemplate;
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

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
public class Jackson2HalFormsIntegrationTest extends AbstractJackson2MarshallingIntegrationTest {

	static final Links PAGINATION_LINKS = Links.of( //
			new Link("foo", IanaLinkRelations.NEXT), //
			new Link("bar", IanaLinkRelations.PREV) //
	);

	MessageSource messageSource = mock(MessageSource.class);

	@Before
	public void setUpModule() {

		LinkRelationProvider provider = new DelegatingLinkRelationProvider(new AnnotationLinkRelationProvider(),
				Jackson2HalIntegrationTest.DefaultLinkRelationProvider.INSTANCE);

		mapper.registerModule(new Jackson2HalFormsModule());
		mapper.setHandlerInstantiator(new HalFormsHandlerInstantiator( //
				provider, CurieProvider.NONE, new MessageSourceAccessor(messageSource), true, new HalFormsConfiguration()));
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
	}

	@Test
	public void rendersSingleLinkAsObject() throws Exception {

		RepresentationModel<?> resourceSupport = new RepresentationModel<>();
		resourceSupport.add(new Link("localhost"));

		assertThat(write(resourceSupport))
				.isEqualTo(MappingUtils.read(new ClassPathResource("single-link-reference.json", getClass())));
	}

	@Test
	public void deserializeSingleLink() throws Exception {

		RepresentationModel<?> expected = new RepresentationModel<>();
		expected.add(new Link("localhost"));

		assertThat(read(MappingUtils.read(new ClassPathResource("single-link-reference.json", getClass())),
				RepresentationModel.class)).isEqualTo(expected);
	}

	@Test
	public void rendersMultipleLinkAsArray() throws Exception {

		RepresentationModel<?> resourceSupport = new RepresentationModel<>();
		resourceSupport.add(new Link("localhost"));
		resourceSupport.add(new Link("localhost2"));

		assertThat(write(resourceSupport))
				.isEqualTo(MappingUtils.read(new ClassPathResource("list-link-reference.json", getClass())));
	}

	@Test
	public void deserializeMultipleLinks() throws Exception {

		RepresentationModel<?> expected = new RepresentationModel<>();
		expected.add(new Link("localhost"));
		expected.add(new Link("localhost2"));

		assertThat(read(MappingUtils.read(new ClassPathResource("list-link-reference.json", getClass())),
				RepresentationModel.class)).isEqualTo(expected);
	}

	@Test
	public void rendersResourceSupportWithTemplates() throws Exception {

		EmployeeResource resource = new EmployeeResource("Frodo Baggins");
		Link selfLink = new Link("/employees/1");
		selfLink = selfLink.andAffordance(new Affordance("foo", selfLink, HttpMethod.POST,
				ResolvableType.forClass(EmployeeResource.class), Collections.emptyList(),
				ResolvableType.forClass(EmployeeResource.class)));
		resource.add(selfLink);

		assertThat(write(resource))
				.isEqualTo(MappingUtils.read(new ClassPathResource("employee-resource-support.json", getClass())));
	}

	@Test
	public void rendersResource() throws Exception {

		EntityModel<SimplePojo> resource = new EntityModel<>(new SimplePojo("test1", 1), new Link("localhost"));

		assertThat(write(resource))
				.isEqualTo(MappingUtils.read(new ClassPathResource("simple-resource-unwrapped.json", getClass())));
	}

	@Test
	public void deserializesResource() throws IOException {

		EntityModel<SimplePojo> expected = new EntityModel<>(new SimplePojo("test1", 1), new Link("localhost"));

		EntityModel<SimplePojo> result = mapper.readValue(
				MappingUtils.read(new ClassPathResource("simple-resource-unwrapped.json", getClass())),
				mapper.getTypeFactory().constructParametricType(EntityModel.class, SimplePojo.class));

		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void rendersSimpleResourcesAsEmbedded() throws Exception {

		List<String> content = new ArrayList<>();
		content.add("first");
		content.add("second");

		CollectionModel<String> resources = new CollectionModel<>(content);
		resources.add(new Link("localhost"));

		assertThat(write(resources))
				.isEqualTo(MappingUtils.read(new ClassPathResource("simple-embedded-resource-reference.json", getClass())));
	}

	@Test
	public void deserializesSimpleResourcesAsEmbedded() throws Exception {

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
	public void rendersSingleResourceResourcesAsEmbedded() throws Exception {

		List<EntityModel<SimplePojo>> content = new ArrayList<>();
		content.add(new EntityModel<>(new SimplePojo("test1", 1), new Link("localhost")));

		CollectionModel<EntityModel<SimplePojo>> resources = new CollectionModel<>(content);
		resources.add(new Link("localhost"));

		assertThat(write(resources))
				.isEqualTo(MappingUtils.read(new ClassPathResource("single-embedded-resource-reference.json", getClass())));
	}

	@Test
	public void deserializesSingleResourceResourcesAsEmbedded() throws Exception {

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
	public void rendersMultipleResourceResourcesAsEmbedded() throws Exception {

		CollectionModel<EntityModel<SimplePojo>> resources = setupResources();
		resources.add(new Link("localhost"));

		assertThat(write(resources))
				.isEqualTo(MappingUtils.read(new ClassPathResource("multiple-resource-resources.json", getClass())));
	}

	@Test
	public void deserializesMultipleResourceResourcesAsEmbedded() throws Exception {

		CollectionModel<EntityModel<SimplePojo>> expected = setupResources();
		expected.add(new Link("localhost"));

		CollectionModel<EntityModel<SimplePojo>> result = mapper.readValue(
				MappingUtils.read(new ClassPathResource("multiple-resource-resources.json", getClass())),
				mapper.getTypeFactory().constructParametricType(CollectionModel.class,
						mapper.getTypeFactory().constructParametricType(EntityModel.class, SimplePojo.class)));

		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void serializesAnnotatedResourceResourcesAsEmbedded() throws Exception {

		List<EntityModel<SimpleAnnotatedPojo>> content = new ArrayList<>();
		content.add(new EntityModel<>(new SimpleAnnotatedPojo("test1", 1), new Link("localhost")));

		CollectionModel<EntityModel<SimpleAnnotatedPojo>> resources = new CollectionModel<>(content);
		resources.add(new Link("localhost"));

		assertThat(write(resources))
				.isEqualTo(MappingUtils.read(new ClassPathResource("annotated-resource-resources.json", getClass())));
	}

	@Test
	public void deserializesAnnotatedResourceResourcesAsEmbedded() throws Exception {

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
	public void serializesMultipleAnnotatedResourceResourcesAsEmbedded() throws Exception {
		assertThat(write(setupAnnotatedResources()))
				.isEqualTo(MappingUtils.read(new ClassPathResource("annotated-embedded-resources-reference.json", getClass())));
	}

	@Test
	public void deserializesMultipleAnnotatedResourceResourcesAsEmbedded() throws Exception {

		CollectionModel<EntityModel<SimpleAnnotatedPojo>> result = mapper.readValue(
				MappingUtils.read(new ClassPathResource("annotated-embedded-resources-reference.json", getClass())),
				mapper.getTypeFactory().constructParametricType(CollectionModel.class,
						mapper.getTypeFactory().constructParametricType(EntityModel.class, SimpleAnnotatedPojo.class)));

		assertThat(result).isEqualTo(setupAnnotatedResources());
	}

	@Test
	public void serializesPagedResource() throws Exception {
		assertThat(write(setupAnnotatedPagedResources()))
				.isEqualTo(MappingUtils.read(new ClassPathResource("annotated-paged-resources.json", getClass())));
	}

	@Test
	public void deserializesPagedResource() throws Exception {
		PagedModel<EntityModel<SimpleAnnotatedPojo>> result = mapper.readValue(
				MappingUtils.read(new ClassPathResource("annotated-paged-resources.json", getClass())),
				mapper.getTypeFactory().constructParametricType(PagedModel.class,
						mapper.getTypeFactory().constructParametricType(EntityModel.class, SimpleAnnotatedPojo.class)));

		assertThat(result).isEqualTo(setupAnnotatedPagedResources());
	}

	@Test
	public void rendersCuriesCorrectly() throws Exception {

		CollectionModel<Object> resources = new CollectionModel<>(Collections.emptySet(), new Link("foo"),
				new Link("bar", "myrel"));

		assertThat(getCuriedObjectMapper().writeValueAsString(resources))
				.isEqualTo(MappingUtils.read(new ClassPathResource("curied-document.json", getClass())));
	}

	@Test
	public void doesNotRenderCuriesIfNoLinkIsPresent() throws Exception {

		CollectionModel<Object> resources = new CollectionModel<>(Collections.emptySet());
		assertThat(getCuriedObjectMapper().writeValueAsString(resources))
				.isEqualTo(MappingUtils.read(new ClassPathResource("empty-document.json", getClass())));
	}

	@Test
	public void doesNotRenderCuriesIfNoCurieLinkIsPresent() throws Exception {

		CollectionModel<Object> resources = new CollectionModel<>(Collections.emptySet());
		resources.add(new Link("foo"));

		assertThat(getCuriedObjectMapper().writeValueAsString(resources))
				.isEqualTo(MappingUtils.read(new ClassPathResource("single-non-curie-document.json", getClass())));
	}

	@Test
	public void rendersTemplate() throws Exception {

		RepresentationModel<?> support = new RepresentationModel<>();
		support.add(new Link("/foo{?bar}", "search"));

		assertThat(write(support)).isEqualTo(MappingUtils.read(new ClassPathResource("link-template.json", getClass())));
	}

	@Test
	public void rendersMultipleCuries() throws Exception {

		CollectionModel<Object> resources = new CollectionModel<>(Collections.emptySet());
		resources.add(new Link("foo", "myrel"));

		CurieProvider provider = new DefaultCurieProvider("default", new UriTemplate("/doc{?rel}")) {
			@Override
			public Collection<?> getCurieInformation(Links links) {
				return Arrays.asList(new Curie("foo", "bar"), new Curie("bar", "foo"));
			}
		};

		assertThat(getCuriedObjectMapper(provider, messageSource).writeValueAsString(resources))
				.isEqualTo(MappingUtils.read(new ClassPathResource("multiple-curies-document.json", getClass())));
	}

	@Test
	public void rendersEmptyEmbeddedCollections() throws Exception {

		EmbeddedWrappers wrappers = new EmbeddedWrappers(false);

		List<Object> values = new ArrayList<>();
		values.add(wrappers.emptyCollectionOf(SimpleAnnotatedPojo.class));

		CollectionModel<Object> resources = new CollectionModel<>(values);

		assertThat(write(resources))
				.isEqualTo(MappingUtils.read(new ClassPathResource("empty-embedded-pojos.json", getClass())));
	}

	@Test
	public void rendersTitleIfMessageSourceResolvesNamespacedKey() throws Exception {
		verifyResolvedTitle("_links.ns:foobar.title");
	}

	@Test
	public void rendersTitleIfMessageSourceResolvesLocalKey() throws Exception {
		verifyResolvedTitle("_links.foobar.title");
	}

	/**
	 * @see #667
	 */
	@Test
	public void handleTemplatedLinksOnDeserialization() throws IOException {

		RepresentationModel<?> original = new RepresentationModel<>();
		original.add(new Link("/orders{?id}", "order"));

		String serialized = mapper.writeValueAsString(original);

		String expected = "{\n  \"_links\" : {\n    \"order\" : {\n      \"href\" : \"/orders{?id}\",\n      \"templated\" : true\n    }\n  }\n}";

		assertThat(serialized).isEqualTo(expected);

		RepresentationModel<?> deserialized = mapper.readValue(serialized, RepresentationModel.class);

		assertThat(deserialized).isEqualTo(original);
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

		return getCuriedObjectMapper(new DefaultCurieProvider("foo", new UriTemplate("http://localhost:8080/rels/{rel}")),
				messageSource);
	}

	private ObjectMapper getCuriedObjectMapper(CurieProvider provider, MessageSource messageSource) {

		ObjectMapper mapper = new ObjectMapper();

		mapper.registerModule(new Jackson2HalFormsModule());
		mapper.setHandlerInstantiator(new HalFormsHandlerInstantiator(new AnnotationLinkRelationProvider(), provider,
				messageSource == null ? null : new MessageSourceAccessor(messageSource), true, new HalFormsConfiguration()));
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		mapper.setSerializationInclusion(Include.NON_NULL);

		return mapper;
	}
}
