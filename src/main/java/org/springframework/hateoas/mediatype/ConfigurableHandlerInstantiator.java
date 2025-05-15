/*
 * Copyright 2021-2024 the original author or authors.
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

import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.KeyDeserializer;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.cfg.HandlerInstantiator;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.Annotated;
import tools.jackson.databind.jsontype.TypeIdResolver;
import tools.jackson.databind.jsontype.TypeResolverBuilder;
import tools.jackson.databind.ser.VirtualBeanPropertyWriter;

import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.util.Assert;

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
	public ValueDeserializer<?> deserializerInstance(DeserializationConfig config, Annotated annotated,
			Class<?> deserClass) {
		return (ValueDeserializer<?>) findOrCreateInstance(deserClass);
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
	public ValueSerializer<?> serializerInstance(SerializationConfig config, Annotated annotated, Class<?> serClass) {
		return (ValueSerializer<?>) findOrCreateInstance(serClass);
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
