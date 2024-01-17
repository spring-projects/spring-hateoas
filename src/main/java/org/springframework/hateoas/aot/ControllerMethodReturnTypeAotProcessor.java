/*
 * Copyright 2022-2024 the original author or authors.
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
package org.springframework.hateoas.aot;

import static org.springframework.hateoas.aot.AotUtils.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.target.EmptyTargetSource;
import org.springframework.aot.generate.GenerationContext;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.beans.factory.aot.BeanRegistrationAotContribution;
import org.springframework.beans.factory.aot.BeanRegistrationAotProcessor;
import org.springframework.beans.factory.aot.BeanRegistrationCode;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.hateoas.server.core.DummyInvocationUtils;
import org.springframework.hateoas.server.core.LastInvocationAware;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * A {@link BeanRegistrationAotProcessor} that contributes proxy types for return types of controller methods so that
 * can be pointed to by {@link DummyInvocationUtils}, i.e. creating links via fake method invocations.
 *
 * @author Christoph Strobl
 * @author Oliver Drotbohm
 * @since 2.0
 */
public class ControllerMethodReturnTypeAotProcessor implements BeanRegistrationAotProcessor {

	private final Class<? extends Annotation> controllerAnnotationType;

	/**
	 * Creates a new {@link ControllerMethodReturnTypeAotProcessor} looking for classes annotated with {@link Controller}.
	 */
	public ControllerMethodReturnTypeAotProcessor() {
		this(Controller.class);
	}

	/**
	 * Creates a new {@link ControllerMethodReturnTypeAotProcessor} looking for classes equipped with the given
	 * annotation.
	 *
	 * @param controllerAnnotationType must not be {@literal null}.
	 */
	protected ControllerMethodReturnTypeAotProcessor(
			Class<? extends Annotation> controllerAnnotationType) {

		Assert.notNull(controllerAnnotationType, "Controller anntotation type must not be null!");

		this.controllerAnnotationType = controllerAnnotationType;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.aot.BeanRegistrationAotProcessor#processAheadOfTime(org.springframework.beans.factory.support.RegisteredBean)
	 */
	@Override
	@Nullable
	public BeanRegistrationAotContribution processAheadOfTime(RegisteredBean registeredBean) {

		var beanClass = registeredBean.getBeanClass();

		return AnnotatedElementUtils.isAnnotated(beanClass, controllerAnnotationType)
				? new ProxyRegisteringAotContribution(beanClass)
				: null;
	}

	/**
	 * AOT contribution that registers proxy types for return types of controller methods.
	 *
	 * @author Oliver Drotbohm
	 * @since 2.0
	 */
	private static class ProxyRegisteringAotContribution implements BeanRegistrationAotContribution {

		private static final Logger LOGGER = LoggerFactory.getLogger(ProxyRegisteringAotContribution.class);

		private final Class<?> beanClass;

		ProxyRegisteringAotContribution(Class<?> beanClass) {

			Assert.notNull(beanClass, "Bean class must not be null!");

			this.beanClass = beanClass;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.beans.factory.aot.BeanRegistrationAotContribution#applyTo(org.springframework.aot.generate.GenerationContext, org.springframework.beans.factory.aot.BeanRegistrationCode)
		 */
		@Override
		public void applyTo(GenerationContext generationContext, BeanRegistrationCode beanRegistrationCode) {

			Class<?> proxyType = registerCglibProxy(beanClass, beanClass, generationContext);

			if (proxyType != null) {
				LOGGER.info("Created proxy type {} for {}", proxyType, beanClass);
			}

			ReflectionUtils.doWithMethods(beanClass, (method) -> {

				Class<?> returnType = method.getReturnType();

				if (ReflectionUtils.isObjectMethod(method)
						|| method.isSynthetic()
						|| method.isBridge()
						|| Modifier.isPrivate(method.getModifiers())
						|| ClassUtils.isAssignable(returnType, void.class)) {
					return;
				}

				var runtimeHints = generationContext.getRuntimeHints();
				var methodReturnType = ResolvableType.forMethodReturnType(method);

				registerModelDomainTypesForReflection(methodReturnType, runtimeHints.reflection(), beanClass);

				if (returnType.isInterface()) {
					runtimeHints.proxies().registerJdkProxy(returnType);
					return;
				}

				registerCglibProxy(returnType, beanClass, generationContext);
			});
		}

		@Nullable
		private Class<?> registerCglibProxy(Class<?> type, Class<?> beanClass, GenerationContext context) {

			if (Modifier.isFinal(type.getModifiers())) {
				return null;
			}

			// Wee need to find at least one non-private constructor to be able to create a CGLib proxy in the first
			// place
			var anyNonPrivateConstructor = Arrays.stream(type.getDeclaredConstructors())
					.map(Constructor::getModifiers)
					.anyMatch(Predicate.not(Modifier::isPrivate));

			if (!anyNonPrivateConstructor) {
				return null;
			}

			var result = createProxyClass(type, beanClass);

			if (result != null) {

				// Required for EnhancerFactoryData(Class, Class[], boolean)
				var reflection = context.getRuntimeHints().reflection();

				reflection.registerType(result, MemberCategory.INVOKE_DECLARED_METHODS);

				reflection.registerField(ReflectionUtils.findField(result, "CGLIB$FACTORY_DATA"));
				reflection.registerField(ReflectionUtils.findField(result, "CGLIB$CALLBACK_FILTER"));
			}

			return result;
		}

		@Nullable
		private Class<?> createProxyClass(Class<?> type, Class<?> beanClass) {

			try {

				var factory = new ProxyFactory();

				factory.addInterface(LastInvocationAware.class);
				factory.setProxyTargetClass(true);
				factory.setTargetSource(EmptyTargetSource.forClass(type));

				return factory.getProxyClass(type.getClassLoader());

			} catch (AopConfigException o_O) {

				LOGGER.info("Could not create proxy class for {} (via {}). Reason {}", type, beanClass,
						o_O.getMessage());
			}

			return null;
		}
	}
}
