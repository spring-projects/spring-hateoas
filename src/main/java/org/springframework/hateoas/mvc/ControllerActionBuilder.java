package org.springframework.hateoas.mvc;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.hateoas.action.ActionDescriptor;
import org.springframework.hateoas.core.AnnotationAttribute;
import org.springframework.hateoas.core.AnnotationMappingDiscoverer;
import org.springframework.hateoas.core.DummyInvocationUtils.LastInvocationAware;
import org.springframework.hateoas.core.MappingDiscoverer;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriTemplate;

public class ControllerActionBuilder {

	private static final MappingDiscoverer DISCOVERER = new AnnotationMappingDiscoverer(RequestMapping.class);
	private static final AnnotatedParametersParameterAccessor pathVariables = new AnnotatedParametersParameterAccessor(
			new AnnotationAttribute(PathVariable.class));
	private static final AnnotatedParametersParameterAccessor requestParams = new AnnotatedParametersParameterAccessor(
			new AnnotationAttribute(RequestParam.class));

	/**
	 * Creates an action descriptor which can be used by message converters such as HtmlResourceMessageConverter to create
	 * html forms.
	 * <p>
	 * The following example method searchPersonForm creates a search form which has the method showPerson as action
	 * target.
	 * 
	 * <pre>
	 * &#064;RequestMapping(value = &quot;/person&quot;)
	 * public HttpEntity&lt;FormDescriptor&gt; searchPersonForm() {
	 * 	ActionDescriptor rd = ControllerActionBuilder.createActionFor(&quot;searchPerson&quot;, methodOn(PersonController.class)
	 * 			.showPerson(null));
	 * 	return new HttpEntity&lt;ActionDescriptor&gt;(rd);
	 * }
	 * 
	 * &#064;RequestMapping(value = &quot;/person&quot;, method = RequestMethod.GET, params = { &quot;personId&quot; })
	 * 	public HttpEntity&lt;? extends Object&gt; showPerson(@RequestParam(value = &quot;personId&quot;) Long personId) {
	 * 		...
	 * }
	 * 
	 * </pre>
	 * 
	 * If you want to predefine a default value for the searchPerson request parameter, pass it into the method
	 * invocation.
	 * 
	 * <pre>
	 * methodOn(PersonController.class).showPerson(1234L);
	 * </pre>
	 * 
	 * This way, the form will have a predefined value of 1234 in the personId form field.
	 * 
	 * @param invocationValue method reference which will handle the request, use
	 *          {@link ControllerLinkBuilder#methodOn(Class, Object...)} to create a suitable method reference
	 * @param actionName name of the action, e.g. to be used as a form name
	 * 
	 * @return resource descriptor
	 * @throws IllegalStateException if the method has no request mapping
	 */
	public static ActionDescriptor createActionFor(Object invocationValue, String actionName) {

		Assert.isInstanceOf(LastInvocationAware.class, invocationValue);
		LastInvocationAware invocations = (LastInvocationAware) invocationValue;

		MethodInvocation invocation = invocations.getLastInvocation();
		Iterator<Object> classMappingParameters = invocations.getObjectParameters();
		Method invokedMethod = invocation.getMethod();
		UriTemplate template = new UriTemplate(DISCOVERER.getMapping(invokedMethod));
		Map<String, Object> values = new HashMap<String, Object>();

		Iterator<String> templateVariables = template.getVariableNames().iterator();
		while (classMappingParameters.hasNext() && templateVariables.hasNext()) {
			values.put(templateVariables.next(), classMappingParameters.next());
		}

		Map<String, Object> pathVariablesMap = pathVariables.getBoundParameters(invocation);
		values.putAll(pathVariablesMap);
		URI uri = template.expand(values);

		String expanded = uri.toASCIIString();
		RequestMethod requestMethod = getRequestMethod(invokedMethod);
		ActionDescriptor actionDescriptor = new ActionDescriptor(actionName, expanded, requestMethod);

		Map<String, MethodParameterValue> requestParamMap = requestParams.getBoundMethodParameterValues(invocation);
		for (Entry<String, MethodParameterValue> entry : requestParamMap.entrySet()) {
			actionDescriptor.addRequestParam(entry.getKey(), entry.getValue());
		}

		return actionDescriptor;
	}

	private static RequestMethod getRequestMethod(Method method) {
		RequestMapping methodRequestMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
		RequestMethod requestMethod;
		if (methodRequestMapping != null) {
			RequestMethod[] methods = methodRequestMapping.method();
			if (methods.length == 0) {
				requestMethod = RequestMethod.GET;
			} else {
				requestMethod = methods[0];
			}
		} else {
			requestMethod = RequestMethod.GET; // default
		}
		return requestMethod;
	}
}
