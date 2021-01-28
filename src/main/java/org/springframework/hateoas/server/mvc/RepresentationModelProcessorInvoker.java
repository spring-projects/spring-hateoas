/*
 * Copyright 2016-2021 the original author or authors.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.hateoas.server.core.EmbeddedWrapper;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * Component to easily invoke all {@link RepresentationModelProcessor} instances registered for values of type
 * {@link RepresentationModel}.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 * @since 0.20
 * @soundtrack Doppelkopf - Die fabelhaften Vier (Von Abseits)
 */
public class RepresentationModelProcessorInvoker {

	private final List<ProcessorWrapper> processors;

	/**
	 * Creates a new {@link RepresentationModelProcessorInvoker} to consider the given
	 * {@link RepresentationModelProcessor} to post-process the controller methods return value to before invoking the
	 * delegate.
	 *
	 * @param processors the {@link RepresentationModelProcessor}s to be considered, must not be {@literal null}.
	 */
	public RepresentationModelProcessorInvoker(Collection<RepresentationModelProcessor<?>> processors) {

		Assert.notNull(processors, "ResourceProcessors must not be null!");

		this.processors = new ArrayList<>();

		for (RepresentationModelProcessor<?> processor : processors) {

			ResolvableType processorType = ResolvableType.forClass(RepresentationModelProcessor.class, processor.getClass());
			Class<?> rawType = processorType.getGeneric(0).resolve();

			if (rawType != null) {
				if (EntityModel.class.isAssignableFrom(rawType)) {
					this.processors.add(new EntityModelProcessorWrapper(processor));
				} else if (CollectionModel.class.isAssignableFrom(rawType)) {
					this.processors.add(new CollectionModelProcessorWrapper(processor));
				} else {
					this.processors.add(new DefaultProcessorWrapper(processor));
				}
			}
		}

		this.processors.sort(AnnotationAwareOrderComparator.INSTANCE);
	}

	/**
	 * Invokes all {@link RepresentationModelProcessor} instances registered for the type of the given value.
	 *
	 * @param value must not be {@literal null}.
	 * @return
	 */
	public <T extends RepresentationModel<T>> T invokeProcessorsFor(T value) {

		Assert.notNull(value, "Value must not be null!");

		return invokeProcessorsFor(value, ResolvableType.forClass(value.getClass()));
	}

	/**
	 * Invokes all {@link RepresentationModelProcessor} instances registered for the type of the given value and reference
	 * type.
	 *
	 * @param value must not be {@literal null}.
	 * @param referenceType must not be {@literal null}.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends RepresentationModel<T>> T invokeProcessorsFor(T value, ResolvableType referenceType) {

		Assert.notNull(value, "Value must not be null!");
		Assert.notNull(referenceType, "Reference type must not be null!");

		// For Resources implementations, process elements first
		if (RepresentationModelProcessorHandlerMethodReturnValueHandler.COLLECTION_MODEL_TYPE
				.isAssignableFrom(referenceType)) {

			CollectionModel<?> collectionModel = (CollectionModel<?>) value;
			Class<?> rawClass = referenceType.getRawClass();

			if (rawClass == null) {
				throw new IllegalArgumentException(String.format("%s does not expose a raw type!", referenceType));
			}

			ResolvableType elementTargetType = ResolvableType.forClass(CollectionModel.class, rawClass).getGeneric(0);
			List<Object> result = new ArrayList<>(collectionModel.getContent().size());

			for (Object element : collectionModel) {

				ResolvableType elementType = ResolvableType.forClass(element.getClass());

				if (!getRawType(elementTargetType).equals(elementType.getRawClass())) {
					elementTargetType = elementType;
				}

				result.add(invokeProcessorsFor(element, elementTargetType));
			}

			if (RepresentationModelProcessorHandlerMethodReturnValueHandler.CONTENT_FIELD != null) {

				ReflectionUtils.setField( //
						RepresentationModelProcessorHandlerMethodReturnValueHandler.CONTENT_FIELD, //
						collectionModel, //
						result //
				);
			}
		}

		return (T) invokeProcessorsFor((Object) value, referenceType);
	}

	/**
	 * Invokes all registered {@link RepresentationModelProcessor}s registered for the given {@link ResolvableType}.
	 *
	 * @param value the object to process
	 * @param type
	 * @return
	 */
	private Object invokeProcessorsFor(Object value, ResolvableType type) {

		Object currentValue = value;

		// Process actual value
		for (RepresentationModelProcessorInvoker.ProcessorWrapper wrapper : this.processors) {
			if (wrapper.supports(type, currentValue)) {
				currentValue = wrapper.invokeProcessor(currentValue);
			}
		}

		return currentValue;
	}

	private static boolean isRawTypeAssignable(@Nullable ResolvableType left, @Nullable Class<?> right) {

		Assert.notNull(right, "right cannot be null!");

		return getRawType(left).isAssignableFrom(right);
	}

	private static Class<?> getRawType(@Nullable ResolvableType type) {
		return type == null ? Object.class : type.resolve(Object.class);
	}

	/**
	 * Interface to unify interaction with {@link RepresentationModelProcessor}s. The {@link Ordered} rank should be
	 * determined by the underlying processor.
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
		<S> S invokeProcessor(S object);
	}

	/**
	 * Default implementation of {@link ProcessorWrapper} to generically deal with {@link RepresentationModel} types.
	 *
	 * @author Oliver Gierke
	 */
	private static class DefaultProcessorWrapper implements RepresentationModelProcessorInvoker.ProcessorWrapper {

		private final RepresentationModelProcessor<?> processor;
		private final ResolvableType targetType;

		/**
		 * Creates a new {@link DefaultProcessorWrapper} with the given {@link RepresentationModelProcessor}.
		 *
		 * @param processor must not be {@literal null}.
		 */
		DefaultProcessorWrapper(RepresentationModelProcessor<?> processor) {

			Assert.notNull(processor, "Processor must not be null!");

			this.processor = processor;
			this.targetType = ResolvableType.forClass(RepresentationModelProcessor.class, processor.getClass()).getGeneric(0);
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
			return ((RepresentationModelProcessor<RepresentationModel<?>>) processor)
					.process((RepresentationModel<?>) object);
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
		 * Returns the target type the underlying {@link RepresentationModelProcessor} wants to get invoked for.
		 *
		 * @return the targetType
		 */
		public ResolvableType getTargetType() {
			return targetType;
		}
	}

	/**
	 * {@link ProcessorWrapper} to deal with {@link RepresentationModelProcessor}s for {@link EntityModel}s. Will fall
	 * back to peeking into the {@link EntityModel}'s content for type resolution.
	 *
	 * @author Oliver Gierke
	 */
	private static class EntityModelProcessorWrapper extends RepresentationModelProcessorInvoker.DefaultProcessorWrapper {

		/**
		 * Creates a new {@link EntityModelProcessorWrapper} for the given {@link RepresentationModelProcessor}.
		 *
		 * @param processor must not be {@literal null}.
		 */
		public EntityModelProcessorWrapper(RepresentationModelProcessor<?> processor) {
			super(processor);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.rest.webmvc.ResourceProcessorHandlerMethodReturnValueHandler.DefaultProcessorWrapper#supports(org.springframework.core.ResolvableType, java.lang.Object)
		 */
		@Override
		public boolean supports(ResolvableType type, Object value) {

			if (!RepresentationModelProcessorHandlerMethodReturnValueHandler.ENTITY_MODEL_TYPE.isAssignableFrom(type)) {
				return false;
			}

			return super.supports(type, value) && isValueTypeMatch((EntityModel<?>) value, getTargetType());
		}

		/**
		 * Returns whether the given {@link EntityModel} matches the given target {@link ResolvableType}. We inspect the
		 * {@link EntityModel}'s value to determine the match.
		 *
		 * @param entityModel can be {@literal null}.
		 * @param target can be {@literal null}.
		 * @return whether the given {@link EntityModel} can be assigned to the given target {@link ResolvableType}
		 */
		private static boolean isValueTypeMatch(@Nullable EntityModel<?> entityModel, @Nullable ResolvableType target) {

			if (target == null || entityModel == null || !isRawTypeAssignable(target, entityModel.getClass())) {
				return false;
			}

			Object content = entityModel.getContent();

			if (content == null) {
				return false;
			}

			ResolvableType type = findGenericType(target, EntityModel.class);

			return target.isAssignableFrom(content.getClass()) || //
					type != null && type.getGeneric(0).isAssignableFrom(ResolvableType.forClass(content.getClass()));
		}

		@Nullable
		private static ResolvableType findGenericType(@Nullable ResolvableType source, Class<?> type) {

			if (source == null) {
				return null;
			}

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
	 * {@link ProcessorWrapper} for {@link RepresentationModelProcessor}s targeting {@link CollectionModel}. Will peek
	 * into the content of the {@link CollectionModel} for type matching decisions if needed.
	 *
	 * @author Oliver Gierke
	 */
	public static class CollectionModelProcessorWrapper
			extends RepresentationModelProcessorInvoker.DefaultProcessorWrapper {

		/**
		 * Creates a new {@link CollectionModelProcessorWrapper} for the given {@link RepresentationModelProcessor}.
		 *
		 * @param processor must not be {@literal null}.
		 */
		public CollectionModelProcessorWrapper(RepresentationModelProcessor<?> processor) {
			super(processor);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.rest.webmvc.ResourceProcessorHandlerMethodReturnValueHandler.DefaultProcessorWrapper#supports(org.springframework.core.ResolvableType, java.lang.Object)
		 */
		@Override
		public boolean supports(ResolvableType type, Object value) {

			if (!RepresentationModelProcessorHandlerMethodReturnValueHandler.COLLECTION_MODEL_TYPE.isAssignableFrom(type)) {
				return false;
			}

			boolean defaultSupports = super.supports(type, value);
			boolean valueTypeMatch = isValueTypeMatch((CollectionModel<?>) value, getTargetType());
			return defaultSupports && valueTypeMatch;
		}

		/**
		 * Returns whether the given {@link CollectionModel} instance matches the given {@link ResolvableType}. We predict
		 * this by inspecting the first element of the content of the {@link CollectionModel}.
		 *
		 * @param collectionModel the {@link CollectionModel} to inspect.
		 * @param target that target {@link ResolvableType}.
		 * @return
		 */
		boolean isValueTypeMatch(@Nullable CollectionModel<?> collectionModel, ResolvableType target) {

			if (collectionModel == null) {
				return false;
			}

			Collection<?> content = collectionModel.getContent();

			if (content.isEmpty()) {
				return false;
			}

			ResolvableType superType = null;

			for (Class<?> collectionModelType : Arrays.<Class<?>> asList(collectionModel.getClass(), CollectionModel.class)) {

				superType = getSuperType(target, collectionModelType);

				if (superType != null) {
					break;
				}
			}

			if (superType == null) {
				return false;
			}

			Object element = content.iterator().next();
			ResolvableType resourceType = superType.getGeneric(0);

			if (element instanceof EntityModel) {
				return EntityModelProcessorWrapper.isValueTypeMatch((EntityModel<?>) element, resourceType);
			} else if (element instanceof RepresentationModel) {
				return resourceType.isInstance(element);
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
		@Nullable
		private static ResolvableType getSuperType(ResolvableType source, Class<?> superType) {

			Class<?> rawType = source.getRawClass();

			if (rawType != null && rawType.equals(superType)) {
				return source;
			}

			ResolvableType candidate = source.getSuperType();

			if (candidate.getRawClass() != null && superType.isAssignableFrom(candidate.getRawClass())) {
				return candidate;
			}

			for (ResolvableType interfaces : source.getInterfaces()) {
				if (interfaces.getRawClass() != null && superType.isAssignableFrom(interfaces.getRawClass())) {
					return interfaces;
				}
			}

			return null;
		}
	}

	/**
	 * Helper extension of {@link AnnotationAwareOrderComparator} to make {@link #getOrder(Object)} public to allow it
	 * being used in a standalone fashion.
	 *
	 * @author Oliver Gierke
	 */
	private static class CustomOrderAwareComparator extends AnnotationAwareOrderComparator {

		public static RepresentationModelProcessorInvoker.CustomOrderAwareComparator INSTANCE = new CustomOrderAwareComparator();

		@Override
		protected int getOrder(@Nullable Object obj) {
			return super.getOrder(obj);
		}
	}
}
