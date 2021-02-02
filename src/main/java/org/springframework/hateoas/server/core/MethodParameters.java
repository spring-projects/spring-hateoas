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
package org.springframework.hateoas.server.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.SynthesizingMethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ConcurrentReferenceHashMap;

/**
 * Value object to represent {@link MethodParameters} to allow to easily find the ones with a given annotation.
 *
 * @author Oliver Gierke
 */
public class MethodParameters {

	private static final ParameterNameDiscoverer DISCOVERER = new DefaultParameterNameDiscoverer();
	private static final Map<Method, MethodParameters> CACHE = new ConcurrentReferenceHashMap<>();

	private final List<MethodParameter> parameters;
	private final Map<Class<?>, List<MethodParameter>> parametersWithAnnotationCache = new ConcurrentReferenceHashMap<>();

	/**
	 * Creates a new {@link MethodParameters} from the given {@link Method}.
	 *
	 * @param method must not be {@literal null}.
	 */
	private MethodParameters(Method method) {
		this(method, null);
	}

	/**
	 * Returns the {@link MethodParameters} for the given {@link Method}.
	 *
	 * @param method must not be {@literal null}.
	 * @return
	 */
	public static MethodParameters of(Method method) {

		Assert.notNull(method, "Method must not be null!");

		return CACHE.computeIfAbsent(method, MethodParameters::new);
	}

	/**
	 * Creates a new {@link MethodParameters} for the given {@link Method} and {@link AnnotationAttribute}. If the latter
	 * is given, method parameter names will be looked up from the annotation attribute if present.
	 *
	 * @param method must not be {@literal null}.
	 * @param namingAnnotation can be {@literal null}.
	 */
	public MethodParameters(Method method, @Nullable AnnotationAttribute namingAnnotation) {

		Assert.notNull(method, "Method must not be null!");

		this.parameters = IntStream.range(0, method.getParameterTypes().length) //
				.mapToObj(it -> new AnnotationNamingMethodParameter(method, it, namingAnnotation)) //
				.peek(it -> it.initParameterNameDiscovery(DISCOVERER)) //
				.collect(Collectors.toList());
	}

	/**
	 * Returns all {@link MethodParameter}s.
	 *
	 * @return
	 */
	public List<MethodParameter> getParameters() {
		return parameters;
	}

	/**
	 * Returns the {@link MethodParameter} with the given name or {@literal null} if none found.
	 *
	 * @param name must not be {@literal null} or empty.
	 * @return
	 */
	public Optional<MethodParameter> getParameter(String name) {

		Assert.hasText(name, "Parameter name must not be null!");

		return getParameters().stream() //
				.filter(it -> name.equals(it.getParameterName())) //
				.findFirst();
	}

	/**
	 * Returns all parameters of the given type.
	 *
	 * @param type must not be {@literal null}.
	 * @return
	 * @since 0.9
	 */
	public List<MethodParameter> getParametersOfType(Class<?> type) {

		Assert.notNull(type, "Type must not be null!");

		return getParameters().stream() //
				.filter(it -> it.getParameterType().equals(type)) //
				.collect(Collectors.toList());
	}

	/**
	 * Returns all {@link MethodParameter}s annotated with the given annotation type.
	 *
	 * @param annotation must not be {@literal null}.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<MethodParameter> getParametersWith(Class<? extends Annotation> annotation) {

		Assert.notNull(annotation, "Annotation must not be null!");

		return parametersWithAnnotationCache.computeIfAbsent(annotation, key -> {

			return getParameters().stream()//
					.filter(it -> it.hasParameterAnnotation((Class<? extends Annotation>) key))//
					.collect(Collectors.toList());
		});
	}

	/**
	 * Custom {@link MethodParameter} extension that will favor the name configured in the {@link AnnotationAttribute} if
	 * set over discovering it.
	 *
	 * @author Oliver Gierke
	 * @author Greg Turnquist
	 */
	private static class AnnotationNamingMethodParameter extends SynthesizingMethodParameter {

		private final AnnotationAttribute attribute;

		private String name;
		private @Nullable Annotation[] combinedAnnotations;

		/**
		 * Creates a new {@link AnnotationNamingMethodParameter} for the given {@link Method}'s parameter with the given
		 * index.
		 *
		 * @param method must not be {@literal null}.
		 * @param parameterIndex
		 * @param attribute can be {@literal null}
		 */
		public AnnotationNamingMethodParameter(Method method, int parameterIndex, @Nullable AnnotationAttribute attribute) {

			super(method, parameterIndex);
			this.attribute = attribute;

		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.core.MethodParameter#getParameterName()
		 */
		@Nullable
		@Override
		public String getParameterName() {

			if (name != null) {
				return name;
			}

			if (attribute != null) {
				String foundName = attribute.getValueFrom(this);
				if (foundName != null) {
					name = foundName;
					return name;
				}
			}

			return super.getParameterName();
		}

		/**
		 * Overriding the original behavior to also include parameter annotations declared on original interface method
		 * declaration for which the parameter is a member of the implementation method.
		 */
		@Override
		public Annotation[] getParameterAnnotations() {

			if (combinedAnnotations != null) {
				return combinedAnnotations;
			}

			Method method = getMethod();

			if (method == null) {
				throw new IllegalStateException("No method available for " + this.toString() + "!");
			}

			Annotation[] original = super.getParameterAnnotations();
			Method interfaceMethod = ClassUtils.getInterfaceMethodIfPossible(method);

			// No interface or method not declared in interface
			if (method.equals(interfaceMethod)) {
				return cacheAndReturn(original);
			}

			// Lookup annotations and their types of the interface method parameter
			MethodParameter interfaceParameter = new MethodParameter(interfaceMethod, getParameterIndex());
			List<Annotation> originalAnnotations = new ArrayList<>(Arrays.asList(original));
			Set<Class<?>> originalAnnotationTypes = originalAnnotations.stream() //
					.map(Object::getClass) //
					.collect(Collectors.toSet());

			// Add annotations which have not been declared on the target method
			Arrays.stream(interfaceParameter.getParameterAnnotations()) //
					.filter(it -> !originalAnnotationTypes.contains(it.annotationType())) //
					.forEach(originalAnnotations::add);

			return cacheAndReturn(originalAnnotations);
		}

		private Annotation[] cacheAndReturn(List<Annotation> annotations) {
			return cacheAndReturn(annotations.toArray(new Annotation[annotations.size()]));
		}

		private Annotation[] cacheAndReturn(Annotation[] annotations) {

			this.combinedAnnotations = annotations;

			return annotations;
		}
	}
}
