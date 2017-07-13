/*
 * Copyright 2017 the original author or authors.
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
package org.springframework.hateoas.hal.forms;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.AffordanceModel;
import org.springframework.hateoas.core.DummyInvocationUtils.MethodInvocation;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.util.UriComponents;

/**
 * {@link AffordanceModel} for a HAL-FORMS {@link org.springframework.http.MediaType}.
 * 
 * @author Greg Turnquist
 */
public class HalFormsAffordanceModel implements AffordanceModel {

	private static final Logger log = LoggerFactory.getLogger(HalFormsAffordanceModel.class);

	/**
	 * Details about the affordance's 
	 */
	private final UriComponents components;

	/**
	 * Is this required/not required?
	 */
	private final boolean required;

	/**
	 * {@link Map} of property names and their types associated with the incoming request body.
	 */
	private final Map<String, Class<?>> properties;

	public HalFormsAffordanceModel(Affordance affordance, MethodInvocation invocationValue, UriComponents components) {

		this.components = components;
		this.required = determineRequired(affordance.getHttpMethod());

		this.properties = new TreeMap<String, Class<?>>();

		if (affordance.getHttpMethod().equalsIgnoreCase("POST") ||
			affordance.getHttpMethod().equalsIgnoreCase("PUT") ||
			affordance.getHttpMethod().equalsIgnoreCase("PATCH")) {
			
			determineAffordanceInputs(invocationValue.getMethod());
		}
	}

	/**
	 * Transform the details of the Spring MVC method's {@link RequestBody} into a collection of {@link HalFormsProperty}s.
	 * 
	 * @return
	 */
	public List<HalFormsProperty> getProperties() {

		List<HalFormsProperty> halFormsProperties = new ArrayList<HalFormsProperty>();

		for (Map.Entry<String, Class<?>> entry : this.properties.entrySet()) {
			halFormsProperties.add(new HalFormsProperty(entry.getKey(), null, null, null, null, false, this.required, false));
		}

		return halFormsProperties;
	}

	/**
	 * Look up the path of the {@link UriComponents}.
	 * 
	 * @return
	 */
	public String getPath() {
		return this.components.getPath();
	}

	/**
	 * Based on the Spring MVC controller's HTTP method, decided whether or not input attributes are required or not.
	 *
	 * @param httpMethod - string representation of an HTTP method, e.g. GET, POST, etc.
	 * @return
	 */
	private boolean determineRequired(String httpMethod) {

		if (httpMethod.equalsIgnoreCase("POST") || httpMethod.equalsIgnoreCase("PUT")) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Look at the inputs for a Spring MVC controller method to decide the {@link Affordance}'s properties.
	 *
	 * @param method - {@link Method} of the Spring MVC controller tied to this affordance
	 */
	private void determineAffordanceInputs(Method method) {

		if (method == null) {
			return;
		}

		log.debug("Gathering details about " + method.getDeclaringClass().getCanonicalName() + "." + method.getName());

		for (int i = 0; i < method.getParameterTypes().length; i++) {

			for (Annotation annotation : method.getParameterAnnotations()[i]) {

				if (annotation.annotationType().equals(RequestBody.class)) {

					log.debug("\tRequest body: " + method.getParameterTypes()[i].getCanonicalName() + "(");

					for (PropertyDescriptor descriptor : BeanUtils.getPropertyDescriptors(method.getParameterTypes()[i])) {

						if (!descriptor.getName().equals("class")) {
							log.debug("\t\t" + descriptor.getPropertyType().getCanonicalName() + " " + descriptor.getName());
							this.properties.put(descriptor.getName(), descriptor.getPropertyType());
						}
					}
					log.debug(")");
				}
			}
		}

		log.debug("Assembled " + this.toString());
	}
}
