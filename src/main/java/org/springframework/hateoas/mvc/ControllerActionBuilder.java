package org.springframework.hateoas.mvc;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.hateoas.HtmlResourceMessageConverter;
import org.springframework.hateoas.action.ActionDescriptor;
import org.springframework.hateoas.core.AnnotationAttribute;
import org.springframework.hateoas.core.DummyInvocationUtils.LastInvocationAware;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

public class ControllerActionBuilder {

	private static final AnnotatedParametersParameterAccessor REQUEST_PARAM_ACCESSOR = new AnnotatedParametersParameterAccessor(
			new AnnotationAttribute(RequestParam.class));

	/**
	 * Creates an ActionDescriptor which tells a hypermedia client how to pass data to a resource, e.g. how to construct a
	 * POST or PUT body or how to fill in GET parameters. The action descriptor knows the names of the expected
	 * parameters, their types, possible values and default values.
	 * <p>
	 * The action descriptor can be used by message converters to create a response in a hypermedia-enabled media type
	 * (see below for examples). Another possibility is to generate a json schema or a html documentation page from the
	 * action descriptor and make it available at a custom rel's URI.
	 * <p>
	 * For instance, the {@link HtmlResourceMessageConverter} can create xhtml forms, which can be used by hypermedia-enabled
	 * clients.
	 * <p>
	 * The following example method searchPersonForm creates a search form which has the method showPerson as action
	 * target. It is returned by the application if the client requests the /person resource without parameters.
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
	 * @see HtmlResourceMessageConverter
	 * @see <a href ="http://oredev.org/2010/sessions/hypermedia-apis">Hypermedia APIs with xhtml</a>
	 * @see <a href="https://github.com/kevinswiber/siren">Siren Hypermedia Format</a>
	 * @see <a href="http://tools.ietf.org/html/draft-kelly-json-hal-05#appendix-B.5">Why does HAL have no forms</a>
	 */
	public static ActionDescriptor createActionFor(Object invocationValue, String actionName) {

		Assert.isInstanceOf(LastInvocationAware.class, invocationValue);
		LastInvocationAware invocations = (LastInvocationAware) invocationValue;

		MethodInvocation invocation = invocations.getLastInvocation();
		Method invokedMethod = invocation.getMethod();

		ControllerLinkBuilder linkBuilder = ControllerLinkBuilder.linkTo(invocationValue);
		UriComponents uri = linkBuilder.toUriComponentsBuilder().build();
		UriComponentsBuilder actionUriBuilder = UriComponentsBuilder.newInstance();
		UriComponents actionUri = actionUriBuilder.scheme(uri.getScheme()).userInfo(uri.getUserInfo()).host(uri.getHost())
				.port(uri.getPort()).path(uri.getPath()).build();
		RequestMethod requestMethod = getRequestMethod(invokedMethod);
		ActionDescriptor actionDescriptor = new ActionDescriptor(actionName, actionUri, requestMethod);

		// the action descriptor needs to know the param type, value and name
		Map<String, MethodParameterValue> requestParamMap = REQUEST_PARAM_ACCESSOR
				.getBoundMethodParameterValues(invocation);
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
