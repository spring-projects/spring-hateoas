/*
 * Copyright 2021 the original author or authors.
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
package org.springframework.hateoas.mediatype;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.ser.VirtualBeanPropertyWriter;

/**
 * A {@link HandlerInstantiator} that will use instances explicitly registered with it but fall back to lookup or even
 * create a fresh instance via the {@link AutowireCapableBeanFactory} provided on construction.
 *
 * @author Oliver Drotbohm
 */
@SuppressWarnings("null")
public class ConfigurableHandlerInstantiator extends HandlerInstantiator {

	private final Map<Class<?>, Object> instances = new HashMap<>();
	private final AutowireCapableBeanFactory beanFactory;

	/**
	 * Creates a new {@link ConfigurableHandlerInstantiator} for the given {@link AutowireCapableBeanFactory}.
	 *
	 * @param beanFactory must not be {@literal null}.
	 */
	protected ConfigurableHandlerInstantiator(AutowireCapableBeanFactory beanFactory) {

		Assert.notNull(beanFactory, "BeanFactory must not be null!");

		this.beanFactory = beanFactory;
	}

	protected void registerInstance(Object instance) {
		this.instances.put(instance.getClass(), instance);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	protected <T> T findInstance(Class<T> type) {
		return (T) this.instances.get(type);
	}

	@SuppressWarnings("unchecked")
	protected <T> T findOrCreateInstance(Class<T> type) {

		Object object = findInstance(type);

		return object != null
				? (T) object
				: beanFactory.getBeanProvider(type)
						.getIfAvailable(() -> beanFactory.createBean(type));
	}

	/*
	 * (non-Javadoc)
	 * @see com.fasterxml.jackson.databind.cfg.HandlerInstantiator#deserializerInstance(com.fasterxml.jackson.databind.DeserializationConfig, com.fasterxml.jackson.databind.introspect.Annotated, java.lang.Class)
	 */
	@Override
	public JsonDeserializer<?> deserializerInstance(DeserializationConfig config, Annotated annotated,
			Class<?> deserClass) {
		return (JsonDeserializer<?>) findOrCreateInstance(deserClass);
	}

	/*
	 * (non-Javadoc)
	 * @see com.fasterxml.jackson.databind.cfg.HandlerInstantiator#keyDeserializerInstance(com.fasterxml.jackson.databind.DeserializationConfig, com.fasterxml.jackson.databind.introspect.Annotated, java.lang.Class)
	 */
	@Override
	public KeyDeserializer keyDeserializerInstance(DeserializationConfig config, Annotated annotated,
			Class<?> keyDeserClass) {
		return (KeyDeserializer) findOrCreateInstance(keyDeserClass);
	}

	/*
	 * (non-Javadoc)
	 * @see com.fasterxml.jackson.databind.cfg.HandlerInstantiator#serializerInstance(com.fasterxml.jackson.databind.SerializationConfig, com.fasterxml.jackson.databind.introspect.Annotated, java.lang.Class)
	 */
	@Override
	public JsonSerializer<?> serializerInstance(SerializationConfig config, Annotated annotated, Class<?> serClass) {
		return (JsonSerializer<?>) findOrCreateInstance(serClass);
	}

	/*
	 * (non-Javadoc)
	 * @see com.fasterxml.jackson.databind.cfg.HandlerInstantiator#typeResolverBuilderInstance(com.fasterxml.jackson.databind.cfg.MapperConfig, com.fasterxml.jackson.databind.introspect.Annotated, java.lang.Class)
	 */
	@Override
	public TypeResolverBuilder<?> typeResolverBuilderInstance(MapperConfig<?> config, Annotated annotated,
			Class<?> builderClass) {
		return (TypeResolverBuilder<?>) findOrCreateInstance(builderClass);
	}

	/*
	 * (non-Javadoc)
	 * @see com.fasterxml.jackson.databind.cfg.HandlerInstantiator#typeIdResolverInstance(com.fasterxml.jackson.databind.cfg.MapperConfig, com.fasterxml.jackson.databind.introspect.Annotated, java.lang.Class)
	 */
	@Override
	public TypeIdResolver typeIdResolverInstance(MapperConfig<?> config, Annotated annotated, Class<?> resolverClass) {
		return (TypeIdResolver) findOrCreateInstance(resolverClass);
	}

	/*
	 * (non-Javadoc)
	 * @see com.fasterxml.jackson.databind.cfg.HandlerInstantiator#virtualPropertyWriterInstance(com.fasterxml.jackson.databind.cfg.MapperConfig, java.lang.Class)
	 */
	@Override
	public VirtualBeanPropertyWriter virtualPropertyWriterInstance(MapperConfig<?> config, Class<?> implClass) {
		return (VirtualBeanPropertyWriter) findOrCreateInstance(implClass);
	}
}
