/*
 * Copyright 2012 the original author or authors.
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
package org.springframework.hateoas.jaxrs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.springframework.hateoas.LinkBuilder;
import org.springframework.hateoas.core.AnnotationMappingDiscoverer;
import org.springframework.hateoas.core.LinkBuilderSupport;
import org.springframework.hateoas.core.MappingDiscoverer;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

/**
 * {@link LinkBuilder} to derive URI mappings from a JAX-RS {@link Path} annotation.
 *
 * @author Oliver Gierke
 */
public class JaxRsLinkBuilder extends LinkBuilderSupport<JaxRsLinkBuilder> {

	private static final MappingDiscoverer DISCOVERER = new AnnotationMappingDiscoverer(Path.class);

	/**
	 * Creates a new {@link JaxRsLinkBuilder} from the given {@link UriComponentsBuilder}.
	 *
	 * @param builder must not be {@literal null}.
	 */
	private JaxRsLinkBuilder(UriComponentsBuilder builder) {
		super(builder);
	}

	/**
	 * Creates a {@link JaxRsLinkBuilder} instance to link to the {@link Path} mapping tied to the given class.
	 *
	 * @param service the class to discover the annotation on, must not be {@literal null}.
	 * @return
	 */
	public static JaxRsLinkBuilder linkTo(Class<?> service) {
		return linkTo(service, new Object[0]);
	}

	/**
	 * Creates a new {@link JaxRsLinkBuilder} instance to link to the {@link Path} mapping tied to the given class binding
	 * the given parameters to the URI template.
	 *
	 * @param service the class to discover the annotation on, must not be {@literal null}.
	 * @param parameters additional parameters to bind to the URI template declared in the annotation, must not be
	 *          {@literal null}.
	 * @return
	 */
	public static JaxRsLinkBuilder linkTo(Class<?> service, Object... parameters) {

		JaxRsLinkBuilder builder = new JaxRsLinkBuilder(ServletUriComponentsBuilder.fromCurrentServletMapping());

		UriTemplate template = new UriTemplate(DISCOVERER.getMapping(service));
		return builder.slash(template.expand(parameters));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.UriComponentsLinkBuilder#getThis()
	 */
	@Override
	protected JaxRsLinkBuilder getThis() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.UriComponentsLinkBuilder#createNewInstance(org.springframework.web.util.UriComponentsBuilder)
	 */
	@Override
	protected JaxRsLinkBuilder createNewInstance(UriComponentsBuilder builder) {
		return new JaxRsLinkBuilder(builder);
	}

	/**
	 * Creates a new {@link JaxRsLinkBuilder} based on the given service method, resolving URI templates if necessary. The
	 * service method is created by {@link #on(Class)}.
	 * <p>
	 *
	 * @param method representation of a method on the target service, created by {@link #on(Class)}.
	 * @return link builder which expects you to set a rel, e.g. using {@link #withRel(String)}.
	 * @see #on(Class)
	 */
	public static JaxRsLinkBuilder linkToMethod(Object method) {
		Invocations invocations = (Invocations) method;
		List<Invocation> recorded = invocations.getInvocations();
		Invocation invocation = recorded.get(0);
		String classLevelMapping = LinkTemplateUtils.getClassLevelMapping(invocation.getTarget().getClass(), Path.class);
		LinkTemplate<PathParam, QueryParam> template = LinkTemplateUtils.createLinkTemplate(classLevelMapping,
				invocation.getMethod(), Path.class, PathParam.class, QueryParam.class);

		JaxRsLinkBuilder builder = new JaxRsLinkBuilder(ServletUriComponentsBuilder.fromCurrentServletMapping());

		UriTemplate uriTemplate = new UriTemplate(template.getLinkTemplate());
		return builder.slash(uriTemplate.expand(invocation.getArgs()));
	}

	/**
	 * Allows to create a representation of a method on the given service, for use with
	 * {@link JaxRsLinkBuilder#linkTo(Object)}. Define the method representation by simply calling the desired method as
	 * shown below.
	 * <p>
	 * This example creates a representation of the method <code>PersonService.showAll()</code>:
	 *
	 * <pre>
	 * on(PersonService.class).showAll();
	 * </pre>
	 *
	 * @param service
	 * @return
	 * @see #linkToMethod(Object)
	 */
	public static <T> T on(Class<T> service) {

		return LinkTemplateUtils.on(service);
	}

	public static FormDescriptor createForm(String formName, Object method) {

		// TODO use on parameter for form default values
		
		Invocations invocations = (Invocations) method;
		List<Invocation> recorded = invocations.getInvocations();
		Invocation invocation = recorded.get(0);
		String classLevelMapping = LinkTemplateUtils.getClassLevelMapping(invocation.getTarget().getClass(), Path.class);
		Method invokedMethod = invocation.getMethod();
		LinkTemplate<PathParam, QueryParam> linkTemplate = LinkTemplateUtils.createLinkTemplate(classLevelMapping,
				invokedMethod, Path.class, PathParam.class, QueryParam.class);

		String requestMethod = getRequestMethod(invokedMethod);

		FormDescriptor formDescriptor = new FormDescriptor(formName, linkTemplate.getLinkTemplate(),
				requestMethod);

		List<AnnotatedParam<PathParam>> pathVariables = linkTemplate.getPathVariables();
		for (AnnotatedParam<PathParam> pathVariable : pathVariables) {
			formDescriptor.addPathVariable(pathVariable.paramAnnotation.value(), pathVariable.paramType);
		}
		List<AnnotatedParam<QueryParam>> requestParams = linkTemplate.getRequestParams();
		for (AnnotatedParam<QueryParam> requestParam : requestParams) {
			formDescriptor.addRequestParam(requestParam.paramAnnotation.value(), requestParam.paramType);
		}

		return formDescriptor;
	}

	private static String getRequestMethod(Method method) {
		String ret = "GET"; // default
		for (Class<? extends Annotation> methodAnnotation : HTTP_METHODS) {
			Annotation ann = AnnotationUtils.findAnnotation(method, methodAnnotation);
			if (ann != null) {
				ret = ann.annotationType().getName();
				break;
			}
		}
		return ret;
	}

}
