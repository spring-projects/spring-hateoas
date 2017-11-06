/*
 * Copyright 2017 the original author or authors.
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
package org.springframework.hateoas.hal.forms;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

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
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.AbstractJackson2MarshallingIntegrationTest;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.core.AnnotationRelProvider;
import org.springframework.hateoas.core.EmbeddedWrappers;
import org.springframework.hateoas.hal.CurieProvider;
import org.springframework.hateoas.hal.DefaultCurieProvider;
import org.springframework.hateoas.hal.SimpleAnnotatedPojo;
import org.springframework.hateoas.hal.SimplePojo;
import org.springframework.hateoas.hal.forms.Jackson2HalFormsModule.HalFormsHandlerInstantiator;
import org.springframework.hateoas.support.MappingUtils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author Greg Turnquist
 */
public class Jackson2HalFormsIntegrationTest extends AbstractJackson2MarshallingIntegrationTest {

	static final Links PAGINATION_LINKS = new Links(new Link("foo", Link.REL_NEXT), new Link("bar", Link.REL_PREVIOUS));

	@Before
	public void setUpModule() {

		mapper.registerModule(new Jackson2HalFormsModule());
		mapper.setHandlerInstantiator(new HalFormsHandlerInstantiator(new AnnotationRelProvider(), null, null, true, new HalFormsConfiguration()));
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
	}

	@Test
	public void rendersSingleLinkAsObject() throws Exception {

		ResourceSupport resourceSupport = new ResourceSupport();
		resourceSupport.add(new Link("localhost"));

		assertThat(write(resourceSupport),
				is(MappingUtils.read(new ClassPathResource("single-link-reference.json", getClass()))));
	}

	@Test
	public void deserializeSingleLink() throws Exception {

		ResourceSupport expected = new ResourceSupport();
		expected.add(new Link("localhost"));

		assertThat(
				read(MappingUtils.read(new ClassPathResource("single-link-reference.json", getClass())), ResourceSupport.class),
				is(expected));
	}

	@Test
	public void rendersMultipleLinkAsArray() throws Exception {

		ResourceSupport resourceSupport = new ResourceSupport();
		resourceSupport.add(new Link("localhost"));
		resourceSupport.add(new Link("localhost2"));

		assertThat(write(resourceSupport),
				is(MappingUtils.read(new ClassPathResource("list-link-reference.json", getClass()))));
	}

	@Test
	public void deserializeMultipleLinks() throws Exception {

		ResourceSupport expected = new ResourceSupport();
		expected.add(new Link("localhost"));
		expected.add(new Link("localhost2"));

		assertThat(
				read(MappingUtils.read(new ClassPathResource("list-link-reference.json", getClass())), ResourceSupport.class),
				is(expected));
	}

	@Test
	public void rendersResource() throws Exception {

		Resource<SimplePojo> resource = new Resource<>(new SimplePojo("test1", 1), new Link("localhost"));

		assertThat(write(resource),
				is(MappingUtils.read(new ClassPathResource("simple-resource-unwrapped.json", getClass()))));
	}

	@Test
	public void deserializesResource() throws IOException {

		Resource<SimplePojo> expected = new Resource<>(new SimplePojo("test1", 1), new Link("localhost"));

		Resource<SimplePojo> result = mapper.readValue(
				MappingUtils.read(new ClassPathResource("simple-resource-unwrapped.json", getClass())),
				mapper.getTypeFactory().constructParametricType(Resource.class, SimplePojo.class));

		assertThat(result, is(expected));
	}

	@Test
	public void rendersSimpleResourcesAsEmbedded() throws Exception {

		List<String> content = new ArrayList<>();
		content.add("first");
		content.add("second");

		Resources<String> resources = new Resources<>(content);
		resources.add(new Link("localhost"));

		assertThat(write(resources),
				is(MappingUtils.read(new ClassPathResource("simple-embedded-resource-reference.json", getClass()))));
	}

	@Test
	public void deserializesSimpleResourcesAsEmbedded() throws Exception {

		List<String> content = new ArrayList<>();
		content.add("first");
		content.add("second");

		Resources<String> expected = new Resources<>(content);
		expected.add(new Link("localhost"));

		Resources<String> result = mapper.readValue(
				MappingUtils.read(new ClassPathResource("simple-embedded-resource-reference.json", getClass())),
				mapper.getTypeFactory().constructParametricType(Resources.class, String.class));

		assertThat(result, is(expected));

	}

	@Test
	public void rendersSingleResourceResourcesAsEmbedded() throws Exception {

		List<Resource<SimplePojo>> content = new ArrayList<>();
		content.add(new Resource<>(new SimplePojo("test1", 1), new Link("localhost")));

		Resources<Resource<SimplePojo>> resources = new Resources<>(content);
		resources.add(new Link("localhost"));

		assertThat(write(resources),
				is(MappingUtils.read(new ClassPathResource("single-embedded-resource-reference.json", getClass()))));
	}

	@Test
	public void deserializesSingleResourceResourcesAsEmbedded() throws Exception {

		List<Resource<SimplePojo>> content = new ArrayList<>();
		content.add(new Resource<>(new SimplePojo("test1", 1), new Link("localhost")));

		Resources<Resource<SimplePojo>> expected = new Resources<>(content);
		expected.add(new Link("localhost"));

		Resources<Resource<SimplePojo>> result = mapper.readValue(
				MappingUtils.read(new ClassPathResource("single-embedded-resource-reference.json", getClass())),
				mapper.getTypeFactory().constructParametricType(Resources.class,
						mapper.getTypeFactory().constructParametricType(Resource.class, SimplePojo.class)));

		assertThat(result, is(expected));
	}

	@Test
	public void rendersMultipleResourceResourcesAsEmbedded() throws Exception {

		Resources<Resource<SimplePojo>> resources = setupResources();
		resources.add(new Link("localhost"));

		assertThat(write(resources),
				is(MappingUtils.read(new ClassPathResource("multiple-resource-resources.json", getClass()))));
	}

	@Test
	public void deserializesMultipleResourceResourcesAsEmbedded() throws Exception {

		Resources<Resource<SimplePojo>> expected = setupResources();
		expected.add(new Link("localhost"));

		Resources<Resource<SimplePojo>> result = mapper.readValue(
				MappingUtils.read(new ClassPathResource("multiple-resource-resources.json", getClass())),
				mapper.getTypeFactory().constructParametricType(Resources.class,
						mapper.getTypeFactory().constructParametricType(Resource.class, SimplePojo.class)));

		assertThat(result, is(expected));
	}

	@Test
	public void serializesAnnotatedResourceResourcesAsEmbedded() throws Exception {

		List<Resource<SimpleAnnotatedPojo>> content = new ArrayList<>();
		content.add(new Resource<>(new SimpleAnnotatedPojo("test1", 1), new Link("localhost")));

		Resources<Resource<SimpleAnnotatedPojo>> resources = new Resources<>(content);
		resources.add(new Link("localhost"));

		assertThat(write(resources),
				is(MappingUtils.read(new ClassPathResource("annotated-resource-resources.json", getClass()))));
	}

	@Test
	public void deserializesAnnotatedResourceResourcesAsEmbedded() throws Exception {

		List<Resource<SimpleAnnotatedPojo>> content = new ArrayList<>();
		content.add(new Resource<>(new SimpleAnnotatedPojo("test1", 1), new Link("localhost")));

		Resources<Resource<SimpleAnnotatedPojo>> expected = new Resources<>(content);
		expected.add(new Link("localhost"));

		Resources<Resource<SimpleAnnotatedPojo>> result = mapper.readValue(
				MappingUtils.read(new ClassPathResource("annotated-resource-resources.json", getClass())),
				mapper.getTypeFactory().constructParametricType(Resources.class,
						mapper.getTypeFactory().constructParametricType(Resource.class, SimpleAnnotatedPojo.class)));

		assertThat(result, is(expected));
	}

	@Test
	public void serializesMultipleAnnotatedResourceResourcesAsEmbedded() throws Exception {
		assertThat(write(setupAnnotatedResources()),
				is(MappingUtils.read(new ClassPathResource("annotated-embedded-resources-reference.json", getClass()))));
	}

	@Test
	public void deserializesMultipleAnnotatedResourceResourcesAsEmbedded() throws Exception {

		Resources<Resource<SimpleAnnotatedPojo>> result = mapper.readValue(
				MappingUtils.read(new ClassPathResource("annotated-embedded-resources-reference.json", getClass())),
				mapper.getTypeFactory().constructParametricType(Resources.class,
						mapper.getTypeFactory().constructParametricType(Resource.class, SimpleAnnotatedPojo.class)));

		assertThat(result, is(setupAnnotatedResources()));
	}

	@Test
	public void serializesPagedResource() throws Exception {
		assertThat(write(setupAnnotatedPagedResources()),
				is(MappingUtils.read(new ClassPathResource("annotated-paged-resources.json", getClass()))));
	}

	@Test
	public void deserializesPagedResource() throws Exception {
		PagedResources<Resource<SimpleAnnotatedPojo>> result = mapper.readValue(
				MappingUtils.read(new ClassPathResource("annotated-paged-resources.json", getClass())),
				mapper.getTypeFactory().constructParametricType(PagedResources.class,
						mapper.getTypeFactory().constructParametricType(Resource.class, SimpleAnnotatedPojo.class)));

		assertThat(result, is(setupAnnotatedPagedResources()));
	}

	@Test
	public void rendersCuriesCorrectly() throws Exception {

		Resources<Object> resources = new Resources<>(Collections.emptySet(), new Link("foo"),
			new Link("bar", "myrel"));

		assertThat(getCuriedObjectMapper().writeValueAsString(resources),
				is(MappingUtils.read(new ClassPathResource("curied-document.json", getClass()))));
	}

	@Test
	public void doesNotRenderCuriesIfNoLinkIsPresent() throws Exception {

		Resources<Object> resources = new Resources<>(Collections.emptySet());
		assertThat(getCuriedObjectMapper().writeValueAsString(resources),
				is(MappingUtils.read(new ClassPathResource("empty-document.json", getClass()))));
	}

	@Test
	public void doesNotRenderCuriesIfNoCurieLinkIsPresent() throws Exception {

		Resources<Object> resources = new Resources<>(Collections.emptySet());
		resources.add(new Link("foo"));

		assertThat(getCuriedObjectMapper().writeValueAsString(resources),
				is(MappingUtils.read(new ClassPathResource("single-non-curie-document.json", getClass()))));
	}

	@Test
	public void rendersTemplate() throws Exception {

		ResourceSupport support = new ResourceSupport();
		support.add(new Link("/foo{?bar}", "search"));

		assertThat(write(support), is(MappingUtils.read(new ClassPathResource("link-template.json", getClass()))));
	}

	@Test
	public void rendersMultipleCuries() throws Exception {

		Resources<Object> resources = new Resources<>(Collections.emptySet());
		resources.add(new Link("foo", "myrel"));

		CurieProvider provider = new DefaultCurieProvider("default", new UriTemplate("/doc{?rel}")) {
			@Override
			public Collection<? extends Object> getCurieInformation(Links links) {
				return Arrays.asList(new Curie("foo", "bar"), new Curie("bar", "foo"));
			}
		};

		assertThat(getCuriedObjectMapper(provider, null).writeValueAsString(resources),
				is(MappingUtils.read(new ClassPathResource("multiple-curies-document.json", getClass()))));
	}

	@Test
	public void rendersEmptyEmbeddedCollections() throws Exception {

		EmbeddedWrappers wrappers = new EmbeddedWrappers(false);

		List<Object> values = new ArrayList<>();
		values.add(wrappers.emptyCollectionOf(SimpleAnnotatedPojo.class));

		Resources<Object> resources = new Resources<>(values);

		assertThat(write(resources), is(MappingUtils.read(new ClassPathResource("empty-embedded-pojos.json", getClass()))));
	}

	@Test
	public void rendersTitleIfMessageSourceResolvesNamespacedKey() throws Exception {
		verifyResolvedTitle("_links.ns:foobar.title");
	}

	@Test
	public void rendersTitleIfMessageSourceResolvesLocalKey() throws Exception {
		verifyResolvedTitle("_links.foobar.title");
	}

	private void verifyResolvedTitle(String resourceBundleKey) throws Exception {

		LocaleContextHolder.setLocale(Locale.US);

		StaticMessageSource messageSource = new StaticMessageSource();
		messageSource.addMessage(resourceBundleKey, Locale.US, "Foobar's title!");

		ObjectMapper objectMapper = getCuriedObjectMapper(null, messageSource);

		ResourceSupport resource = new ResourceSupport();
		resource.add(new Link("target", "ns:foobar"));

		assertThat(objectMapper.writeValueAsString(resource),
				is(MappingUtils.read(new ClassPathResource("link-with-title.json", getClass()))));
	}

	private static Resources<Resource<SimplePojo>> setupResources() {

		List<Resource<SimplePojo>> content = new ArrayList<>();
		content.add(new Resource<>(new SimplePojo("test1", 1), new Link("localhost")));
		content.add(new Resource<>(new SimplePojo("test2", 2), new Link("localhost")));

		return new Resources<>(content);
	}

	private static Resources<Resource<SimpleAnnotatedPojo>> setupAnnotatedResources() {

		List<Resource<SimpleAnnotatedPojo>> content = new ArrayList<>();
		content.add(new Resource<>(new SimpleAnnotatedPojo("test1", 1), new Link("localhost")));
		content.add(new Resource<>(new SimpleAnnotatedPojo("test2", 2), new Link("localhost")));

		return new Resources<>(content);
	}

	private static Resources<Resource<SimpleAnnotatedPojo>> setupAnnotatedPagedResources() {

		List<Resource<SimpleAnnotatedPojo>> content = new ArrayList<>();
		content.add(new Resource<>(new SimpleAnnotatedPojo("test1", 1), new Link("localhost")));
		content.add(new Resource<>(new SimpleAnnotatedPojo("test2", 2), new Link("localhost")));

		return new PagedResources<>(content, new PagedResources.PageMetadata(2, 0, 4),
			PAGINATION_LINKS);
	}

	private static ObjectMapper getCuriedObjectMapper() {

		return getCuriedObjectMapper(new DefaultCurieProvider("foo", new UriTemplate("http://localhost:8080/rels/{rel}")),
				null);
	}

	private static ObjectMapper getCuriedObjectMapper(CurieProvider provider, MessageSource messageSource) {

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Jackson2HalFormsModule());
		mapper.setHandlerInstantiator(new HalFormsHandlerInstantiator(new AnnotationRelProvider(), provider,
				messageSource == null ? null : new MessageSourceAccessor(messageSource), true, new HalFormsConfiguration()));
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		mapper.setSerializationInclusion(Include.NON_NULL);

		return mapper;
	}
}
