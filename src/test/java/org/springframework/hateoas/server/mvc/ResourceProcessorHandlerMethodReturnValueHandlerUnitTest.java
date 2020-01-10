/*
 * Copyright 2012-2020 the original author or authors.
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
package org.springframework.hateoas.server.mvc;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.util.ReflectionUtils.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.PagedModel.PageMetadata;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.hateoas.server.core.EmbeddedWrappers;
import org.springframework.hateoas.server.core.HeaderLinksResponseEntity;
import org.springframework.hateoas.server.mvc.RepresentationModelProcessorInvoker.CollectionModelProcessorWrapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;

/**
 * Unit tests for {@link RepresentationModelProcessorHandlerMethodReturnValueHandler}.
 *
 * @author Oliver Gierke
 * @author Jon Brisbin
 */
@ExtendWith(MockitoExtension.class)
class ResourceProcessorHandlerMethodReturnValueHandlerUnitTest {

	static final EntityModel<String> FOO = new EntityModel<>("foo");
	static final CollectionModel<EntityModel<String>> FOOS = new CollectionModel<>(Collections.singletonList(FOO));
	static final PagedModel<EntityModel<String>> FOO_PAGE = new PagedModel<>(singleton(FOO), new PageMetadata(1, 0, 10));
	static final StringResource FOO_RES = new StringResource("foo");
	static final HttpEntity<EntityModel<String>> FOO_ENTITY = new HttpEntity<>(FOO);
	static final ResponseEntity<EntityModel<String>> FOO_RESP_ENTITY = new ResponseEntity<>(FOO, HttpStatus.OK);
	static final HttpEntity<StringResource> FOO_RES_ENTITY = new HttpEntity<>(FOO_RES);
	static final EntityModel<String> BAR = new EntityModel<>("bar");
	static final CollectionModel<EntityModel<String>> BARS = new CollectionModel<>(Collections.singletonList(BAR));
	static final StringResource BAR_RES = new StringResource("bar");
	static final HttpEntity<EntityModel<String>> BAR_ENTITY = new HttpEntity<>(BAR);
	static final ResponseEntity<EntityModel<String>> BAR_RESP_ENTITY = new ResponseEntity<>(BAR, HttpStatus.OK);
	static final HttpEntity<StringResource> BAR_RES_ENTITY = new HttpEntity<>(BAR_RES);
	static final EntityModel<Long> LONG_10 = new EntityModel<>(10L);
	static final EntityModel<Long> LONG_20 = new EntityModel<>(20L);
	static final LongResource LONG_10_RES = new LongResource(10L);
	static final LongResource LONG_20_RES = new LongResource(20L);
	static final HttpEntity<EntityModel<Long>> LONG_10_ENTITY = new HttpEntity<>(LONG_10);
	static final HttpEntity<LongResource> LONG_10_RES_ENTITY = new HttpEntity<>(LONG_10_RES);
	static final HttpEntity<EntityModel<Long>> LONG_20_ENTITY = new HttpEntity<>(LONG_20);
	static final HttpEntity<LongResource> LONG_20_RES_ENTITY = new HttpEntity<>(LONG_20_RES);
	static final Map<String, MethodParameter> METHOD_PARAMS = new HashMap<>();

	static {
		doWithMethods(Controller.class, method -> METHOD_PARAMS.put(method.getName(), new MethodParameter(method, -1)));
	}

	@Mock HandlerMethodReturnValueHandler delegate;
	List<RepresentationModelProcessor<?>> resourceProcessors;

	@BeforeEach
	void setUp() {
		resourceProcessors = new ArrayList<>();
	}

	/**
	 * @see #362
	 */
	@Test
	void supportsIfDelegateSupports() {
		assertSupport(true);
	}

	/**
	 * @see #362
	 */
	@Test
	void doesNotSupportIfDelegateDoesNot() {
		assertSupport(false);
	}

	/**
	 * @see #362
	 */
	@Test
	void postProcessesStringResource() throws Exception {

		resourceProcessors.add(StringResourceProcessor.INSTANCE);
		resourceProcessors.add(LongResourceProcessor.INSTANCE);

		invokeReturnValueHandler("stringResourceEntity", FOO, BAR);
	}

	/**
	 * @see #362
	 */
	@Test
	void postProcessesStringResourceInResponseEntity() throws Exception {

		resourceProcessors.add(StringResourceProcessor.INSTANCE);
		resourceProcessors.add(LongResourceProcessor.INSTANCE);

		invokeReturnValueHandler("stringResourceEntity", FOO_RESP_ENTITY, BAR_RESP_ENTITY);
	}

	/**
	 * @see #362
	 */
	@Test
	void postProcessesStringResourceInWildcardResponseEntity() throws Exception {

		resourceProcessors.add(StringResourceProcessor.INSTANCE);
		resourceProcessors.add(LongResourceProcessor.INSTANCE);

		invokeReturnValueHandler("resourceEntity", FOO_RESP_ENTITY, BAR_RESP_ENTITY);
	}

	/**
	 * @see #362
	 */
	@Test
	void postProcessesStringResources() throws Exception {

		resourceProcessors.add(StringResourcesProcessor.INSTANCE);
		resourceProcessors.add(LongResourceProcessor.INSTANCE);

		invokeReturnValueHandler("resources", FOOS, BARS);
	}

	/**
	 * @see #362
	 */
	@Test
	void postProcessesSpecializedStringResource() throws Exception {

		resourceProcessors.add(SpecializedStringResourceProcessor.INSTANCE);
		resourceProcessors.add(LongResourceProcessor.INSTANCE);

		invokeReturnValueHandler("stringResourceEntity", FOO_RES_ENTITY, BAR_RES_ENTITY);
	}

	/**
	 * @see #362
	 */
	@Test
	void postProcessesSpecializedStringUsingStringResourceProcessor() throws Exception {

		resourceProcessors.add(StringResourceProcessor.INSTANCE);
		resourceProcessors.add(LongResourceProcessor.INSTANCE);

		invokeReturnValueHandler("specializedStringResourceEntity", FOO_RES_ENTITY, BAR_ENTITY);
	}

	/**
	 * @see #362
	 */
	@Test
	void postProcessesLongResource() throws Exception {

		resourceProcessors.add(StringResourceProcessor.INSTANCE);
		resourceProcessors.add(LongResourceProcessor.INSTANCE);

		invokeReturnValueHandler("longResource", LONG_10, LONG_20);
	}

	/**
	 * @see #362
	 */
	@Test
	void postProcessesSpecializedLongResource() throws Exception {

		resourceProcessors.add(StringResourceProcessor.INSTANCE);
		resourceProcessors.add(SpecializedLongResourceProcessor.INSTANCE);

		invokeReturnValueHandler("specializedLongResourceEntity", LONG_10_RES_ENTITY, LONG_20_RES_ENTITY);
	}

	/**
	 * @see #362
	 */
	@Test
	void doesNotPostProcesseLongResourceWithSpecializedLongResourceProcessor() throws Exception {

		resourceProcessors.add(StringResourceProcessor.INSTANCE);
		resourceProcessors.add(SpecializedLongResourceProcessor.INSTANCE);

		invokeReturnValueHandler("numberResourceEntity", LONG_10_ENTITY, LONG_10_ENTITY);
	}

	/**
	 * @see #362
	 */
	@Test
	void postProcessesSpecializedLongResourceUsingLongResourceProcessor() throws Exception {

		resourceProcessors.add(StringResourceProcessor.INSTANCE);
		resourceProcessors.add(LongResourceProcessor.INSTANCE);

		invokeReturnValueHandler("resourceEntity", LONG_10_RES, LONG_20);
	}

	/**
	 * @see #362
	 */
	@Test
	void usesHeaderLinksResponseEntityForResourceIfConfigured() throws Exception {
		usesHeaderLinksResponseEntityIfConfigured(Function.identity());
	}

	/**
	 * @see #362
	 */
	@Test
	void usesHeaderLinksResponseEntityIfConfigured() throws Exception {
		usesHeaderLinksResponseEntityIfConfigured(ResponseEntity::ok);
	}

	private void usesHeaderLinksResponseEntityIfConfigured(Function<Object, Object> mapper) throws Exception {

		EntityModel<String> resource = new EntityModel<>("foo", new Link("href", "rel"));
		MethodParameter parameter = METHOD_PARAMS.get("resource");

		RepresentationModelProcessorHandlerMethodReturnValueHandler handler = new RepresentationModelProcessorHandlerMethodReturnValueHandler(
				delegate, () -> new RepresentationModelProcessorInvoker(resourceProcessors));
		handler.setRootLinksAsHeaders(true);
		handler.handleReturnValue(mapper.apply(resource), parameter, null, null);

		verify(delegate, times(1)).handleReturnValue(any(HeaderLinksResponseEntity.class), eq(parameter), isNull(),
				isNull());
	}

	/**
	 * @see #362
	 */
	@Test
	void resourcesProcessorMatchesValueSubTypes() {

		ResolvableType type = ResolvableType.forClass(PagedStringResources.class);

		assertThat(CollectionModelProcessorWrapper.isValueTypeMatch(FOO_PAGE, type)).isTrue();
	}

	/**
	 * @see #362
	 */
	@Test
	void doesNotInvokeAProcessorForASpecializedType() throws Exception {

		EmbeddedWrappers wrappers = new EmbeddedWrappers(false);
		CollectionModel<Object> value = new CollectionModel<>(singleton(wrappers.emptyCollectionOf(Object.class)));
		CollectionModelProcessorWrapper wrapper = new CollectionModelProcessorWrapper(new SpecialResourcesProcessor());

		ResolvableType type = ResolvableType.forMethodReturnType(Controller.class.getMethod("resourcesOfObject"));

		assertThat(wrapper.supports(type, value)).isFalse();
	}

	/**
	 * @see #362
	 */
	@Test
	void registersProcessorForProxyType() {

		ProjectionProcessor processor = new ProjectionProcessor();
		ProxyFactory factory = new ProxyFactory(processor);

		resourceProcessors.add((RepresentationModelProcessor<?>) factory.getProxy());

		new RepresentationModelProcessorHandlerMethodReturnValueHandler(delegate,
				() -> new RepresentationModelProcessorInvoker(resourceProcessors));
	}

	/**
	 * @see #486
	 */
	@Test
	void processesElementsForWildcardedResources() throws Exception {

		resourceProcessors.add(StringResourceProcessor.INSTANCE);

		invokeReturnValueHandler("wildcardedResources", FOOS, BARS);
	}

	private void invokeReturnValueHandler(String method, Object returnValue, Object expected) throws Exception {

		MethodParameter methodParam = METHOD_PARAMS.get(method);

		if (methodParam == null) {
			throw new IllegalArgumentException("Invalid method!");
		}

		HandlerMethodReturnValueHandler handler = new RepresentationModelProcessorHandlerMethodReturnValueHandler(delegate,
				() -> new RepresentationModelProcessorInvoker(resourceProcessors));
		handler.handleReturnValue(returnValue, methodParam, null, null);

		verify(delegate, times(1)).handleReturnValue(expected, methodParam, null, null);
	}

	private void assertSupport(boolean value) {

		final MethodParameter parameter = Mockito.mock(MethodParameter.class);
		when(delegate.supportsReturnType(Mockito.any(MethodParameter.class))).thenReturn(value);

		HandlerMethodReturnValueHandler handler = new RepresentationModelProcessorHandlerMethodReturnValueHandler(delegate,
				() -> new RepresentationModelProcessorInvoker(resourceProcessors));

		assertThat(handler.supportsReturnType(parameter)).isEqualTo(value);
	}

	enum StringResourceProcessor implements RepresentationModelProcessor<EntityModel<String>> {
		INSTANCE;

		@Override
		public EntityModel<String> process(EntityModel<String> model) {
			return BAR;
		}
	}

	enum LongResourceProcessor implements RepresentationModelProcessor<EntityModel<Long>> {
		INSTANCE;

		@Override
		public EntityModel<Long> process(EntityModel<Long> model) {
			return LONG_20;
		}
	}

	enum StringResourcesProcessor implements RepresentationModelProcessor<CollectionModel<EntityModel<String>>> {
		INSTANCE;

		@Override
		public CollectionModel<EntityModel<String>> process(CollectionModel<EntityModel<String>> model) {
			return BARS;
		}
	}

	enum SpecializedStringResourceProcessor implements RepresentationModelProcessor<StringResource> {
		INSTANCE;

		@Override
		public StringResource process(StringResource model) {
			return BAR_RES;
		}
	}

	enum SpecializedLongResourceProcessor implements RepresentationModelProcessor<LongResource> {
		INSTANCE;

		@Override
		public LongResource process(LongResource model) {
			return LONG_20_RES;
		}
	}

	interface Controller {

		CollectionModel<EntityModel<String>> resources();

		EntityModel<String> resource();

		EntityModel<Long> longResource();

		StringResource specializedResource();

		Object object();

		HttpEntity<EntityModel<?>> resourceEntity();

		HttpEntity<CollectionModel<?>> resourcesEntity();

		HttpEntity<Object> objectEntity();

		HttpEntity<EntityModel<String>> stringResourceEntity();

		HttpEntity<EntityModel<? extends Number>> numberResourceEntity();

		HttpEntity<StringResource> specializedStringResourceEntity();

		HttpEntity<LongResource> specializedLongResourceEntity();

		ResponseEntity<EntityModel<?>> resourceResponseEntity();

		ResponseEntity<CollectionModel<?>> resourcesResponseEntity();

		CollectionModel<Object> resourcesOfObject();

		CollectionModel<?> wildcardedResources();
	}

	static class StringResource extends EntityModel<String> {
		public StringResource(String value) {
			super(value);
		}
	}

	static class LongResource extends EntityModel<Long> {
		public LongResource(Long value) {
			super(value);
		}
	}

	static class PagedStringResources extends PagedModel<EntityModel<String>> {}

	static class Sample {

	}

	interface SampleProjection {

	}

	static class ProjectionProcessor implements RepresentationModelProcessor<EntityModel<SampleProjection>> {

		boolean invoked = false;

		@Override
		public EntityModel<SampleProjection> process(EntityModel<SampleProjection> model) {
			this.invoked = true;
			return model;
		}
	}

	static class SpecialResources extends CollectionModel<Object> {
		public SpecialResources() {
			super(Collections.emptyList());
		}
	}

	static class SpecialResourcesProcessor implements RepresentationModelProcessor<SpecialResources> {

		boolean invoked = false;

		@Override
		public SpecialResources process(SpecialResources model) {
			this.invoked = true;
			return model;
		}
	}
}
