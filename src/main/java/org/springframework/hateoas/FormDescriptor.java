package org.springframework.hateoas;

import java.util.HashMap;
import java.util.Map;

import org.springframework.hateoas.mvc.MethodParameterValue;

/**
 * Describes a form that is suitable to call a Controller method which handles the request built by the form.
 * 
 * @author Dietrich Schulten
 * 
 */
public class FormDescriptor {

	// TODO maybe separate expanded and non-expanded link template

	private String linkTemplate;
	private Map<String, Class<?>> pathVariables = new HashMap<String, Class<?>>();
	private Map<String, MethodParameterValue> requestParams = new HashMap<String, MethodParameterValue>();
	private String httpMethod;
	private String resourceName;

	public String getResourceName() {
		return resourceName;
	}

	public FormDescriptor(String resourceName, String linkTemplate, String httpMethod) {
		this.linkTemplate = linkTemplate;
		this.httpMethod = httpMethod;
		this.resourceName = resourceName;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public String getLinkTemplate() {
		return linkTemplate;
	}

	public Map<String, Class<?>> getPathVariables() {
		return pathVariables;
	}

	public Map<String, MethodParameterValue> getRequestParams() {
		return requestParams;
	}

	public void addPathVariable(String key, Class<?> type) {
		pathVariables.put(key, type);
	}

	public void addRequestParam(String key, MethodParameterValue methodParameterValue) {
		requestParams.put(key, methodParameterValue);
	}

}
