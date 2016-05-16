package de.escalon.hypermedia.spring.halforms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;

import de.escalon.hypermedia.action.Input;
import de.escalon.hypermedia.affordance.ActionDescriptor;
import de.escalon.hypermedia.affordance.ActionInputParameter;
import de.escalon.hypermedia.affordance.ActionInputParameterVisitor;
import de.escalon.hypermedia.affordance.Affordance;
import de.escalon.hypermedia.affordance.SuggestType;
import de.escalon.hypermedia.spring.halforms.ValueSuggest.ValueSuggestType;

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

					// TODO: revisar!
					// if ("application/prs.hal-forms+json".equals(actionDescriptor.getProduces())) {
					if (actionDescriptor.getRequestParamNames() != null
							&& actionDescriptor.getRequestParamNames().contains("rel")) {
						processed.add(affordance);
					} else {
						String key = actionDescriptor.getSemanticActionType();
						Template template = new Template(link.getHref(), key != null ? key : "default");
						template.setContentType(actionDescriptor.getConsumes());

						// there is only one httpmethod??
						template.setMethod(actionDescriptor.getHttpMethod());
						actionDescriptor.accept(new TemplateActionInputParameterVisitor(template, actionDescriptor));
						processed.add(template);
					}
				}
			} else {
				processed.add(link);
			}
		}
		return processed;
	}

	public static Property getProperty(ActionInputParameter actionInputParameter, ActionDescriptor actionDescriptor,
			Object propertyValue, String name) {
		Map<String, Object> inputConstraints = actionInputParameter.getInputConstraints();

		// TODO: templated comes from an Input attribute?
		boolean templated = false;
		// FIXME: input.readOnly or input.editable?
		boolean readOnly = inputConstraints.containsKey(Input.EDITABLE) ? !((Boolean) inputConstraints.get(Input.EDITABLE))
				: true;
		String regex = inputConstraints.containsKey(Input.PATTERN) ? (String) inputConstraints.get(Input.PATTERN) : null;
		boolean required = inputConstraints.containsKey(Input.REQUIRED) ? (Boolean) inputConstraints.get(Input.REQUIRED)
				: false;

		String value = propertyValue != null ? propertyValue.toString() : null;

		// FIXME: we need suggest type!
		final de.escalon.hypermedia.affordance.Suggest<Object>[] possibleValues = actionInputParameter
				.getPossibleValues(actionDescriptor);
		ValueSuggest<?> suggest = null;
		SuggestType suggestType = SuggestType.INTERNAL;
		if (possibleValues.length > 0) {
			String textField = null;
			String valueField = null;
			List<Object> values = new ArrayList<Object>();
			for (de.escalon.hypermedia.affordance.Suggest<Object> possibleValue : possibleValues) {
				values.add(possibleValue.getValue());
				textField = possibleValue.getTextField();
				valueField = possibleValue.getValueField();
				suggestType = possibleValue.getType();
			}
			ValueSuggestType valueSuggestType = ValueSuggestType.valueOf(suggestType);
			suggest = new ValueSuggest<Object>(values, textField, valueField, valueSuggestType);
		}

		return new Property(name, readOnly, templated, value, null, regex, required, suggest);
	}

	static class TemplateActionInputParameterVisitor implements ActionInputParameterVisitor {

		private final Template template;
		private final ActionDescriptor actionDescriptor;

		public TemplateActionInputParameterVisitor(Template template, ActionDescriptor actionDescriptor) {
			this.template = template;
			this.actionDescriptor = actionDescriptor;
		}

		@Override
		public String visit(ActionInputParameter inputParameter) {

			Property property = getProperty(inputParameter, actionDescriptor, inputParameter.getValue(),
					inputParameter.getName());

			template.getProperties().add(property);

			return property.getName();
		}

	}

}
