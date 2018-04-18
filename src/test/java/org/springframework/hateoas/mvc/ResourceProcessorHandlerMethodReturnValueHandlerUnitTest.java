/*
 * Copyright 2012-2016 the original author or authors.
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
package org.springframework.hateoas.mvc;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.util.ReflectionUtils.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.PagedResources.PageMetadata;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.core.EmbeddedWrappers;
import org.springframework.hateoas.mvc.ResourceProcessorInvoker.ResourcesProcessorWrapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;

/**
 * Unit tests for {@link org.springframework.data.rest.webmvc.ResourceProcessorHandlerMethodReturnValueHandler}.
 * 
 * @author Oliver Gierke
 * @author Jon Brisbin
 */
@RunWith(MockitoJUnitRunner.class)
public class ResourceProcessorHandlerMethodReturnValueHandlerUnitTest {

	static final Resource<String> FOO = new Resource<>("foo");
	static final Resources<Resource<String>> FOOS = new Resources<>(Collections.singletonList(FOO));
	static final PagedResources<Resource<String>> FOO_PAGE = new PagedResources<>(
		Collections.singleton(FOO), new PageMetadata(1, 0, 10));
	static final StringResource FOO_RES = new StringResource("foo");
	static final HttpEntity<Resource<String>> FOO_ENTITY = new HttpEntity<>(FOO);
	static final ResponseEntity<Resource<String>> FOO_RESP_ENTITY = new ResponseEntity<>(FOO,
		HttpStatus.OK);
	static final HttpEntity<StringResource> FOO_RES_ENTITY = new HttpEntity<>(FOO_RES);
	static final Resource<String> BAR = new Resource<>("bar");
	static final Resources<Resource<String>> BARS = new Resources<>(Collections.singletonList(BAR));
	static final StringResource BAR_RES = new StringResource("bar");
	static final HttpEntity<Resource<String>> BAR_ENTITY = new HttpEntity<>(BAR);
	static final ResponseEntity<Resource<String>> BAR_RESP_ENTITY = new ResponseEntity<>(BAR,
		HttpStatus.OK);
	static final HttpEntity<StringResource> BAR_RES_ENTITY = new HttpEntity<>(BAR_RES);
	static final Resource<Long> LONG_10 = new Resource<>(10L);
	static final Resource<Long> LONG_20 = new Resource<>(20L);
	static final LongResource LONG_10_RES = new LongResource(10L);
	static final LongResource LONG_20_RES = new LongResource(20L);
	static final HttpEntity<Resource<Long>> LONG_10_ENTITY = new HttpEntity<>(LONG_10);
	static final HttpEntity<LongResource> LONG_10_RES_ENTITY = new HttpEntity<>(LONG_10_RES);
	static final HttpEntity<Resource<Long>> LONG_20_ENTITY = new HttpEntity<>(LONG_20);
	static final HttpEntity<LongResource> LONG_20_RES_ENTITY = new HttpEntity<>(LONG_20_RES);
	static final Map<String, MethodParameter> METHOD_PARAMS = new HashMap<>();

	static {
		doWithMethods(Controller.class, method -> METHOD_PARAMS.put(method.getName(), new MethodParameter(method, -1)));
	}

	@Mock HandlerMethodReturnValueHandler delegate;
	List<ResourceProcessor<?>> resourceProcessors;

	@Before
	public void setUp() {
		resourceProcessors = new ArrayList<>();
	}

	/**
	 * @see #362
	 */
	@Test
	public void supportsIfDelegateSupports() {
		assertSupport(true);
	}

	/**
	 * @see #362
	 */
	@Test
	public void doesNotSupportIfDelegateDoesNot() {
		assertSupport(false);
	}

	/**
	 * @see #362
	 */
	@Test
	public void postProcessesStringResource() throws Exception {

		resourceProcessors.add(StringResourceProcessor.INSTANCE);
		resourceProcessors.add(LongResourceProcessor.INSTANCE);

		invokeReturnValueHandler("stringResourceEntity", FOO, BAR);
	}

	/**
	 * @see #362
	 */
	@Test
	public void postProcessesStringResourceInResponseEntity() throws Exception {

		resourceProcessors.add(StringResourceProcessor.INSTANCE);
		resourceProcessors.add(LongResourceProcessor.INSTANCE);

		invokeReturnValueHandler("stringResourceEntity", FOO_RESP_ENTITY, BAR_RESP_ENTITY);
	}

	/**
	 * @see #362
	 */
	@Test
	public void postProcessesStringResourceInWildcardResponseEntity() throws Exception {

		resourceProcessors.add(StringResourceProcessor.INSTANCE);
		resourceProcessors.add(LongResourceProcessor.INSTANCE);

		invokeReturnValueHandler("resourceEntity", FOO_RESP_ENTITY, BAR_RESP_ENTITY);
	}

	/**
	 * @see #362
	 */
	@Test
	public void postProcessesStringResources() throws Exception {

		resourceProcessors.add(StringResourcesProcessor.INSTANCE);
		resourceProcessors.add(LongResourceProcessor.INSTANCE);

		invokeReturnValueHandler("resources", FOOS, BARS);
	}

	/**
	 * @see #362
	 */
	@Test
	public void postProcessesSpecializedStringResource() throws Exception {

		resourceProcessors.add(SpecializedStringResourceProcessor.INSTANCE);
		resourceProcessors.add(LongResourceProcessor.INSTANCE);

		invokeReturnValueHandler("stringResourceEntity", FOO_RES_ENTITY, BAR_RES_ENTITY);
	}

	/**
	 * @see #362
	 */
	@Test
	public void postProcessesSpecializedStringUsingStringResourceProcessor() throws Exception {

		resourceProcessors.add(StringResourceProcessor.INSTANCE);
		resourceProcessors.add(LongResourceProcessor.INSTANCE);

		invokeReturnValueHandler("specializedStringResourceEntity", FOO_RES_ENTITY, BAR_ENTITY);
	}

	/**
	 * @see #362
	 */
	@Test
	public void postProcessesLongResource() throws Exception {

		resourceProcessors.add(StringResourceProcessor.INSTANCE);
		resourceProcessors.add(LongResourceProcessor.INSTANCE);

		invokeReturnValueHandler("longResource", LONG_10, LONG_20);
	}

	/**
	 * @see #362
	 */
	@Test
	public void postProcessesSpecializedLongResource() throws Exception {

		resourceProcessors.add(StringResourceProcessor.INSTANCE);
		resourceProcessors.add(SpecializedLongResourceProcessor.INSTANCE);

		invokeReturnValueHandler("specializedLongResourceEntity", LONG_10_RES_ENTITY, LONG_20_RES_ENTITY);
	}

	/**
	 * @see #362
	 */
	@Test
	public void doesNotPostProcesseLongResourceWithSpecializedLongResourceProcessor() throws Exception {

		resourceProcessors.add(StringResourceProcessor.INSTANCE);
		resourceProcessors.add(SpecializedLongResourceProcessor.INSTANCE);

		invokeReturnValueHandler("numberResourceEntity", LONG_10_ENTITY, LONG_10_ENTITY);
	}

	/**
	 * @see #362
	 */
	@Test
	public void postProcessesSpecializedLongResourceUsingLongResourceProcessor() throws Exception {

		resourceProcessors.add(StringResourceProcessor.INSTANCE);
		resourceProcessors.add(LongResourceProcessor.INSTANCE);

		invokeReturnValueHandler("resourceEntity", LONG_10_RES, LONG_20);
	}

	/**
	 * @see #362
	 */
	@Test
	public void usesHeaderLinksResponseEntityForResourceIfConfigured() throws Exception {
		usesHeaderLinksResponseEntityIfConfigured(Function.identity());
	}

	/**
	 * @see #362
	 */
	@Test
	public void usesHeaderLinksResponseEntityIfConfigured() throws Exception {
		usesHeaderLinksResponseEntityIfConfigured(ResponseEntity::ok);
	}

	private void usesHeaderLinksResponseEntityIfConfigured(Function<Object, Object> mapper) throws Exception {

		Resource<String> resource = new Resource<>("foo", new Link("href", "rel"));
		MethodParameter parameter = METHOD_PARAMS.get("resource");

		ResourceProcessorHandlerMethodReturnValueHandler handler = new ResourceProcessorHandlerMethodReturnValueHandler(
				delegate, new ResourceProcessorInvoker(resourceProcessors));
		handler.setRootLinksAsHeaders(true);
		handler.handleReturnValue(mapper.apply(resource), parameter, null, null);

		verify(delegate, times(1)).handleReturnValue(any(HeaderLinksResponseEntity.class), eq(parameter), isNull(),
				isNull());
	}

	/**
	 * @see #362
	 */
	@Test
	public void resourcesProcessorMatchesValueSubTypes() {

		ResolvableType type = ResolvableType.forClass(PagedStringResources.class);

		assertThat(ResourcesProcessorWrapper.isValueTypeMatch(FOO_PAGE, type)).isTrue();
	}

	/**
	 * @see #362
	 */
	@Test
	public void doesNotInvokeAProcessorForASpecializedType() throws Exception {

		EmbeddedWrappers wrappers = new EmbeddedWrappers(false);
		Resources<Object> value = new Resources<>(
			Collections.singleton(wrappers.emptyCollectionOf(Object.class)));
		ResourcesProcessorWrapper wrapper = new ResourcesProcessorWrapper(new SpecialResourcesProcessor());

		ResolvableType type = ResolvableType.forMethodReturnType(Controller.class.getMethod("resourcesOfObject"));

		assertThat(wrapper.supports(type, value)).isFalse();
	}

	/**
	 * @see #362
	 */
	@Test
	public void registersProcessorForProxyType() {

		ProjectionProcessor processor = new ProjectionProcessor();
		ProxyFactory factory = new ProxyFactory(processor);

		resourceProcessors.add((ResourceProcessor<?>) factory.getProxy());

		new ResourceProcessorHandlerMethodReturnValueHandler(delegate, new ResourceProcessorInvoker(resourceProcessors));
	}

	/**
	 * @see #486
	 */
	@Test
	public void processesElementsForWildcardedResources() throws Exception {

		resourceProcessors.add(StringResourceProcessor.INSTANCE);

		invokeReturnValueHandler("wildcardedResources", FOOS, BARS);
	}

	private void invokeReturnValueHandler(String method, Object returnValue, Object expected) throws Exception {

		MethodParameter methodParam = METHOD_PARAMS.get(method);

		if (methodParam == null) {
			throw new IllegalArgumentException("Invalid method!");
		}

		HandlerMethodReturnValueHandler handler = new ResourceProcessorHandlerMethodReturnValueHandler(delegate,
				new ResourceProcessorInvoker(resourceProcessors));
		handler.handleReturnValue(returnValue, methodParam, null, null);

		verify(delegate, times(1)).handleReturnValue(expected, methodParam, null, null);
	}

	private void assertSupport(boolean value) {

		final MethodParameter parameter = Mockito.mock(MethodParameter.class);
		when(delegate.supportsReturnType(Mockito.any(MethodParameter.class))).thenReturn(value);

		HandlerMethodReturnValueHandler handler = new ResourceProcessorHandlerMethodReturnValueHandler(delegate,
				new ResourceProcessorInvoker(resourceProcessors));

		assertThat(handler.supportsReturnType(parameter)).isEqualTo(value);
	}

	enum StringResourceProcessor implements ResourceProcessor<Resource<String>> {
		INSTANCE;

		@Override
		public Resource<String> process(Resource<String> resource) {
			return BAR;
		}
	}

	enum LongResourceProcessor implements ResourceProcessor<Resource<Long>> {
		INSTANCE;

		@Override
		public Resource<Long> process(Resource<Long> resource) {
			return LONG_20;
		}
	}

	enum StringResourcesProcessor implements ResourceProcessor<Resources<Resource<String>>> {
		INSTANCE;

		@Override
		public Resources<Resource<String>> process(Resources<Resource<String>> resource) {
			return BARS;
		}
	}

	enum SpecializedStringResourceProcessor implements ResourceProcessor<StringResource> {
		INSTANCE;

		@Override
		public StringResource process(StringResource resource) {
			return BAR_RES;
		}
	}

	enum SpecializedLongResourceProcessor implements ResourceProcessor<LongResource> {
		INSTANCE;

		@Override
		public LongResource process(LongResource resource) {
			return LONG_20_RES;
		}
	}

	interface Controller {

		Resources<Resource<String>> resources();

		Resource<String> resource();

		Resource<Long> longResource();

		StringResource specializedResource();

		Object object();

		HttpEntity<Resource<?>> resourceEntity();

		HttpEntity<Resources<?>> resourcesEntity();

		HttpEntity<Object> objectEntity();

		HttpEntity<Resource<String>> stringResourceEntity();

		HttpEntity<Resource<? extends Number>> numberResourceEntity();

		HttpEntity<StringResource> specializedStringResourceEntity();

		HttpEntity<LongResource> specializedLongResourceEntity();

		ResponseEntity<Resource<?>> resourceResponseEntity();

		ResponseEntity<Resources<?>> resourcesResponseEntity();

		Resources<Object> resourcesOfObject();

		Resources<?> wildcardedResources();
	}

	static class StringResource extends Resource<String> {
		public StringResource(String value) {
			super(value);
		}
	}

	static class LongResource extends Resource<Long> {
		public LongResource(Long value) {
			super(value);
		}
	}

	static class PagedStringResources extends PagedResources<Resource<String>> {}

	static class Sample {

	}

	interface SampleProjection {

	}

	static class ProjectionProcessor implements ResourceProcessor<Resource<SampleProjection>> {

		boolean invoked = false;

		@Override
		public Resource<SampleProjection> process(Resource<SampleProjection> resource) {
			this.invoked = true;
			return resource;
		}
	}

	static class SpecialResources extends Resources<Object> {
		public SpecialResources() {
			super(Collections.emptyList());
		}
	}

	static class SpecialResourcesProcessor implements ResourceProcessor<SpecialResources> {

		boolean invoked = false;

		@Override
		public SpecialResources process(SpecialResources resource) {
			this.invoked = true;
			return resource;
		}
	}
}
