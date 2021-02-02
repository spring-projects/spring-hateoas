/*
 * Copyright 2017-2021 the original author or authors.
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

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.reactivestreams.Publisher;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.convert.Property;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.hateoas.AffordanceModel.InputPayloadMetadata;
import org.springframework.hateoas.AffordanceModel.PropertyMetadata;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.InputType;
import org.springframework.http.HttpEntity;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
public class PropertyUtils {

	private static final Map<ResolvableType, ResolvableType> DOMAIN_TYPE_CACHE = new ConcurrentReferenceHashMap<>();
	private static final Map<ResolvableType, InputPayloadMetadata> METADATA_CACHE = new ConcurrentReferenceHashMap<>();
	private static final Set<String> FIELDS_TO_IGNORE = new HashSet<>(Arrays.asList("class", "links"));
	private static final boolean JSR_303_PRESENT = ClassUtils.isPresent("javax.validation.Valid",
			PropertyUtils.class.getClassLoader());
	private static final List<Class<?>> TYPES_TO_UNWRAP = new ArrayList<>(
			Arrays.asList(EntityModel.class, CollectionModel.class, HttpEntity.class));
	private static final ResolvableType OBJECT_TYPE = ResolvableType.forClass(Object.class);

	static {
		if (ClassUtils.isPresent("org.reactivestreams.Publisher", PropertyUtils.class.getClassLoader())) {
			TYPES_TO_UNWRAP.addAll(ReactiveWrappers.getTypesToUnwrap());
		}
	}

	private static class ReactiveWrappers {

		static List<Class<?>> getTypesToUnwrap() {
			return Arrays.asList(Publisher.class);
		}
	}

	public static Map<String, Object> extractPropertyValues(@Nullable Object object) {
		return extractPropertyValues(object, true);
	}

	public static <T> T createObjectFromProperties(Class<T> clazz, Map<String, Object> properties) {

		T obj = BeanUtils.instantiateClass(clazz);

		properties.forEach((key, value) -> {
			Optional.ofNullable(BeanUtils.getPropertyDescriptor(clazz, key)) //
					.ifPresent(property -> {

						try {

							Method writeMethod = property.getWriteMethod();
							ReflectionUtils.makeAccessible(writeMethod);
							writeMethod.invoke(obj, value);

						} catch (IllegalAccessException | InvocationTargetException e) {
							throw new RuntimeException(e);
						}
					});
		});

		return obj;
	}

	public static InputPayloadMetadata getExposedProperties(@Nullable Class<?> type) {
		return getExposedProperties(type == null ? null : ResolvableType.forClass(type));
	}

	/**
	 * Returns the {@link InputPayloadMetadata} model for the given {@link ResolvableType}.
	 *
	 * @param type must not be {@literal null}.
	 * @return
	 */
	public static InputPayloadMetadata getExposedProperties(@Nullable ResolvableType type) {

		if (type == null) {
			return InputPayloadMetadata.NONE;
		}

		return METADATA_CACHE.computeIfAbsent(type, it -> {

			ResolvableType domainType = unwrapDomainType(type);
			Class<?> resolved = domainType.resolve(Object.class);

			return Object.class.equals(resolved) //
					? InputPayloadMetadata.NONE //
					: new TypeBasedPayloadMetadata(domainType, lookupExposedProperties(resolved));
		});
	}

	private static Map<String, Object> unwrapPropertyIfNeeded(String propertyName, BeanWrapper wrapper) {

		Field descriptorField = ReflectionUtils.findField(wrapper.getWrappedClass(), propertyName);
		Method readMethod = wrapper.getPropertyDescriptor(propertyName).getReadMethod();

		MergedAnnotation<JsonUnwrapped> unwrappedAnnotation = Stream.of(descriptorField, readMethod)
				.filter(Objects::nonNull).map(MergedAnnotations::from)
				.flatMap(mergedAnnotations -> mergedAnnotations.stream(JsonUnwrapped.class))
				.filter(it -> it.getBoolean("enabled")).findFirst().orElse(null);

		Object propertyValue = wrapper.getPropertyValue(propertyName);

		if (unwrappedAnnotation == null) {
			return Collections.singletonMap(propertyName, propertyValue);
		}

		String prefix = unwrappedAnnotation.getString("prefix");
		String suffix = unwrappedAnnotation.getString("suffix");

		Map<String, Object> properties = new HashMap<>();

		extractPropertyValues(propertyValue, true) //
				.forEach((name, value) -> properties.put(prefix + name + suffix, value));

		return properties;
	}

	private static Map<String, Object> extractPropertyValues(@Nullable Object object, boolean unwrapEligibleProperties) {

		if (object == null) {
			return Collections.emptyMap();
		}

		if (EntityModel.class.isInstance(object)) {
			return extractPropertyValues(EntityModel.class.cast(object).getContent());
		}

		BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(object);

		return getExposedProperties(object.getClass()).stream() //
				.map(PropertyMetadata::getName) //
				.map(name -> unwrapEligibleProperties //
						? unwrapPropertyIfNeeded(name, wrapper) //
						: Collections.singletonMap(name, wrapper.getPropertyValue(name))) //
				.flatMap(it -> it.entrySet().stream()) //
				.collect(HashMap::new, (map, it) -> map.put(it.getKey(), it.getValue()), HashMap::putAll);
	}

	private static ResolvableType unwrapDomainType(ResolvableType type) {

		if (!type.hasGenerics()) {
			return type;
		}

		if (type.hasUnresolvableGenerics()) {
			return replaceIfUnwrappable(type, () -> OBJECT_TYPE);
		}

		return DOMAIN_TYPE_CACHE.computeIfAbsent(type,
				it -> replaceIfUnwrappable(it, () -> unwrapDomainType(it.getGeneric(0))));
	}

	/**
	 * Replaces the given {@link ResolvableType} with the one produced by the given {@link Supplier} if the former is
	 * assignable from one of the types to be unwrapped.
	 *
	 * @param type must not be {@literal null}.
	 * @param mapper must not be {@literal null}.
	 * @return
	 * @see #TYPES_TO_UNWRAP
	 */
	private static ResolvableType replaceIfUnwrappable(ResolvableType type, Supplier<ResolvableType> mapper) {

		Class<?> resolved = type.resolve(Object.class);

		return TYPES_TO_UNWRAP.stream().anyMatch(it -> it.isAssignableFrom(resolved)) //
				? mapper.get() //
				: type;
	}

	private static Stream<PropertyMetadata> lookupExposedProperties(@Nullable Class<?> type) {

		return type == null //
				? Stream.empty() //
				: getPropertyDescriptors(type) //
						.map(it -> new AnnotatedProperty(new Property(type, it.getReadMethod(), it.getWriteMethod())))
						.map(it -> JSR_303_PRESENT ? new Jsr303AwarePropertyMetadata(it) : new DefaultPropertyMetadata(it));
	}

	/**
	 * Take a {@link Class} and find all properties that are NOT to be ignored, and return them as a {@link Stream}.
	 *
	 * @param type
	 * @return
	 */
	private static Stream<PropertyDescriptor> getPropertyDescriptors(Class<?> type) {

		return Arrays.stream(BeanUtils.getPropertyDescriptors(type))
				.filter(descriptor -> !FIELDS_TO_IGNORE.contains(descriptor.getName()))
				.filter(descriptor -> !descriptorToBeIgnoredByJackson(type, descriptor))
				.filter(descriptor -> !toBeIgnoredByJackson(type, descriptor.getName()))
				.filter(descriptor -> !readerIsToBeIgnoredByJackson(descriptor));
	}

	/**
	 * Check if a given {@link PropertyDescriptor} has {@link JsonIgnore} applied to the field declaration.
	 *
	 * @param clazz
	 * @param descriptor
	 * @return
	 */
	private static boolean descriptorToBeIgnoredByJackson(Class<?> clazz, PropertyDescriptor descriptor) {

		Field descriptorField = ReflectionUtils.findField(clazz, descriptor.getName());

		return descriptorField == null //
				? false //
				: toBeIgnoredByJackson(MergedAnnotations.from(descriptorField));
	}

	/**
	 * Check if a given {@link PropertyDescriptor} has {@link JsonIgnore} on the getter.
	 *
	 * @param descriptor
	 * @return
	 */
	private static boolean readerIsToBeIgnoredByJackson(PropertyDescriptor descriptor) {

		Method reader = descriptor.getReadMethod();

		return reader == null ? false : toBeIgnoredByJackson(MergedAnnotations.from(reader));
	}

	/**
	 * Scan a list of {@link Annotation}s for {@link JsonIgnore} annotations.
	 *
	 * @param annotations
	 * @return
	 */
	private static boolean toBeIgnoredByJackson(MergedAnnotations annotations) {

		return annotations.stream(JsonIgnore.class) //
				.findFirst() //
				.map(it -> it.getBoolean("value")) //
				.orElse(false);
	}

	/**
	 * Check if a field name is to be ignored due to {@link JsonIgnoreProperties}.
	 *
	 * @param clazz
	 * @param field
	 * @return
	 */
	private static boolean toBeIgnoredByJackson(Class<?> clazz, String field) {

		MergedAnnotations annotations = MergedAnnotations.from(clazz);

		return annotations.stream(JsonIgnoreProperties.class) //
				.map(it -> it.getStringArray("value")) //
				.flatMap(Arrays::stream) //
				.anyMatch(it -> it.equalsIgnoreCase(field));
	}

	/**
	 * An abstraction of a {@link Property} in combination with an underlying field for the purpose of looking up
	 * annotations on either the accessors or the field itself.
	 *
	 * @author Oliver Drotbohm
	 */
	private static class AnnotatedProperty {

		private final Map<Class<?>, MergedAnnotation<?>> annotationCache = new ConcurrentReferenceHashMap<>();

		private final Property property;
		private final ResolvableType type;
		private final List<MergedAnnotations> annotations;
		private final MergedAnnotations typeAnnotations;

		/**
		 * Creates a new {@link AnnotatedProperty} for the given {@link Property}.
		 *
		 * @param property must not be {@literal null}.
		 */
		@SuppressWarnings("unchecked")
		public AnnotatedProperty(Property property) {

			Assert.notNull(property, "Property must not be null!");

			this.property = property;

			Field field = ReflectionUtils.findField(property.getObjectType(), property.getName());

			this.type = firstNonEmpty( //
					() -> Optional.ofNullable(property.getReadMethod()).map(ResolvableType::forMethodReturnType), //
					() -> Optional.ofNullable(property.getWriteMethod()).map(it -> ResolvableType.forMethodParameter(it, 0)), //
					() -> Optional.ofNullable(field).map(ResolvableType::forField));

			this.annotations = Stream.of(property.getReadMethod(), property.getWriteMethod(), field) //
					.filter(it -> it != null) //
					.map(MergedAnnotations::from) //
					.collect(Collectors.toList());

			this.typeAnnotations = MergedAnnotations.from(this.type.resolve(Object.class));
		}

		@SuppressWarnings("unchecked")
		private static <T> T firstNonEmpty(Supplier<Optional<T>>... suppliers) {

			Assert.notNull(suppliers, "Suppliers must not be null!");

			return Stream.of(suppliers) //
					.map(Supplier::get).flatMap(it -> it.map(Stream::of).orElseGet(Stream::empty)) //
					.findFirst() //
					.orElseThrow(() -> new IllegalStateException("Could not resolve value!"));
		}

		/**
		 * Returns the name of the property.
		 *
		 * @return will never be {@literal null} or empty.
		 */
		public String getName() {
			return property.getName();
		}

		/**
		 * Returns the property type.
		 *
		 * @return will never be {@literal null}.
		 */
		public ResolvableType getType() {
			return type;
		}

		/**
		 * Returns the annotations on the type of the property.
		 *
		 * @return will never be {@literal null}.
		 */
		public MergedAnnotations getTypeAnnotations() {
			return typeAnnotations;
		}

		/**
		 * Returns whether the write method for the property is present.
		 *
		 * @return
		 */
		public boolean hasWriteMethod() {
			return property.getWriteMethod() != null;
		}

		/**
		 * Returns the {@link MergedAnnotation} of the given type.
		 *
		 * @param <T> the annotation type.
		 * @param type must not be {@literal null}.
		 * @return the {@link MergedAnnotation} if available or {@link MergedAnnotation#missing()} if not.
		 */
		@SuppressWarnings("unchecked")
		public <T extends Annotation> MergedAnnotation<T> getAnnotation(Class<T> type) {

			Assert.notNull(type, "Type must not be null!");

			return (MergedAnnotation<T>) annotationCache.computeIfAbsent(type, it -> lookupAnnotation(type));
		}

		private <T extends Annotation> MergedAnnotation<T> lookupAnnotation(Class<T> type) {

			return this.annotations.stream() //
					.map(it -> it.get(type)) //
					.filter(it -> it != null && it.isPresent()) //
					.findFirst() //
					.orElse(MergedAnnotation.missing());
		}
	}

	/**
	 * Default {@link PropertyMetadata} implementation, considering accessor methods and Jackson annotations to calculate
	 * the metadata settings.
	 *
	 * @author Oliver Drotbohm
	 */
	private static class DefaultPropertyMetadata implements PropertyMetadata, Comparable<DefaultPropertyMetadata> {

		private static final Comparator<PropertyMetadata> BY_NAME = Comparator.comparing(PropertyMetadata::getName);
		private static final InputTypeFactory INPUT_TYPE_FACTORY;

		static {

			INPUT_TYPE_FACTORY = SpringFactoriesLoader.loadFactories(InputTypeFactory.class,
					DefaultPropertyMetadata.class.getClassLoader()).get(0);
		}

		private final AnnotatedProperty property;

		private DefaultPropertyMetadata(AnnotatedProperty property) {
			this.property = property;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.PropertyMetadata#getName()
		 */
		@Override
		public String getName() {
			return property.getName();
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.PropertyMetadata#isRequired()
		 */
		@Override
		public boolean isRequired() {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.PropertyMetadata#isReadOnly()
		 */
		@Override
		public boolean isReadOnly() {

			if (!property.hasWriteMethod()) {
				return true;
			}

			MergedAnnotation<JsonProperty> annotation = property.getAnnotation(JsonProperty.class);

			return !annotation.isPresent() //
					? false //
					: Access.READ_ONLY.equals(annotation.getEnum("access", Access.class));
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.PropertyMetadata#getRegex()
		 */
		@Override
		public Optional<String> getPattern() {
			return Optional.empty();
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.AffordanceModel.PropertyMetadata#getType()
		 */
		@Override
		public ResolvableType getType() {
			return property.getType();
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		@SuppressWarnings("null")
		public int compareTo(DefaultPropertyMetadata that) {
			return BY_NAME.compare(this, that);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.AffordanceModel.PropertyMetadata#getInputType()
		 */
		@Nullable
		@Override
		public String getInputType() {

			String annotatedInputType = getAnnotatedInputType();

			return annotatedInputType != null
					? annotatedInputType
					: INPUT_TYPE_FACTORY.getInputType(getType().resolve(Object.class));
		}

		@Nullable
		protected String getAnnotatedInputType() {

			MergedAnnotation<InputType> annotation = property.getAnnotation(InputType.class);
			String value = annotation.isPresent() ? annotation.getString("value") : null;

			return StringUtils.hasText(value) ? value : null;
		}
	}

	/**
	 * Creates a new {@link PropertyMetadata} aware of JSR-303 annotationns.
	 *
	 * @author Oliver Drotbohm
	 */
	private static class Jsr303AwarePropertyMetadata extends DefaultPropertyMetadata {

		private static final Optional<Class<? extends Annotation>> LENGTH_ANNOTATION;
		private static final Class<? extends Annotation> URL_ANNOTATION, RANGE_ANNOTATION;
		private static final Map<Class<? extends Annotation>, String> TYPE_MAP;

		static {

			LENGTH_ANNOTATION = Optional.ofNullable(
					org.springframework.hateoas.support.ClassUtils.loadIfPresent("org.hibernate.validator.constraints.Length"));

			URL_ANNOTATION = org.springframework.hateoas.support.ClassUtils
					.loadIfPresent("org.hibernate.validator.constraints.URL");

			RANGE_ANNOTATION = org.springframework.hateoas.support.ClassUtils
					.loadIfPresent("org.hibernate.validator.constraints.Range");

			Map<Class<? extends Annotation>, String> typeMap = new HashMap<>();
			typeMap.put(Email.class, "email");

			if (URL_ANNOTATION != null) {
				typeMap.put(URL_ANNOTATION, "url");
			}

			if (RANGE_ANNOTATION != null) {
				typeMap.put(RANGE_ANNOTATION, "range");
			}

			TYPE_MAP = Collections.unmodifiableMap(typeMap);
		}

		private final AnnotatedProperty property;
		private @Nullable Optional<String> inputType;

		/**
		 * Creates a new {@link Jsr303AwarePropertyMetadata} instance for the given {@link AnnotatedProperty}.
		 *
		 * @param property must not be {@literal null}.
		 */
		private Jsr303AwarePropertyMetadata(AnnotatedProperty property) {

			super(property);

			this.property = property;
			this.inputType = null;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.PropertyUtils.PropertyMetadata#isRequired()
		 */
		@Override
		public boolean isRequired() {
			return super.isRequired() || property.getAnnotation(NotNull.class).isPresent();
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.PropertyUtils.PropertyMetadata#getRegex()
		 */
		@Override
		public Optional<String> getPattern() {
			return getAnnotationAttribute(Pattern.class, "regexp", String.class);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.AffordanceModel.PropertyMetadata#getMin()
		 */
		@Nullable
		@Override
		public Long getMin() {

			Optional<Long> attribute = getAnnotationAttribute(RANGE_ANNOTATION, "min", Long.class);

			if (attribute.isPresent()) {
				return attribute.get();
			}

			return getAnnotationAttribute(Min.class, "value", Long.class).orElse(null);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.AffordanceModel.PropertyMetadata#getMax()
		 */
		@Nullable
		@Override
		public Long getMax() {

			Optional<Long> attribute = getAnnotationAttribute(RANGE_ANNOTATION, "max", Long.class);

			if (attribute.isPresent()) {
				return attribute.get();
			}

			return getAnnotationAttribute(Max.class, "value", Long.class).orElse(null);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.AffordanceModel.PropertyMetadata#getMinLength()
		 */
		@Nullable
		@Override
		public Long getMinLength() {
			return LENGTH_ANNOTATION.flatMap(it -> getAnnotationAttribute(it, "min", Integer.class))
					.map(Integer::longValue)
					.orElse(null);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.AffordanceModel.PropertyMetadata#getMaxLength()
		 */
		@Nullable
		@Override
		public Long getMaxLength() {
			return LENGTH_ANNOTATION.flatMap(it -> getAnnotationAttribute(it, "max", Integer.class))
					.map(Integer::longValue)
					.orElse(null);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.PropertyUtils.DefaultPropertyMetadata#getInputType()
		 */
		@Nullable
		@Override
		public String getInputType() {

			if (inputType != null) {
				return inputType.orElse(null);
			}

			String inputType = getAnnotatedInputType();

			if (inputType != null) {
				return cacheAndReturn(inputType);
			}

			inputType = lookupFromTypeMap();

			return cacheAndReturn(inputType != null ? inputType : super.getInputType());
		}

		private String cacheAndReturn(String value) {

			this.inputType = Optional.ofNullable(value);

			return value;
		}

		private String lookupFromTypeMap() {

			return TYPE_MAP.entrySet().stream()
					.flatMap(it -> {

						MergedAnnotation<? extends Annotation> annotation = property.getAnnotation(it.getKey());

						return annotation.isPresent() ? Stream.of(it.getValue()) : Stream.empty();
					})
					.findFirst()
					.orElse(null);
		}

		private <T> Optional<T> getAnnotationAttribute(Class<? extends Annotation> annotation, String attribute,
				Class<T> type) {

			MergedAnnotation<? extends Annotation> mergedAnnotation = property.getAnnotation(annotation);

			if (mergedAnnotation.isPresent()) {
				return mergedAnnotation.getValue(attribute, type);
			}

			mergedAnnotation = property.getTypeAnnotations().get(annotation);

			return mergedAnnotation.isPresent() ? mergedAnnotation.getValue(attribute, type) : Optional.empty();
		}
	}
}
