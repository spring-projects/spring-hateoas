package org.springframework.hateoas;

import java.util.HashMap;
import java.util.Map;

public class ResourceDescriptor {

	private String linkTemplate;
	private Map<String, Class<?>> pathVariables = new HashMap<String, Class<?>>();
	private Map<String, Class<?>> requestParams = new HashMap<String, Class<?>>();

	public ResourceDescriptor(String linkTemplate) {
		this.linkTemplate = linkTemplate;
	}

	public String getLinkTemplate() {
		return linkTemplate;
	}

	public Map<String, Class<?>> getPathVariables() {
		return pathVariables;
	}

	public Map<String, Class<?>> getRequestParams() {
		return requestParams;
	}

	public void addPathVariable(String key, Class<?> type) {
		pathVariables.put(key, type);
	}

	public void addRequestParam(String key, Class<?> type) {
		requestParams.put(key, type);
	}

}
