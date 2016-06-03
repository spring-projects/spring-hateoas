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

public class HalFormsUtils {

	public static Object toHalFormsDocument(final Object object) {
		if (object == null) {
			return null;
		}

		if (object instanceof ResourceSupport) {
			ResourceSupport rs = (ResourceSupport) object;
			List<Template> templates = new ArrayList<Template>();
			List<Link> links = new ArrayList<Link>();
			process(rs, links, templates);
			return new HalFormsDocument(links, templates);

		} else { // bean
			return object;
		}
	}

	private static void process(final ResourceSupport resource, final List<Link> links, final List<Template> templates) {
		for (Link link : resource.getLinks()) {
			if (link instanceof Affordance) {
				Affordance affordance = (Affordance) link;

				// TODO: review! the first ActionDescriptor corresponds to the "self" link therefore does never add a template
				for (int i = 0; i < affordance.getActionDescriptors().size(); i++) {
					ActionDescriptor actionDescriptor = affordance.getActionDescriptors().get(i);
					if (i == 0) {
						links.add(affordance);
					} else {
						String key = actionDescriptor.getSemanticActionType();
						if (true || actionDescriptor.hasRequestBody() || !actionDescriptor.getRequestParamNames().isEmpty()) {
							Template template = templates.isEmpty() ? new Template()
									: new Template(key != null ? key : actionDescriptor.getHttpMethod().toLowerCase());
							template.setContentType(actionDescriptor.getConsumes());

							// there is only one httpmethod??
							template.setMethod(actionDescriptor.getHttpMethod());
							TemplateActionInputParameterVisitor visitor = new TemplateActionInputParameterVisitor(template,
									actionDescriptor);
							actionDescriptor.accept(visitor);

							templates.add(template);
						}
					}
				}
			} else {
				links.add(link);
			}
		}

	}

	public static Property getProperty(final ActionInputParameter actionInputParameter,
			final ActionDescriptor actionDescriptor, final Object propertyValue, final String name) {
		Map<String, Object> inputConstraints = actionInputParameter.getInputConstraints();

		// TODO: templated comes from an Input attribute?
		boolean templated = false;
		// FIXME: input.readOnly or input.editable?
		boolean readOnly = inputConstraints.containsKey(Input.EDITABLE) ? !((Boolean) inputConstraints.get(Input.EDITABLE))
				: true;
		String regex = inputConstraints.containsKey(Input.PATTERN) ? (String) inputConstraints.get(Input.PATTERN) : null;
		boolean required = inputConstraints.containsKey(Input.REQUIRED) ? (Boolean) inputConstraints.get(Input.REQUIRED)
				: false;

		final de.escalon.hypermedia.affordance.Suggest<Object>[] possibleValues = actionInputParameter
				.getPossibleValues(actionDescriptor);
		ValueSuggest<?> suggest = null;
		SuggestType suggestType = SuggestType.INTERNAL;
		boolean multi = false;
		if (possibleValues.length > 0) {
			if (actionInputParameter.isArrayOrCollection()) {
				multi = true;
			}
			String textField = null;
			String valueField = null;
			List<Object> values = new ArrayList<Object>();
			for (de.escalon.hypermedia.affordance.Suggest<Object> possibleValue : possibleValues) {
				values.add(possibleValue.getValue());
				textField = possibleValue.getTextField();
				valueField = possibleValue.getValueField();
				suggestType = possibleValue.getType();
			}
			suggest = new ValueSuggest<Object>(values, textField, valueField, suggestType);
		}

		return new Property(name, readOnly, templated, propertyValue, null, regex, required, multi, suggest);
	}

	static class TemplateActionInputParameterVisitor implements ActionInputParameterVisitor {

		private final Template template;

		private final ActionDescriptor actionDescriptor;

		public TemplateActionInputParameterVisitor(final Template template, final ActionDescriptor actionDescriptor) {
			this.template = template;
			this.actionDescriptor = actionDescriptor;
		}

		@Override
		public void visit(ActionInputParameter inputParameter) {
			System.out.println(
					"input:" + inputParameter.hashCode() + " " + inputParameter.getName() + " " + inputParameter.isReadOnly(""));
			Property property = getProperty(inputParameter, actionDescriptor, inputParameter.getValue(),
					inputParameter.getName());

			template.getProperties().add(property);
		}

	}

}
