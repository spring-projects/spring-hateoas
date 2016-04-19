package org.springframework.hateoas.forms;

import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Value object for a HAL-FORMS template. Describes the available state transition details.
 * @see http://mamund.site44.com/misc/hal-forms/
 */
@JsonPropertyOrder({ "title", "method", "contentType", "properties" })
public class Template extends Link {

	private static final long serialVersionUID = 2593020248152501268L;

	public static final String DEFAULT_KEY = "default";

	private List<Property> properties;

	private RequestMethod[] method;

	private String contentType;

	private String title;

	public Template() {
	}

	public Template(String href, String rel) {
		super(href, rel);
	}

	public void setProperties(List<Property> properties) {
		this.properties = properties;
	}

	public void setMethod(RequestMethod[] method) {
		this.method = method;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getContentType() {
		return contentType;
	}

	public String getTitle() {
		return title;
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

	@JsonIgnore
	public RequestMethod[] getMethod() {
		return method;
	}

	@JsonProperty("method")
	public String getMethodStr() {
		StringBuilder sb = new StringBuilder();
		for (RequestMethod rm : method) {
			sb.append(rm.toString()).append(",");
		}
		String methodStr = sb.toString();
		return methodStr.substring(0, methodStr.length() - 1);
	}

}
