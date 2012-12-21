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

	private String actionLink;
	private Map<String, Class<?>> pathVariables = new HashMap<String, Class<?>>();
	private Map<String, MethodParameterValue> requestParams = new HashMap<String, MethodParameterValue>();
	private String httpMethod;
	private String formName;

	public FormDescriptor(String resourceName, String linkTemplate, String httpMethod) {
		this.actionLink = linkTemplate;
		this.httpMethod = httpMethod;
		this.formName = resourceName;
	}

	public String getFormName() {
		return formName;
	}
	
	public String getHttpMethod() {
		return httpMethod;
	}

	public String getActionLink() {
		return actionLink;
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
