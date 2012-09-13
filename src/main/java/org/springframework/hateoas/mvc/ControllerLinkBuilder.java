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

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.UriComponentsLinkBuilder;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

/**
 * Builder to ease building {@link Link} instances pointing to Spring MVC controllers.
 * 
 * @author Oliver Gierke
 */
public class ControllerLinkBuilder {

	/**
	 * Creates a new {@link ControllerLinkBuilder} with a base of the mapping annotated to the given controller class.
	 * 
	 * @param controller must not be {@literal null}.
	 * @return
	 */
	public static UriComponentsLinkBuilder linkTo(Class<?> controller) {
		return linkTo(controller, new Object[0]);
	}

	/**
	 * Creates a new {@link ControllerLinkBuilder} with a base of the mapping annotated to the given controller class. The
	 * additional parameters are used to fill up potentially available path variables in the class scop request mapping.
	 * 
	 * @param controller must not be {@literal null}.
	 * @param parameters
	 * @return
	 */
	public static UriComponentsLinkBuilder linkTo(Class<?> controller, Object... parameters) {

		Assert.notNull(controller);

		RequestMapping annotation = AnnotationUtils.findAnnotation(controller, RequestMapping.class);
		String[] mapping = annotation == null ? new String[0] : (String[]) AnnotationUtils.getValue(annotation);

		if (mapping.length > 1) {
			throw new IllegalStateException("Multiple controller mappings defined! Unable to build URI!");
		}

		UriComponentsLinkBuilder builder = new UriComponentsLinkBuilder(ServletUriComponentsBuilder.fromCurrentServletMapping());

		if (mapping.length == 0) {
			return builder;
		}

		UriTemplate template = new UriTemplate(mapping[0]);
		return builder.slash(template.expand(parameters));
	}

}
