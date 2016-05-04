package de.escalon.hypermedia.spring.halforms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.core.MethodParameter;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.web.bind.annotation.RequestMethod;

import de.escalon.hypermedia.action.Input;
import de.escalon.hypermedia.affordance.ActionDescriptor;
import de.escalon.hypermedia.affordance.ActionInputParameter;
import de.escalon.hypermedia.affordance.Affordance;
import de.escalon.hypermedia.spring.BeanUtils;
import de.escalon.hypermedia.spring.BeanUtils.MethodParameterHandler;
import de.escalon.hypermedia.spring.SpringActionInputParameter;

public class HalFormsUtils {

	public static Object toHalFormsDocument(Object object) {
		if (object == null) {
			return null;
		}

		if (object instanceof ResourceSupport) {
			ResourceSupport rs = (ResourceSupport) object;
			return new HalFormsDocument(getLinks(rs));

		} else { // bean
			return object;
		}
	}

	private static List<Link> getLinks(ResourceSupport resource) {
		List<Link> processed = new ArrayList<Link>();
		for (Link link : resource.getLinks()) {
			if (link instanceof Affordance) {
				Affordance affordance = (Affordance) link;
				for (ActionDescriptor actionDescriptor : affordance.getActionDescriptors()) {
					Template template = new Template(link.getHref());
					template.setContentType(actionDescriptor.getContentType());

					// there is only one httpmethod??
					template.setMethod(new RequestMethod[] { RequestMethod.valueOf(actionDescriptor.getHttpMethod()) });

					if (actionDescriptor.hasRequestBody()) {

						// TODO: add params to Template defined by Java API
						BeanUtils.recurseBeanCreationParams(actionDescriptor.getRequestBody().getParameterType(), actionDescriptor,
								actionDescriptor.getRequestBody(), actionDescriptor.getRequestBody().getValue(), "",
								Collections.<String> emptySet(), new TemplateMethodParameterHandler(template));

						// TODO: templated GETs
					} else if (!actionDescriptor.getRequestParamNames().isEmpty()) {}

					processed.add(template);
				}
			} else {
				processed.add(link);
			}
		}
		return processed;
	}

	public static class TemplateMethodParameterHandler implements MethodParameterHandler {

		private final Template template;

		public TemplateMethodParameterHandler(Template template) {
			this.template = template;
			if (this.template.getProperties() == null) {
				this.template.setProperties(new ArrayList<Property>());
			}
		}

		@Override
		public String onMethodParameter(MethodParameter methodParameter, ActionInputParameter annotatedParameter,
				ActionDescriptor actionDescriptor, String parentParamName, String paramName, Class<?> parameterType,
				Object propertyValue) {

			// TODO: templated comes from an Input attribute?
			boolean templated = false;

			ActionInputParameter constructorParamInputParameter = new SpringActionInputParameter(methodParameter,
					propertyValue);

			Map<String, Object> inputConstraints = constructorParamInputParameter.getInputConstraints();

			// FIXME: input.readOnly or input.editable?
			boolean readOnly = inputConstraints.containsKey(Input.EDITABLE)
					? !((Boolean) inputConstraints.get(Input.EDITABLE)) : true;
			String regex = inputConstraints.containsKey(Input.PATTERN) ? (String) inputConstraints.get(Input.PATTERN) : null;
			boolean required = inputConstraints.containsKey(Input.REQUIRED) ? (Boolean) inputConstraints.get(Input.REQUIRED)
					: false;

			String value = propertyValue != null ? propertyValue.toString() : null;

			final de.escalon.hypermedia.affordance.Suggest<Object>[] possibleValues = constructorParamInputParameter
					.getPossibleValues(actionDescriptor);
			ValueSuggest<?> suggest = null;
			if (possibleValues.length > 0) {

				String textField = null;
				String valueField = null;
				List<Object> values = new ArrayList<Object>();
				for (de.escalon.hypermedia.affordance.Suggest<Object> possibleValue : possibleValues) {
					values.add(possibleValue.getValue());
					textField = possibleValue.getTextField();
					valueField = possibleValue.getValueField();
				}
				suggest = new ValueSuggest<Object>(values, textField, valueField);
			}

			Property property = new Property(parentParamName + paramName, readOnly, templated, value, null, regex, required,
					suggest);

			template.getProperties().add(property);

			return property.getName();
		}

	}

}
