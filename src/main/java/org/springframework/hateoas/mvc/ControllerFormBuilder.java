package org.springframework.hateoas.mvc;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.hateoas.FormDescriptor;
import org.springframework.hateoas.core.AnnotationAttribute;
import org.springframework.hateoas.core.AnnotationMappingDiscoverer;
import org.springframework.hateoas.core.DummyInvocationUtils.LastInvocationAware;
import org.springframework.hateoas.core.MappingDiscoverer;
import org.springframework.hateoas.util.AnnotatedParam;
import org.springframework.hateoas.util.LinkTemplate;
import org.springframework.hateoas.util.LinkTemplateUtils;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriTemplate;

public class ControllerFormBuilder {

	private static final MappingDiscoverer DISCOVERER = new AnnotationMappingDiscoverer(RequestMapping.class);
	private static final AnnotatedParametersParameterAccessor pathVariables = new AnnotatedParametersParameterAccessor(
			new AnnotationAttribute(PathVariable.class));
	private static final AnnotatedParametersParameterAccessor requestParams = new AnnotatedParametersParameterAccessor(
			new AnnotationAttribute(RequestParam.class));

	/**
	 * Creates a form descriptor which can be used by message converters such as HtmlFormMessageConverter to create html
	 * forms.
	 * <p>
	 * The following example method searchPersonForm creates a search form which has the method showPerson as action
	 * target:
	 * 
	 * <pre>
	 * &#064;RequestMapping(value = &quot;/person&quot;, method = RequestMethod.GET)
	 * public HttpEntity&lt;FormDescriptor&gt; searchPersonForm() {
	 * 	FormDescriptor rd = ControllerLinkBuilder.linkToResource(&quot;searchPerson&quot;, on(PersonController.class).showPerson(null));
	 * 	return new HttpEntity&lt;FormDescriptor&gt;(rd);
	 * }
	 * </pre>
	 * 
	 * 
	 * @param formName name of the resource, e.g. to be used as form name
	 * @param method reference which will handle the request, use {@link #on(Class)} to create a suitable method reference
	 * @return resource descriptor
	 * @throws IllegalStateException if the method has no request mapping
	 */
	public static FormDescriptor createForm(String formName, Object invocationValue) {

		Assert.isInstanceOf(LastInvocationAware.class, invocationValue);
		LastInvocationAware invocations = (LastInvocationAware) invocationValue;

		MethodInvocation invocation = invocations.getLastInvocation();
		Iterator<Object> classMappingParameters = invocations.getObjectParameters();
		Method invokedMethod = invocation.getMethod();
		UriTemplate template = new UriTemplate(DISCOVERER.getMapping(invokedMethod));
		Map<String, Object> values = new HashMap<String, Object>();

		if (classMappingParameters.hasNext()) {
			for (String variable : template.getVariableNames()) {
				values.put(variable, classMappingParameters.next());
			}
		}

		Map<String, Object> pathVariablesMap = pathVariables.getBoundParameters(invocation);
		values.putAll(pathVariablesMap);
		URI uri = template.expand(values);

		// TODO use on parameter for form default values

		// Invocations invocations = (Invocations) method;
		// List<Invocation> recorded = invocations.getInvocations();
		// Invocation invocation = recorded.get(0);

		String classLevelMapping = DISCOVERER.getMapping(invokedMethod.getDeclaringClass());
		LinkTemplate<PathVariable, RequestParam> linkTemplate = LinkTemplateUtils.createLinkTemplate(classLevelMapping,
				invokedMethod, RequestMapping.class, PathVariable.class, RequestParam.class);

		// UriTemplate uriTemplate = new UriTemplate(linkTemplate.getLinkTemplate());

		String expanded = uri.toASCIIString();
		RequestMethod requestMethod = getRequestMethod(invokedMethod);
		FormDescriptor formDescriptor = new FormDescriptor(formName, expanded, requestMethod.toString());
		// TODO use variableMap with names to handle non-positional method params correctly
		// for now, users can just reorder the method param list so that the path vars come first
		// Map<String, ?> variableMap = new HashMap<String, String>();
//		List<AnnotatedParam<PathVariable>> pathVariables = linkTemplate.getPathVariables();
//		for (AnnotatedParam<PathVariable> pathVariable : pathVariables) {
//			String paramName = pathVariable.paramAnnotation.value();
//			formDescriptor.addPathVariable(paramName, pathVariable.paramType);
			// variableMap.put(paramName, value)
//		}
		for (Entry<String, Object> entry : pathVariablesMap.entrySet()) {
			formDescriptor.addPathVariable(entry.getKey(), entry.getValue().getClass());
		}
		
		Map<String, MethodParameterValue> requestParamMap = requestParams.getBoundMethodParameterValues(invocation);
		for (Entry<String, MethodParameterValue> entry : requestParamMap.entrySet()) {
			formDescriptor.addRequestParam(entry.getKey(), entry.getValue());
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
}
