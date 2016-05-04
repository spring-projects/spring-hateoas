package de.escalon.hypermedia.spring.halforms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.core.MethodParameter;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import de.escalon.hypermedia.action.Input;
import de.escalon.hypermedia.affordance.ActionDescriptor;
import de.escalon.hypermedia.affordance.ActionInputParameter;
import de.escalon.hypermedia.affordance.Affordance;
import de.escalon.hypermedia.spring.BeanUtils;
import de.escalon.hypermedia.spring.BeanUtils.MethodParameterHandler;

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
				ActionDescriptor annotatedParameters, String parentParamName, String paramName, Class<?> parameterType,
				Object propertyValue) {
			boolean readOnly = true;
			String regex = null;
			boolean required = false;
			boolean templated = false;

			// TODO: templated comes from an Input attribute?
			if (methodParameter.hasParameterAnnotation(Input.class)) {
				Input input = methodParameter.getParameterAnnotation(Input.class);

				// input.readOnly or input.editable?
				readOnly = !input.editable();
				regex = StringUtils.isEmpty(input.pattern()) ? null : input.pattern();
				required = input.required();
			}
			String value = propertyValue != null ? propertyValue.toString() : "";
			Property property = new Property(parentParamName + paramName, readOnly, templated, value, "", regex, required,
					null);

			template.getProperties().add(property);

			return property.getName();
		}

	}

}
