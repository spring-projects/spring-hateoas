/*
 * Copyright 2014 the original author or authors.
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
package org.springframework.hateoas;

import java.util.List;
import java.util.Map;

/**
 * A link template.
 * 
 * @author Oliver Gierke
 */
public class LinkTemplate extends Link {

	private static final long serialVersionUID = 770560851448247262L;

	private UriTemplate uriTemplate;

	/**
	 * Creates a new {@link LinkTemplate} for the given template string and relation type.
	 * 
	 * @param template must not be {@literal null} or empty.
	 * @param rel must not be {@literal null} or empty.
	 */
	public LinkTemplate(String template, String rel) {

		super(template, rel);

		this.uriTemplate = new UriTemplate(template);
	}

	/**
	 * Default constructor for the marshalling frameworks.
	 */
	protected LinkTemplate() {}

	/**
	 * Returns the variable names contained in the template.
	 * 
	 * @return
	 */
	public List<String> getVariableNames() {
		return uriTemplate.getVariableNames();
	}

	/**
	 * Returns whether the link is templated.
	 * 
	 * @return
	 */
	public boolean isTemplate() {
		return true;
	}

	/**
	 * Turns the current template into a {@link Link} by expanding it using the given parameters.
	 * 
	 * @param arguments
	 * @return
	 */
	public Link toLink(Object... arguments) {
		return new Link(uriTemplate.expand(arguments).toString(), getRel());
	}

	/**
	 * Turns the current template into a {@link Link} by expanding it using the given parameters.
	 * 
	 * @param arguments
	 * @return
	 */
	public Link toLink(Map<String, ? extends Object> arguments) {
		return new Link(uriTemplate.expand(arguments).toString(), getRel());
	}
}
