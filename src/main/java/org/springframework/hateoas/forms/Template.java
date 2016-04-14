package org.springframework.hateoas.forms;

import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Value object for a HAL-FORMS template. Describes the available state transition details.
 * @see http://mamund.site44.com/misc/hal-forms/
 */
public class Template extends Link {

	private static final long serialVersionUID = 2593020248152501268L;

	public static final String DEFAULT_KEY = "default";

	private List<Property> properties;

	private RequestMethod[] method;

	public Template(String href, String rel) {
		super(href, rel);
	}

	public void setProperties(List<Property> properties) {
		this.properties = properties;
	}

	public void setMethod(RequestMethod[] method) {
		this.method = method;
	}

	public Property getProperty(String propertyName) {
		for (Property property : properties) {
			if (property.getName().equals(propertyName)) {
				return property;
			}
		}
		return null;
	}

	public List<Property> getProperties() {
		return properties;
	}

	public RequestMethod[] getMethod() {
		return method;
	}

}
