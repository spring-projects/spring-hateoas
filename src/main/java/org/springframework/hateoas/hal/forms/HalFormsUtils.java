/*
 * Copyright 2016-2017 the original author or authors.
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

import static org.springframework.hateoas.hal.forms.HalFormsDocument.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.affordance.ActionDescriptor;
import org.springframework.hateoas.affordance.ActionInputParameter;
import org.springframework.hateoas.affordance.ActionInputParameterVisitor;
import org.springframework.hateoas.affordance.Affordance;
import org.springframework.hateoas.affordance.Suggest;
import org.springframework.hateoas.affordance.Suggestions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class HalFormsUtils {

	public static Object toHalFormsDocument(final Object object, final ObjectMapper objectMapper) {

		if (object == null) {
			return null;
		}

		if (object instanceof ResourceSupport) {
			ResourceSupport rs = (ResourceSupport) object;

			List<Link> links = new ArrayList<Link>();
			List<Template> templates = new ArrayList<Template>();

			process(rs, links, templates, objectMapper);

			return halFormsDocument()
				.links(links)
				.templates(templates)
				.build();
		} else { // bean
			return object;
		}
	}

	@SuppressWarnings("unused")
	private static void process(ResourceSupport resource, List<Link> links, List<Template> templates,
			ObjectMapper objectMapper) {

		for (Link link : resource.getLinks()) {
			if (link instanceof Affordance) {
				Affordance affordance = (Affordance) link;

				for (int i = 0; i < affordance.getActionDescriptors().size(); i++) {

					ActionDescriptor actionDescriptor = affordance.getActionDescriptors().get(i);

					if (i == 0) {
						links.add(affordance);
					} else {
						String key = actionDescriptor.getSemanticActionType();
						if (true || actionDescriptor.hasRequestBody() || !actionDescriptor.getRequestParamNames().isEmpty()) {

							Template template = templates.isEmpty() ? new Template()
									: new Template(key != null ? key : actionDescriptor.getHttpMethod().toString());
							
							template.setContentType(actionDescriptor.getConsumes());

							template.setHttpMethod(actionDescriptor.getHttpMethod());

							actionDescriptor.accept(
								new TemplateActionInputParameterVisitor(template, actionDescriptor, objectMapper));

							templates.add(template);
						}
					}
				}
			} else {
				links.add(link);
			}
		}
	}

	public static Property getProperty(ActionInputParameter actionInputParameter, ActionDescriptor actionDescriptor,
			Object propertyValue, String name, ObjectMapper objectMapper) {

		Map<String, Object> inputConstraints = actionInputParameter.getInputConstraints();

		// TODO: templated comes from an Input attribute?
		boolean templated = false;

		boolean readOnly = inputConstraints.containsKey(ActionInputParameter.EDITABLE)
				? !((Boolean) inputConstraints.get(ActionInputParameter.EDITABLE)) : true;
		String regex = (String) inputConstraints.get(ActionInputParameter.PATTERN);
		boolean required = inputConstraints.containsKey(ActionInputParameter.REQUIRED)
				? (Boolean) inputConstraints.get(ActionInputParameter.REQUIRED) : false;

		String value = null;

		List<Suggest<Object>> possibleValues = actionInputParameter
				.getPossibleValues(actionDescriptor);

		boolean multi = false;

		if (!possibleValues.isEmpty()) {
			try {
				if (propertyValue != null) {
					if (propertyValue.getClass().isEnum()) {
						value = propertyValue.toString();
					} else {
						value = objectMapper.writeValueAsString(propertyValue);
					}
				}
			} catch (JsonProcessingException e) {}

			if (actionInputParameter.isArrayOrCollection()) {
				multi = true;
			}
			List<Object> values = new ArrayList<Object>();

			for (Suggest<Object> possibleValue : possibleValues) {
				values.add(possibleValue.getValue());
			}

		} else {
			if (propertyValue != null) {
				try {
					if (propertyValue instanceof List || propertyValue.getClass().isArray()) {
						value = objectMapper.writeValueAsString(propertyValue);
					} else {
						value = propertyValue.toString();
					}
				} catch (JsonProcessingException e) {}
			}
		}

		Suggestions suggestions = actionInputParameter.getSuggestions();

		return new Property(name, readOnly, value, null, regex, templated, required, multi,
				suggestions.equals(Suggestions.NONE) ? null : suggestions);
	}

	@RequiredArgsConstructor
	static class TemplateActionInputParameterVisitor implements ActionInputParameterVisitor {

		private final Template template;
		private final ActionDescriptor actionDescriptor;
		private final ObjectMapper objectMapper;

		/*
		 * (non-Javadoc)
		 * @see ActionInputParameterVisitor#visit(ActionInputParameter)
		 */
		@Override
		public void visit(ActionInputParameter inputParameter) {

			Property property = getProperty(inputParameter, actionDescriptor, inputParameter.getValue(),
					inputParameter.getName(), objectMapper);

			template.getProperties().add(property);
		}
	}
}
