/*
 * Copyright 2016 the original author or authors.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.core.EmbeddedWrapper;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * Component to easily invoke all {@link ResourceProcessor} instances registered for values of type
 * {@link ResourceSupport}.
 *
 * @author Oliver Gierke
 * @since 0.20
 * @soundtrack Doppelkopf - Die fabelhaften Vier (Von Abseits)
 */
public class ResourceProcessorInvoker {

	private final List<ProcessorWrapper> processors;

	/**
	 * Creates a new {@link ResourceProcessorInvoker} to consider the given {@link ResourceProcessor} to post-process the
	 * controller methods return value to before invoking the delegate.
	 * 
	 * @param processors the {@link ResourceProcessor}s to be considered, must not be {@literal null}.
	 */
	public ResourceProcessorInvoker(Collection<ResourceProcessor<?>> processors) {

		Assert.notNull(processors, "ResourceProcessors must not be null!");

		this.processors = new ArrayList<ProcessorWrapper>();

		for (ResourceProcessor<?> processor : processors) {

			ResolvableType processorType = ResolvableType.forClass(ResourceProcessor.class, processor.getClass());
			Class<?> rawType = processorType.getGeneric(0).resolve();

			if (Resource.class.isAssignableFrom(rawType)) {
				this.processors.add(new ResourceProcessorWrapper(processor));
			} else if (Resources.class.isAssignableFrom(rawType)) {
				this.processors.add(new ResourcesProcessorWrapper(processor));
			} else {
				this.processors.add(new DefaultProcessorWrapper(processor));
			}
		}

		Collections.sort(this.processors, AnnotationAwareOrderComparator.INSTANCE);
	}

	/**
	 * Invokes all {@link ResourceProcessor} instances registered for the type of the given value.
	 * 
	 * @param value must not be {@literal null}.
	 * @return
	 */
	public <T extends ResourceSupport> T invokeProcessorsFor(T value) {

		Assert.notNull(value, "Value must not be null!");

		return invokeProcessorsFor(value, ResolvableType.forClass(value.getClass()));
	}

	/**
	 * Invokes all {@link ResourceProcessor} instances registered for the type of the given value and reference type.
	 * 
	 * @param value must not be {@literal null}.
	 * @param referenceType must not be {@literal null}.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends ResourceSupport> T invokeProcessorsFor(T value, ResolvableType referenceType) {

		Assert.notNull(value, "Value must not be null!");
		Assert.notNull(referenceType, "Reference type must not be null!");

		// For Resources implementations, process elements first
		if (ResourceProcessorHandlerMethodReturnValueHandler.RESOURCES_TYPE.isAssignableFrom(referenceType)) {

			Resources<?> resources = (Resources<?>) value;
			ResolvableType elementTargetType = ResolvableType.forClass(Resources.class, referenceType.getRawClass())
					.getGeneric(0);
			List<Object> result = new ArrayList<Object>(resources.getContent().size());

			for (Object element : resources) {

				ResolvableType elementType = ResolvableType.forClass(element.getClass());

				if (!getRawType(elementTargetType).equals(elementType.getRawClass())) {
					elementTargetType = elementType;
				}

				result.add(invokeProcessorsFor(element, elementTargetType));
			}

			ReflectionUtils.setField(ResourceProcessorHandlerMethodReturnValueHandler.CONTENT_FIELD, resources, result);
		}

		return (T) invokeProcessorsFor((Object) value, referenceType);
	}

	/**
	 * Invokes all registered {@link ResourceProcessor}s registered for the given {@link ResolvableType}.
	 * 
	 * @param value the object to process
	 * @param type
	 * @return
	 */
	private Object invokeProcessorsFor(Object value, ResolvableType type) {

		Object currentValue = value;

		// Process actual value
		for (ResourceProcessorInvoker.ProcessorWrapper wrapper : this.processors) {
			if (wrapper.supports(type, currentValue)) {
				currentValue = wrapper.invokeProcessor(currentValue);
			}
		}

		return currentValue;
	}

	private static boolean isRawTypeAssignable(ResolvableType left, Class<?> right) {
		return getRawType(left).isAssignableFrom(right);
	}

	private static Class<?> getRawType(ResolvableType type) {

		Class<?> rawType = type.getRawClass();
		return rawType == null ? Object.class : rawType;
	}

	/**
	 * Interface to unify interaction with {@link ResourceProcessor}s. The {@link Ordered} rank should be determined by
	 * the underlying processor.
	 * 
	 * @author Oliver Gierke
	 */
	private interface ProcessorWrapper extends Ordered {

		/**
		 * Returns whether the underlying processor supports the given {@link ResolvableType}. It might also additionally
		 * inspect the object that would eventually be handed to the processor.
		 * 
		 * @param type the type of object to be post processed, will never be {@literal null}.
		 * @param value the object that would be passed into the processor eventually, can be {@literal null}.
		 * @return
		 */
		boolean supports(ResolvableType type, Object value);

		/**
		 * Performs the actual invocation of the processor. Implementations can be sure
		 * {@link #supports(ResolvableType, Object)} has been called before and returned {@literal true}.
		 * 
		 * @param object
		 */
		Object invokeProcessor(Object object);
	}

	/**
	 * Default implementation of {@link ProcessorWrapper} to generically deal with {@link ResourceSupport} types.
	 * 
	 * @author Oliver Gierke
	 */
	private static class DefaultProcessorWrapper implements ResourceProcessorInvoker.ProcessorWrapper {

		private final ResourceProcessor<?> processor;
		private final ResolvableType targetType;

		/**
		 * Creates a new {@link DefaultProcessorWrapper} with the given {@link ResourceProcessor}.
		 * 
		 * @param processor must not be {@literal null}.
		 */
		public DefaultProcessorWrapper(ResourceProcessor<?> processor) {

			Assert.notNull(processor, "Processor must not be null!");

			this.processor = processor;
			this.targetType = ResolvableType.forClass(ResourceProcessor.class, processor.getClass()).getGeneric(0);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.rest.webmvc.ResourceProcessorHandlerMethodReturnValueHandler.ProcessorWrapper#supports(org.springframework.core.ResolvableType, java.lang.Object)
		 */
		@Override
		public boolean supports(ResolvableType type, Object value) {
			return isRawTypeAssignable(targetType, getRawType(type));
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.data.rest.webmvc.ResourceProcessorHandlerMethodReturnValueHandler.PostProcessorWrapper#invokeProcessor(java.lang.Object)
		 */
		@Override
		@SuppressWarnings("unchecked")
		public Object invokeProcessor(Object object) {
			return ((ResourceProcessor<ResourceSupport>) processor).process((ResourceSupport) object);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.core.Ordered#getOrder()
		 */
		@Override
		public int getOrder() {
			return CustomOrderAwareComparator.INSTANCE.getOrder(processor);
		}

		/**
		 * Returns the target type the underlying {@link ResourceProcessor} wants to get invoked for.
		 * 
		 * @return the targetType
		 */
		public ResolvableType getTargetType() {
			return targetType;
		}
	}

	/**
	 * {@link ProcessorWrapper} to deal with {@link ResourceProcessor}s for {@link Resource}s. Will fall back to peeking
	 * into the {@link Resource}'s content for type resolution.
	 * 
	 * @author Oliver Gierke
	 */
	private static class ResourceProcessorWrapper extends ResourceProcessorInvoker.DefaultProcessorWrapper {

		/**
		 * Creates a new {@link ResourceProcessorWrapper} for the given {@link ResourceProcessor}.
		 * 
		 * @param processor must not be {@literal null}.
		 */
		public ResourceProcessorWrapper(ResourceProcessor<?> processor) {
			super(processor);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.rest.webmvc.ResourceProcessorHandlerMethodReturnValueHandler.DefaultProcessorWrapper#supports(org.springframework.core.ResolvableType, java.lang.Object)
		 */
		@Override
		public boolean supports(ResolvableType type, Object value) {

			if (!ResourceProcessorHandlerMethodReturnValueHandler.RESOURCE_TYPE.isAssignableFrom(type)) {
				return false;
			}

			return super.supports(type, value) && isValueTypeMatch((Resource<?>) value, getTargetType());
		}

		/**
		 * Returns whether the given {@link Resource} matches the given target {@link ResolvableType}. We inspect the
		 * {@link Resource}'s value to determine the match.
		 * 
		 * @param resource
		 * @param target must not be {@literal null}.
		 * @return whether the given {@link Resource} can be assigned to the given target {@link ResolvableType}
		 */
		private static boolean isValueTypeMatch(Resource<?> resource, ResolvableType target) {

			if (resource == null || !isRawTypeAssignable(target, resource.getClass())) {
				return false;
			}

			Object content = resource.getContent();

			if (content == null) {
				return false;
			}

			ResolvableType type = findGenericType(target, Resource.class);
			return type != null && type.getGeneric(0).isAssignableFrom(ResolvableType.forClass(content.getClass()));
		}

		private static ResolvableType findGenericType(ResolvableType source, Class<?> type) {

			Class<?> rawType = getRawType(source);

			if (Object.class.equals(rawType)) {
				return null;
			}

			if (rawType.equals(type)) {
				return source;
			}

			return findGenericType(source.getSuperType(), type);
		}
	}

	/**
	 * {@link ProcessorWrapper} for {@link ResourceProcessor}s targeting {@link Resources}. Will peek into the content of
	 * the {@link Resources} for type matching decisions if needed.
	 * 
	 * @author Oliver Gierke
	 */
	public static class ResourcesProcessorWrapper extends ResourceProcessorInvoker.DefaultProcessorWrapper {

		/**
		 * Creates a new {@link ResourcesProcessorWrapper} for the given {@link ResourceProcessor}.
		 * 
		 * @param processor must not be {@literal null}.
		 */
		public ResourcesProcessorWrapper(ResourceProcessor<?> processor) {
			super(processor);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.rest.webmvc.ResourceProcessorHandlerMethodReturnValueHandler.DefaultProcessorWrapper#supports(org.springframework.core.ResolvableType, java.lang.Object)
		 */
		@Override
		public boolean supports(ResolvableType type, Object value) {

			if (!ResourceProcessorHandlerMethodReturnValueHandler.RESOURCES_TYPE.isAssignableFrom(type)) {
				return false;
			}

			return super.supports(type, value) && isValueTypeMatch((Resources<?>) value, getTargetType());
		}

		/**
		 * Returns whether the given {@link Resources} instance matches the given {@link ResolvableType}. We predict this by
		 * inspecting the first element of the content of the {@link Resources}.
		 * 
		 * @param resources the {@link Resources} to inspect.
		 * @param target that target {@link ResolvableType}.
		 * @return
		 */
		static boolean isValueTypeMatch(Resources<?> resources, ResolvableType target) {

			if (resources == null) {
				return false;
			}

			Collection<?> content = resources.getContent();

			if (content.isEmpty()) {
				return false;
			}

			ResolvableType superType = null;

			for (Class<?> resourcesType : Arrays.<Class<?>>asList(resources.getClass(), Resources.class)) {

				superType = getSuperType(target, resourcesType);

				if (superType != null) {
					break;
				}
			}

			if (superType == null) {
				return false;
			}

			Object element = content.iterator().next();
			ResolvableType resourceType = superType.getGeneric(0);

			if (element instanceof Resource) {
				return ResourceProcessorWrapper.isValueTypeMatch((Resource<?>) element, resourceType);
			} else if (element instanceof EmbeddedWrapper) {
				return isRawTypeAssignable(resourceType, ((EmbeddedWrapper) element).getRelTargetType());
			}

			return false;
		}

		/**
		 * Returns the {@link ResolvableType} for the given raw super class.
		 * 
		 * @param source must not be {@literal null}.
		 * @param superType must not be {@literal null}.
		 * @return
		 */
		private static ResolvableType getSuperType(ResolvableType source, Class<?> superType) {

			if (source.getRawClass().equals(superType)) {
				return source;
			}

			ResolvableType candidate = source.getSuperType();

			if (superType.isAssignableFrom(candidate.getRawClass())) {
				return candidate;
			}

			for (ResolvableType interfaces : source.getInterfaces()) {
				if (superType.isAssignableFrom(interfaces.getRawClass())) {
					return interfaces;
				}
			}

			return ResolvableType.forClass(superType);
		}
	}

	/**
	 * Helper extension of {@link AnnotationAwareOrderComparator} to make {@link #getOrder(Object)} public to allow it
	 * being used in a standalone fashion.
	 * 
	 * @author Oliver Gierke
	 */
	private static class CustomOrderAwareComparator extends AnnotationAwareOrderComparator {

		public static ResourceProcessorInvoker.CustomOrderAwareComparator INSTANCE = new CustomOrderAwareComparator();

		@Override
		protected int getOrder(Object obj) {
			return super.getOrder(obj);
		}
	}
}
