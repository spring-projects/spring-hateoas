/*
 * Copyright 2012-2017 the original author or authors.
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
package org.springframework.hateoas.hal;

import static org.assertj.core.api.Assertions.*;

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
import org.springframework.hateoas.AbstractJackson2MarshallingIntegrationTest;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.PagedResources.PageMetadata;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.ResourceSupportUnitTest;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.core.AnnotationRelProvider;
import org.springframework.hateoas.core.EmbeddedWrappers;
import org.springframework.hateoas.hal.HalConfiguration.RenderSingleLinks;
import org.springframework.hateoas.hal.Jackson2HalModule.HalHandlerInstantiator;
import org.springframework.hateoas.support.Author;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Integration tests for Jackson 2 HAL integration.
 * 
 * @author Alexander Baetz
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
public class Jackson2HalIntegrationTest extends AbstractJackson2MarshallingIntegrationTest {

	static final String SINGLE_LINK_REFERENCE = "{\"_links\":{\"self\":{\"href\":\"localhost\"}}}";
	static final String LIST_LINK_REFERENCE = "{\"_links\":{\"self\":[{\"href\":\"localhost\"},{\"href\":\"localhost2\"}]}}";

	static final String SIMPLE_EMBEDDED_RESOURCE_REFERENCE = "{\"_links\":{\"self\":{\"href\":\"localhost\"}},\"_embedded\":{\"content\":[\"first\",\"second\"]}}";
	static final String SINGLE_EMBEDDED_RESOURCE_REFERENCE = "{\"_links\":{\"self\":{\"href\":\"localhost\"}},\"_embedded\":{\"content\":[{\"text\":\"test1\",\"number\":1,\"_links\":{\"self\":{\"href\":\"localhost\"}}}]}}";
	static final String LIST_EMBEDDED_RESOURCE_REFERENCE = "{\"_links\":{\"self\":{\"href\":\"localhost\"}},\"_embedded\":{\"content\":[{\"text\":\"test1\",\"number\":1,\"_links\":{\"self\":{\"href\":\"localhost\"}}},{\"text\":\"test2\",\"number\":2,\"_links\":{\"self\":{\"href\":\"localhost\"}}}]}}";

	static final String ANNOTATED_EMBEDDED_RESOURCE_REFERENCE = "{\"_links\":{\"self\":{\"href\":\"localhost\"}},\"_embedded\":{\"pojos\":[{\"text\":\"test1\",\"number\":1,\"_links\":{\"self\":{\"href\":\"localhost\"}}}]}}";
	static final String ANNOTATED_EMBEDDED_RESOURCES_REFERENCE = "{\"_embedded\":{\"pojos\":[{\"text\":\"test1\",\"number\":1,\"_links\":{\"self\":{\"href\":\"localhost\"}}},{\"text\":\"test2\",\"number\":2,\"_links\":{\"self\":{\"href\":\"localhost\"}}}]}}";

	static final String ANNOTATED_PAGED_RESOURCES = "{\"_links\":{\"next\":{\"href\":\"foo\"},\"prev\":{\"href\":\"bar\"}},\"_embedded\":{\"pojos\":[{\"text\":\"test1\",\"number\":1,\"_links\":{\"self\":{\"href\":\"localhost\"}}},{\"text\":\"test2\",\"number\":2,\"_links\":{\"self\":{\"href\":\"localhost\"}}}]},\"page\":{\"size\":2,\"totalElements\":4,\"totalPages\":2,\"number\":0}}";

	static final Links PAGINATION_LINKS = new Links(new Link("foo", Link.REL_NEXT), new Link("bar", Link.REL_PREVIOUS));

	static final String CURIED_DOCUMENT = "{\"_links\":{\"self\":{\"href\":\"foo\"},\"foo:myrel\":{\"href\":\"bar\"},\"curies\":[{\"href\":\"http://localhost:8080/rels/{rel}\",\"name\":\"foo\",\"templated\":true}]}}";
	static final String MULTIPLE_CURIES_DOCUMENT = "{\"_links\":{\"default:myrel\":{\"href\":\"foo\"},\"curies\":[{\"href\":\"bar\",\"name\":\"foo\"},{\"href\":\"foo\",\"name\":\"bar\"}]}}";
	static final String SINGLE_NON_CURIE_LINK = "{\"_links\":{\"self\":{\"href\":\"foo\"}}}";
	static final String EMPTY_DOCUMENT = "{}";

	static final String LINK_TEMPLATE = "{\"_links\":{\"search\":{\"href\":\"/foo{?bar}\",\"templated\":true}}}";

	static final String LINK_WITH_TITLE = "{\"_links\":{\"ns:foobar\":{\"href\":\"target\",\"title\":\"Foobar's title!\"}}}";

	static final String SINGLE_WITH_ONE_EXTRA_ATTRIBUTES = "{\"_links\":{\"self\":{\"href\":\"localhost\",\"title\":\"the title\"}}}";
	static final String SINGLE_WITH_ALL_EXTRA_ATTRIBUTES = "{\"_links\":{\"self\":{\"href\":\"localhost\",\"hreflang\":\"en\",\"title\":\"the title\",\"type\":\"the type\",\"deprecation\":\"/customers/deprecated\"}}}";

	@Before
	public void setUpModule() {

		mapper.registerModule(new Jackson2HalModule());
		mapper.setHandlerInstantiator(
				new HalHandlerInstantiator(new AnnotationRelProvider(), null, null, new HalConfiguration()));
	}

	/**
	 * @see #29
	 */
	@Test
	public void rendersSingleLinkAsObject() throws Exception {

		ResourceSupport resourceSupport = new ResourceSupport();
		resourceSupport.add(new Link("localhost"));

		assertThat(write(resourceSupport)).isEqualTo(SINGLE_LINK_REFERENCE);
	}

	/**
	 * @see #100
	 */
	@Test
	public void rendersAllExtraRFC5988Attributes() throws Exception {

		ResourceSupport resourceSupport = new ResourceSupport();
		resourceSupport.add(new Link("localhost", "self") //
				.withHreflang("en") //
				.withTitle("the title") //
				.withType("the type") //
				.withMedia("the media") //
				.withDeprecation("/customers/deprecated"));

		assertThat(write(resourceSupport)).isEqualTo(SINGLE_WITH_ALL_EXTRA_ATTRIBUTES);
	}

	@Test
	public void rendersWithOneExtraRFC5988Attribute() throws Exception {

		ResourceSupport resourceSupport = new ResourceSupport();
		resourceSupport.add(new Link("localhost", "self").withTitle("the title"));

		assertThat(write(resourceSupport)).isEqualTo(SINGLE_WITH_ONE_EXTRA_ATTRIBUTES);
	}

	@Test
	public void rendersEmbeddedResourceSupportAsEmbedded() throws Exception {

		ResourceSupport resourceSupport = new ResourceSupport();

		Author author = new Author("Alan Watts", "January 6, 1915", "November 16, 1973");
		author.add(new Link("/people/alan-watts").withSelfRel());

		resourceSupport.addEmbeddedResource("author", author);
		resourceSupport.add(new Link("/blog-post").withSelfRel(), new Link("/people/alan-watts").withRel("author"));

		assertThat(write(resourceSupport)).isEqualTo("{\"_links\":{\"self\":{\"href\":\"/blog-post\"},\"author\":{\"href\":\"/people/alan-watts\"}},\"_embedded\":{\"author\":{\"name\":\"Alan Watts\",\"born\":\"January 6, 1915\",\"died\":\"November 16, 1973\",\"_links\":{\"self\":{\"href\":\"/people/alan-watts\"}}}}}");
	}

	@Test
	public void deserializeSingleLink() throws Exception {
		ResourceSupport expected = new ResourceSupport();
		expected.add(new Link("localhost"));
		assertThat(read(SINGLE_LINK_REFERENCE, ResourceSupport.class)).isEqualTo(expected);
	}

	/**
	 * @see #29
	 */
	@Test
	public void rendersMultipleLinkAsArray() throws Exception {

		ResourceSupport resourceSupport = new ResourceSupport();
		resourceSupport.add(new Link("localhost"));
		resourceSupport.add(new Link("localhost2"));

		assertThat(write(resourceSupport)).isEqualTo(LIST_LINK_REFERENCE);
	}

	@Test
	public void deserializeMultipleLinks() throws Exception {

		ResourceSupport expected = new ResourceSupport();
		expected.add(new Link("localhost"));
		expected.add(new Link("localhost2"));

		assertThat(read(LIST_LINK_REFERENCE, ResourceSupport.class)).isEqualTo(expected);
	}

	@Test
	public void rendersSimpleResourcesAsEmbedded() throws Exception {

		List<String> content = new ArrayList<String>();
		content.add("first");
		content.add("second");

		Resources<String> resources = new Resources<String>(content);
		resources.add(new Link("localhost"));

		assertThat(write(resources)).isEqualTo(SIMPLE_EMBEDDED_RESOURCE_REFERENCE);
	}

	@Test
	public void deserializesSimpleResourcesAsEmbedded() throws Exception {

		List<String> content = new ArrayList<String>();
		content.add("first");
		content.add("second");

		Resources<String> expected = new Resources<String>(content);
		expected.add(new Link("localhost"));

		Resources<String> result = mapper.readValue(SIMPLE_EMBEDDED_RESOURCE_REFERENCE,
				mapper.getTypeFactory().constructParametricType(Resources.class, String.class));

		assertThat(result).isEqualTo(expected);

	}

	@Test
	public void rendersSingleResourceResourcesAsEmbedded() throws Exception {

		List<Resource<SimplePojo>> content = new ArrayList<Resource<SimplePojo>>();
		content.add(new Resource<SimplePojo>(new SimplePojo("test1", 1), new Link("localhost")));

		Resources<Resource<SimplePojo>> resources = new Resources<Resource<SimplePojo>>(content);
		resources.add(new Link("localhost"));

		assertThat(write(resources)).isEqualTo(SINGLE_EMBEDDED_RESOURCE_REFERENCE);
	}

	@Test
	public void deserializesSingleResourceResourcesAsEmbedded() throws Exception {

		List<Resource<SimplePojo>> content = new ArrayList<Resource<SimplePojo>>();
		content.add(new Resource<SimplePojo>(new SimplePojo("test1", 1), new Link("localhost")));

		Resources<Resource<SimplePojo>> expected = new Resources<Resource<SimplePojo>>(content);
		expected.add(new Link("localhost"));

		Resources<Resource<SimplePojo>> result = mapper.readValue(SINGLE_EMBEDDED_RESOURCE_REFERENCE,
				mapper.getTypeFactory().constructParametricType(Resources.class,
						mapper.getTypeFactory().constructParametricType(Resource.class, SimplePojo.class)));

		assertThat(result).isEqualTo(expected);

	}

	@Test
	public void rendersMultipleResourceResourcesAsEmbedded() throws Exception {

		Resources<Resource<SimplePojo>> resources = setupResources();
		resources.add(new Link("localhost"));

		assertThat(write(resources)).isEqualTo(LIST_EMBEDDED_RESOURCE_REFERENCE);
	}

	@Test
	public void deserializesMultipleResourceResourcesAsEmbedded() throws Exception {

		Resources<Resource<SimplePojo>> expected = setupResources();
		expected.add(new Link("localhost"));

		Resources<Resource<SimplePojo>> result = mapper.readValue(LIST_EMBEDDED_RESOURCE_REFERENCE,
				mapper.getTypeFactory().constructParametricType(Resources.class,
						mapper.getTypeFactory().constructParametricType(Resource.class, SimplePojo.class)));

		assertThat(result).isEqualTo(expected);
	}

	/**
	 * @see #47, #60
	 */
	@Test
	public void serializesAnnotatedResourceResourcesAsEmbedded() throws Exception {

		List<Resource<SimpleAnnotatedPojo>> content = new ArrayList<Resource<SimpleAnnotatedPojo>>();
		content.add(new Resource<SimpleAnnotatedPojo>(new SimpleAnnotatedPojo("test1", 1), new Link("localhost")));

		Resources<Resource<SimpleAnnotatedPojo>> resources = new Resources<Resource<SimpleAnnotatedPojo>>(content);
		resources.add(new Link("localhost"));

		assertThat(write(resources)).isEqualTo(ANNOTATED_EMBEDDED_RESOURCE_REFERENCE);
	}

	/**
	 * @see #47, #60
	 */
	@Test
	public void deserializesAnnotatedResourceResourcesAsEmbedded() throws Exception {

		List<Resource<SimpleAnnotatedPojo>> content = new ArrayList<Resource<SimpleAnnotatedPojo>>();
		content.add(new Resource<SimpleAnnotatedPojo>(new SimpleAnnotatedPojo("test1", 1), new Link("localhost")));

		Resources<Resource<SimpleAnnotatedPojo>> expected = new Resources<Resource<SimpleAnnotatedPojo>>(content);
		expected.add(new Link("localhost"));

		Resources<Resource<SimpleAnnotatedPojo>> result = mapper.readValue(ANNOTATED_EMBEDDED_RESOURCE_REFERENCE,
				mapper.getTypeFactory().constructParametricType(Resources.class,
						mapper.getTypeFactory().constructParametricType(Resource.class, SimpleAnnotatedPojo.class)));

		assertThat(result).isEqualTo(expected);
	}

	/**
	 * @see #63
	 */
	@Test
	public void serializesMultipleAnnotatedResourceResourcesAsEmbedded() throws Exception {
		assertThat(write(setupAnnotatedResources())).isEqualTo(ANNOTATED_EMBEDDED_RESOURCES_REFERENCE);
	}

	/**
	 * @see #63
	 */
	@Test
	public void deserializesMultipleAnnotatedResourceResourcesAsEmbedded() throws Exception {

		Resources<Resource<SimpleAnnotatedPojo>> result = mapper.readValue(ANNOTATED_EMBEDDED_RESOURCES_REFERENCE,
				mapper.getTypeFactory().constructParametricType(Resources.class,
						mapper.getTypeFactory().constructParametricType(Resource.class, SimpleAnnotatedPojo.class)));

		assertThat(result).isEqualTo(setupAnnotatedResources());
	}

	/**
	 * @see #63
	 */
	@Test
	public void serializesPagedResource() throws Exception {
		assertThat(write(setupAnnotatedPagedResources())).isEqualTo(ANNOTATED_PAGED_RESOURCES);
	}

	/**
	 * @see #64
	 */
	@Test
	public void deserializesPagedResource() throws Exception {
		PagedResources<Resource<SimpleAnnotatedPojo>> result = mapper.readValue(ANNOTATED_PAGED_RESOURCES,
				mapper.getTypeFactory().constructParametricType(PagedResources.class,
						mapper.getTypeFactory().constructParametricType(Resource.class, SimpleAnnotatedPojo.class)));

		assertThat(result).isEqualTo(setupAnnotatedPagedResources());
	}

	/**
	 * @see #125
	 */
	@Test
	public void rendersCuriesCorrectly() throws Exception {

		Resources<Object> resources = new Resources<Object>(Collections.emptySet(), new Link("foo"),
				new Link("bar", "myrel"));

		assertThat(getCuriedObjectMapper().writeValueAsString(resources)).isEqualTo(CURIED_DOCUMENT);
	}

	/**
	 * @see #125
	 */
	@Test
	public void doesNotRenderCuriesIfNoLinkIsPresent() throws Exception {

		Resources<Object> resources = new Resources<Object>(Collections.emptySet());
		assertThat(getCuriedObjectMapper().writeValueAsString(resources)).isEqualTo(EMPTY_DOCUMENT);
	}

	/**
	 * @see #125
	 */
	@Test
	public void doesNotRenderCuriesIfNoCurieLinkIsPresent() throws Exception {

		Resources<Object> resources = new Resources<Object>(Collections.emptySet());
		resources.add(new Link("foo"));

		assertThat(getCuriedObjectMapper().writeValueAsString(resources)).isEqualTo(SINGLE_NON_CURIE_LINK);
	}

	/**
	 * @see #137
	 */
	@Test
	public void rendersTemplate() throws Exception {

		ResourceSupport support = new ResourceSupport();
		support.add(new Link("/foo{?bar}", "search"));

		assertThat(write(support)).isEqualTo(LINK_TEMPLATE);
	}

	/**
	 * @see #142
	 */
	@Test
	public void rendersMultipleCuries() throws Exception {

		Resources<Object> resources = new Resources<Object>(Collections.emptySet());
		resources.add(new Link("foo", "myrel"));

		CurieProvider provider = new DefaultCurieProvider("default", new UriTemplate("/doc{?rel}")) {
			@Override
			public Collection<? extends Object> getCurieInformation(Links links) {
				return Arrays.asList(new Curie("foo", "bar"), new Curie("bar", "foo"));
			}
		};

		assertThat(getCuriedObjectMapper(provider, null).writeValueAsString(resources)).isEqualTo(MULTIPLE_CURIES_DOCUMENT);
	}

	/**
	 * @see #286, #236
	 */
	@Test
	public void rendersEmptyEmbeddedCollections() throws Exception {

		EmbeddedWrappers wrappers = new EmbeddedWrappers(false);

		List<Object> values = new ArrayList<Object>();
		values.add(wrappers.emptyCollectionOf(SimpleAnnotatedPojo.class));

		Resources<Object> resources = new Resources<Object>(values);

		assertThat(write(resources)).isEqualTo("{\"_embedded\":{\"pojos\":[]}}");
	}

	/**
	 * @see #378
	 */
	@Test
	public void rendersTitleIfMessageSourceResolvesNamespacedKey() throws Exception {
		verifyResolvedTitle("_links.ns:foobar.title");
	}

	/**
	 * @see #378
	 */
	@Test
	public void rendersTitleIfMessageSourceResolvesLocalKey() throws Exception {
		verifyResolvedTitle("_links.foobar.title");
	}

	@Test
	public void rendersSingleLinkAsArrayWhenConfigured() throws Exception {

		mapper.setHandlerInstantiator(new HalHandlerInstantiator(new AnnotationRelProvider(), null, null,
				new HalConfiguration().withRenderSingleLinks(RenderSingleLinks.AS_ARRAY)));

		ResourceSupport resourceSupport = new ResourceSupport();
		resourceSupport.add(new Link("localhost").withSelfRel());

		assertThat(write(resourceSupport)).isEqualTo("{\"_links\":{\"self\":[{\"href\":\"localhost\"}]}}");
	}

	private static void verifyResolvedTitle(String resourceBundleKey) throws Exception {

		LocaleContextHolder.setLocale(Locale.US);

		StaticMessageSource messageSource = new StaticMessageSource();
		messageSource.addMessage(resourceBundleKey, Locale.US, "Foobar's title!");

		ObjectMapper objectMapper = getCuriedObjectMapper(null, messageSource);

		ResourceSupport resource = new ResourceSupport();
		resource.add(new Link("target", "ns:foobar"));

		assertThat(objectMapper.writeValueAsString(resource)).isEqualTo(LINK_WITH_TITLE);
	}

	private static Resources<Resource<SimpleAnnotatedPojo>> setupAnnotatedPagedResources() {

		List<Resource<SimpleAnnotatedPojo>> content = new ArrayList<Resource<SimpleAnnotatedPojo>>();
		content.add(new Resource<SimpleAnnotatedPojo>(new SimpleAnnotatedPojo("test1", 1), new Link("localhost")));
		content.add(new Resource<SimpleAnnotatedPojo>(new SimpleAnnotatedPojo("test2", 2), new Link("localhost")));

		return new PagedResources<Resource<SimpleAnnotatedPojo>>(content, new PageMetadata(2, 0, 4), PAGINATION_LINKS);
	}

	private static Resources<Resource<SimpleAnnotatedPojo>> setupAnnotatedResources() {

		List<Resource<SimpleAnnotatedPojo>> content = new ArrayList<Resource<SimpleAnnotatedPojo>>();
		content.add(new Resource<SimpleAnnotatedPojo>(new SimpleAnnotatedPojo("test1", 1), new Link("localhost")));
		content.add(new Resource<SimpleAnnotatedPojo>(new SimpleAnnotatedPojo("test2", 2), new Link("localhost")));

		return new Resources<Resource<SimpleAnnotatedPojo>>(content);
	}

	private static Resources<Resource<SimplePojo>> setupResources() {

		List<Resource<SimplePojo>> content = new ArrayList<Resource<SimplePojo>>();
		content.add(new Resource<SimplePojo>(new SimplePojo("test1", 1), new Link("localhost")));
		content.add(new Resource<SimplePojo>(new SimplePojo("test2", 2), new Link("localhost")));

		return new Resources<Resource<SimplePojo>>(content);
	}

	private static ObjectMapper getCuriedObjectMapper() {

		return getCuriedObjectMapper(new DefaultCurieProvider("foo", new UriTemplate("http://localhost:8080/rels/{rel}")),
				null);
	}

	private static ObjectMapper getCuriedObjectMapper(CurieProvider provider, MessageSource messageSource) {

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Jackson2HalModule());
		mapper.setHandlerInstantiator(new HalHandlerInstantiator(new AnnotationRelProvider(), provider,
				messageSource == null ? null : new MessageSourceAccessor(messageSource)));

		return mapper;
	}
}
