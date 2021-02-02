/*
 * Copyright 2012-2021 the original author or authors.
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Supplier;

import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.hateoas.server.core.HeaderLinksResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * {@link HandlerMethodReturnValueHandler} to post-process the objects returned from controller methods using the
 * configured {@link RepresentationModelProcessor}s.
 *
 * @author Oliver Gierke
 * @since 0.20
 * @soundtrack Doppelkopf - Balance (Von Abseits)
 */
public class RepresentationModelProcessorHandlerMethodReturnValueHandler implements HandlerMethodReturnValueHandler {

	static final ResolvableType ENTITY_MODEL_TYPE = ResolvableType.forRawClass(EntityModel.class);
	static final ResolvableType COLLECTION_MODEL_TYPE = ResolvableType.forRawClass(CollectionModel.class);
	private static final ResolvableType HTTP_ENTITY_TYPE = ResolvableType.forRawClass(HttpEntity.class);

	static final Field CONTENT_FIELD = ReflectionUtils.findField(CollectionModel.class, "content");

	static {
		if (CONTENT_FIELD != null) {
			ReflectionUtils.makeAccessible(CONTENT_FIELD);
		}
	}

	private final HandlerMethodReturnValueHandler delegate;
	private final Supplier<RepresentationModelProcessorInvoker> invoker;

	private boolean rootLinksAsHeaders = false;

	public RepresentationModelProcessorHandlerMethodReturnValueHandler(HandlerMethodReturnValueHandler delegate,
			Supplier<RepresentationModelProcessorInvoker> invoker) {

		Assert.notNull(delegate, "delegate must not be null!");
		Assert.notNull(invoker, "invoker must not be null!");

		this.delegate = delegate;
		this.invoker = invoker;
	}

	/**
	 * @param rootLinksAsHeaders the rootLinksAsHeaders to set
	 */
	public void setRootLinksAsHeaders(boolean rootLinksAsHeaders) {
		this.rootLinksAsHeaders = rootLinksAsHeaders;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.web.method.support.HandlerMethodReturnValueHandler#supportsReturnType(org.springframework.core.MethodParameter)
	 */
	@Override
	public boolean supportsReturnType(MethodParameter returnType) {
		return delegate.supportsReturnType(returnType);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.web.method.support.HandlerMethodReturnValueHandler#handleReturnValue(java.lang.Object, org.springframework.core.MethodParameter, org.springframework.web.method.support.ModelAndViewContainer, org.springframework.web.context.request.NativeWebRequest)
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void handleReturnValue(@Nullable Object returnValue, MethodParameter returnType,
			ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {

		Object value = returnValue;

		if (returnValue instanceof HttpEntity) {
			value = ((HttpEntity<?>) returnValue).getBody();
		}

		// No post-processable type found - proceed with delegate
		if (!(value instanceof RepresentationModel)) {
			delegate.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
			return;
		}

		Method method = returnType.getMethod();

		if (method == null) {
			throw new IllegalStateException(String.format("Return type %s does not expose a method!", returnType));
		}

		// We have a Resource or Resources - find suitable processors
		ResolvableType targetType = ResolvableType.forMethodReturnType(method);

		// Unbox HttpEntity
		if (HTTP_ENTITY_TYPE.isAssignableFrom(targetType)) {
			targetType = targetType.getGeneric(0);
		}

		ResolvableType returnValueType = ResolvableType.forClass(value.getClass());

		// Returned value is actually of a more specific type, use this type information
		if (!getRawType(targetType).equals(getRawType(returnValueType))) {
			targetType = returnValueType;
		}

		RepresentationModel<?> result = invoker.get().invokeProcessorsFor((RepresentationModel) value, targetType);
		delegate.handleReturnValue(rewrapResult(result, returnValue), returnType, mavContainer, webRequest);
	}

	/**
	 * Re-wraps the result of the post-processing work into an {@link HttpEntity} or {@link ResponseEntity} if the
	 * original value was one of those two types. Copies headers and status code from the original value but uses the new
	 * body.
	 *
	 * @param newBody the post-processed value.
	 * @param originalValue the original input value.
	 * @return
	 */
	Object rewrapResult(RepresentationModel<?> newBody, @Nullable Object originalValue) {

		if (!(originalValue instanceof HttpEntity)) {
			return rootLinksAsHeaders ? HeaderLinksResponseEntity.wrap(newBody) : newBody;
		}

		HttpEntity<RepresentationModel<?>> entity;

		if (originalValue instanceof ResponseEntity) {
			ResponseEntity<?> source = (ResponseEntity<?>) originalValue;
			entity = new ResponseEntity<>(newBody, source.getHeaders(), source.getStatusCode());
		} else {
			HttpEntity<?> source = (HttpEntity<?>) originalValue;
			entity = new HttpEntity<>(newBody, source.getHeaders());
		}

		return rootLinksAsHeaders ? HeaderLinksResponseEntity.wrap(entity) : entity;
	}

	private static Class<?> getRawType(ResolvableType type) {

		Class<?> rawType = type.getRawClass();
		return rawType == null ? Object.class : rawType;
	}
}
