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
package org.springframework.hateoas.mvc;

import java.lang.reflect.Method;
import java.util.List;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.FormDescriptor;
import org.springframework.hateoas.util.AnnotatedParam;
import org.springframework.hateoas.util.Invocation;
import org.springframework.hateoas.util.Invocations;
import org.springframework.hateoas.util.LinkTemplate;
import org.springframework.hateoas.util.LinkTemplateUtils;
import org.springframework.hateoas.core.LinkBuilderSupport;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

/**
 * Builder to ease building {@link Link} instances pointing to Spring MVC controllers.
 *
 * @author Oliver Gierke
 */
public class ControllerLinkBuilder extends LinkBuilderSupport<ControllerLinkBuilder> {

	/**
	 * Creates a new {@link ControllerLinkBuilder} using the given {@link UriComponentsBuilder}.
	 *
	 * @param builder must not be {@literal null}.
	 */
	private ControllerLinkBuilder(UriComponentsBuilder builder) {
		super(builder);
	}

	/**
	 * Creates a new {@link ControllerLinkBuilder} with a base of the mapping annotated to the given controller class.
	 *
	 * @param controller the class to discover the annotation on, must not be {@literal null}.
	 * @return
	 */
	public static ControllerLinkBuilder linkTo(Class<?> controller) {
		return linkTo(controller, new Object[0]);
	}

	/**
	 * Creates a new {@link ControllerLinkBuilder} with a base of the mapping annotated to the given controller class. The
	 * additional parameters are used to fill up potentially available path variables in the class scope request mapping.
	 *
	 * @param controller the class to discover the annotation on, must not be {@literal null}.
	 * @param parameters additional parameters to bind to the URI template declared in the annotation, must not be
	 *          {@literal null}.
	 * @return
	 */
	public static ControllerLinkBuilder linkTo(Class<?> controller, Object... parameters) {

		Assert.notNull(controller);

		RequestMapping annotation = AnnotationUtils.findAnnotation(controller, RequestMapping.class);
		String[] mapping = annotation == null ? new String[0] : (String[]) AnnotationUtils.getValue(annotation);

		if (mapping.length > 1) {
			throw new IllegalStateException("Multiple controller mappings defined! Unable to build URI!");
		}

		ControllerLinkBuilder builder = new ControllerLinkBuilder(ServletUriComponentsBuilder.fromCurrentServletMapping());

		if (mapping.length == 0) {
			return builder;
		}

		UriTemplate template = new UriTemplate(mapping[0]);
		return builder.slash(template.expand(parameters));
	}

	/**
	 * Allows to create a representation of a method on the given controller, for use with
	 * {@link ControllerLinkBuilder#linkToMethod(Object)}. Define the method representation by simply calling the desired
	 * method as shown below.
	 * <p>
	 * This example creates a representation of the method <code>PersonController.showAll()</code>:
	 *
	 * <pre>
	 * on(PersonController.class).showAll();
	 * </pre>
	 *
	 * @param controller
	 * @return
	 * @see #linkToMethod(Object)
	 */
	public static <T> T on(Class<T> controller) {
		return LinkTemplateUtils.on(controller);
	}

	/**
	 * Creates a new {@link ControllerLinkBuilder} based on the given controller method, resolving URI templates if
	 * necessary. The controller method is created by {@link #on(Class)}.
	 * <p>
	 * Consider the following PersonController with a class level mapping and a method level mapping:
	 *
	 * <pre>
	 * &#064;Controller
	 * &#064;RequestMapping(&quot;/people&quot;)
	 * public class PersonController {
	 *
	 * 	&#064;RequestMapping(value = &quot;/{personId}/address&quot;, method = RequestMethod.GET)
	 *     public HttpEntity&lt;PersonResource&gt; showAddress(@PathVariable Long personId) {
	 *         (...)
	 *     }
	 * }
	 * </pre>
	 *
	 * You may link to this person controller's <code>/{personId}/address</code> resource from another controller.
	 * Assuming we are within a method where we produce the personResource for a given personId, e.g. from a form request
	 * for www.example.com/people/search?id=42, we can do:
	 *
	 * <pre>
	 * Link address = linkToMethod(on(PersonController.class).show(personId).withRel("address");
	 * PersonResource personResource = (...);
	 * personResource.addLink(address);
	 * </pre>
	 *
	 * The <code>linkTo</code> method above gives us a
	 * <code>Link</link> to the person's address, which we can add to the personResource. Note that the path
	 * variable <code>{personId}</code> will be expanded to its actual value:
	 *
	 * <pre>
	 * http://www.example.com/people/42/address
	 * </pre>
	 *
	 * @param method representation of a method on the target controller, created by {@link #on(Class)}.
	 * @return link builder which expects you to set a rel, e.g. using {@link #withRel(String)}.
	 * @see #on(Class)
	 */
	public static ControllerLinkBuilder linkToMethod(Object method) {
		Invocations invocations = (Invocations) method;
		List<Invocation> recorded = invocations.getInvocations();
		Invocation invocation = recorded.get(0);
		String classLevelMapping = LinkTemplateUtils.getClassLevelMapping(invocation.getTarget().getClass(),
				RequestMapping.class);
		LinkTemplate<PathVariable, RequestParam> template = LinkTemplateUtils.createLinkTemplate(classLevelMapping,
				invocation.getMethod(), RequestMapping.class, PathVariable.class, RequestParam.class);

		ControllerLinkBuilder builder = new ControllerLinkBuilder(ServletUriComponentsBuilder.fromCurrentServletMapping());

		UriTemplate uriTemplate = new UriTemplate(template.getLinkTemplate());
		return builder.slash(uriTemplate.expand(invocation.getArgs()));
	}

	/**
	 * Creates a resource descriptor which can be used by message converters such as HtmlFormMessageConverter to create
	 * html forms.
	 * <p>
	 * The following example method searchPersonForm creates a search form which has the method showPerson as action
	 * target:
	 *
	 * <pre>
	 * &#064;RequestMapping(value = &quot;/person&quot;, method = RequestMethod.GET)
	 * public HttpEntity&lt;ResourceDescriptor&gt; searchPersonForm() {
	 * 	ResourceDescriptor rd = ControllerLinkBuilder.linkToResource(&quot;searchPerson&quot;,
	 * 			on(PersonController.class).showPerson(null));
	 * 	return new HttpEntity&lt;ResourceDescriptor&gt;(rd);
	 * }
	 * </pre>
	 *
	 *
	 * @param formName name of the resource, e.g. to be used as form name
	 * @param method reference which will handle the request, use {@link #on(Class)} to create a suitable method reference
	 * @return resource descriptor
	 * @throws IllegalStateException if the method has no request mapping
	 */
	public static FormDescriptor linkToForm(String formName, Object method) {

		Invocations invocations = (Invocations) method;
		List<Invocation> recorded = invocations.getInvocations();
		Invocation invocation = recorded.get(0);
		String classLevelMapping = LinkTemplateUtils.getClassLevelMapping(invocation.getTarget().getClass(),
				RequestMapping.class);
		Method invokedMethod = invocation.getMethod();
		LinkTemplate<PathVariable, RequestParam> linkTemplate = LinkTemplateUtils.createLinkTemplate(classLevelMapping,
				invokedMethod, RequestMapping.class, PathVariable.class, RequestParam.class);

		RequestMethod requestMethod = getRequestMethod(invokedMethod);

		UriTemplate uriTemplate = new UriTemplate(linkTemplate.getLinkTemplate());
		String expanded = uriTemplate.expand(invocation.getArgs()).toASCIIString();

		FormDescriptor formDescriptor = new FormDescriptor(formName, expanded, requestMethod.toString());
		// TODO use variableMap with names to handle non-positional method params correctly
		// for now, users can just reorder the method list so that the path vars come first
		//		Map<String, ?> variableMap = new HashMap<String, String>();
		List<AnnotatedParam<PathVariable>> pathVariables = linkTemplate.getPathVariables();
		for (AnnotatedParam<PathVariable> pathVariable : pathVariables) {
			String paramName = pathVariable.paramAnnotation.value();
			formDescriptor.addPathVariable(paramName, pathVariable.paramType);
//			variableMap.put(paramName, value)
		}
		List<AnnotatedParam<RequestParam>> requestParams = linkTemplate.getRequestParams();
		for (AnnotatedParam<RequestParam> requestParam : requestParams) {
			formDescriptor.addRequestParam(requestParam.paramAnnotation.value(), requestParam.paramType);
		}

		return formDescriptor;
	}

	private static RequestMethod getRequestMethod(Method method) {
		RequestMapping methodRequestMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
		RequestMethod requestMethod;
		if (methodRequestMapping != null) {
			RequestMethod[] methods = methodRequestMapping.method();
			requestMethod = methods[0];
		} else {
			requestMethod = RequestMethod.GET; // default
		}
		return requestMethod;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.hateoas.UriComponentsLinkBuilder#getThis()
	 */
	@Override
	protected ControllerLinkBuilder getThis() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.springframework.hateoas.UriComponentsLinkBuilder#createNewInstance
	 * (org.springframework.web.util.UriComponentsBuilder)
	 */
	@Override
	protected ControllerLinkBuilder createNewInstance(UriComponentsBuilder builder) {
		return new ControllerLinkBuilder(builder);
	}

}
