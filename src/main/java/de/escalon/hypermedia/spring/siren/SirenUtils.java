package de.escalon.hypermedia.spring.siren;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.TemplateVariable;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.core.DefaultRelProvider;
import org.springframework.util.ObjectUtils;

import de.escalon.hypermedia.PropertyUtils;
import de.escalon.hypermedia.action.Type;
import de.escalon.hypermedia.affordance.ActionDescriptor;
import de.escalon.hypermedia.affordance.ActionInputParameter;
import de.escalon.hypermedia.affordance.ActionInputParameterVisitor;
import de.escalon.hypermedia.affordance.Affordance;
import de.escalon.hypermedia.affordance.DataType;
import de.escalon.hypermedia.affordance.Suggest;
import de.escalon.hypermedia.spring.DefaultDocumentationProvider;
import de.escalon.hypermedia.spring.DocumentationProvider;

/**
 * Maps spring-hateoas response data to siren data. Created by Dietrich on 17.04.2016.
 */
public class SirenUtils {

	private static final Set<String> FILTER_RESOURCE_SUPPORT = new HashSet<String>(Arrays.asList("class", "links", "id"));
	private String requestMediaType;

	private final Set<String> navigationalRels = new HashSet<String>(Arrays.asList("self", "next", "previous", "prev"));

	private RelProvider relProvider = new DefaultRelProvider();

	private DocumentationProvider documentationProvider = new DefaultDocumentationProvider();

	public void toSirenEntity(SirenEntityContainer objectNode, Object object) {
		if (object == null) {
			return;
		}
		try {
			if (object instanceof Resource) {
				Resource<?> resource = (Resource<?>) object;
				objectNode.setLinks(toSirenLinks(getNavigationalLinks(resource.getLinks())));
				objectNode.setEmbeddedLinks(toSirenEmbeddedLinks(getEmbeddedLinks(resource.getLinks())));
				objectNode.setActions(toSirenActions(getActions(resource.getLinks())));
				toSirenEntity(objectNode, resource.getContent());
				return;
			} else if (object instanceof Resources) {
				Resources<?> resources = (Resources<?>) object;

				objectNode.setLinks(toSirenLinks(getNavigationalLinks(resources.getLinks())));
				Collection<?> content = resources.getContent();
				toSirenEntity(objectNode, content);
				objectNode.setActions(toSirenActions(getActions(resources.getLinks())));
				return;
			} else if (object instanceof ResourceSupport) {
				ResourceSupport resource = (ResourceSupport) object;
				objectNode.setLinks(toSirenLinks(getNavigationalLinks(resource.getLinks())));
				objectNode.setEmbeddedLinks(toSirenEmbeddedLinks(getEmbeddedLinks(resource.getLinks())));
				objectNode.setActions(toSirenActions(getActions(resource.getLinks())));

				// wrap object attributes below to avoid endless loop

			} else if (object instanceof Collection) {
				Collection<?> collection = (Collection<?>) object;
				for (Object item : collection) {
					SirenEmbeddedRepresentation child = new SirenEmbeddedRepresentation();
					toSirenEntity(child, item);
					objectNode.addSubEntity(child);
				}
				return;
			}
			if (object instanceof Map) {
				Map<?, ?> map = (Map<?, ?>) object;
				Map<String, Object> propertiesNode = new HashMap<String, Object>();
				objectNode.setProperties(propertiesNode);
				for (Map.Entry<?, ?> entry : map.entrySet()) {
					String key = entry.getKey().toString();
					Object content = entry.getValue();

					String docUrl = documentationProvider.getDocumentationUrl(key, content);
					traverseAttribute(objectNode, propertiesNode, key, docUrl, content);
				}
			} else { // bean or ResourceSupport
				objectNode.setSirenClasses(getSirenClasses(object));
				Map<String, Object> propertiesNode = new HashMap<String, Object>();
				createRecursiveSirenEntitiesFromPropertiesAndFields(objectNode, propertiesNode, object);
				objectNode.setProperties(propertiesNode);
			}
		} catch (Exception ex) {
			throw new RuntimeException("failed to transform object " + object, ex);
		}
	}

	private List<String> getSirenClasses(Object object) {
		List<String> sirenClasses;
		String sirenClass = relProvider.getItemResourceRelFor(object.getClass());
		if (sirenClass != null) {
			sirenClasses = Collections.singletonList(sirenClass);
		} else {
			sirenClasses = Collections.emptyList();
		}
		return sirenClasses;
	}

	private List<Link> getEmbeddedLinks(List<Link> links) {
		List<Link> ret = new ArrayList<Link>();
		for (Link link : links) {
			if (!navigationalRels.contains(link.getRel())) {
				if (link instanceof Affordance) {
					Affordance affordance = (Affordance) link;
					List<ActionDescriptor> actionDescriptors = affordance.getActionDescriptors();
					for (ActionDescriptor actionDescriptor : actionDescriptors) {
						if ("GET".equals(actionDescriptor.getHttpMethod()) && !affordance.isTemplated()) {
							ret.add(link);
						}
					}
				} else {
					ret.add(link);
				}
			}
		}
		return ret;
	}

	private List<Link> getNavigationalLinks(List<Link> links) {
		List<Link> ret = new ArrayList<Link>();
		for (Link link : links) {
			if (navigationalRels.contains(link.getRel())) {
				ret.add(link);
			}
		}
		return ret;
	}

	private List<Link> getActions(List<Link> links) {
		List<Link> ret = new ArrayList<Link>();
		for (Link link : links) {
			if (link instanceof Affordance) {
				Affordance affordance = (Affordance) link;

				List<ActionDescriptor> actionDescriptors = affordance.getActionDescriptors();
				for (ActionDescriptor actionDescriptor : actionDescriptors) {
					// non-self GET non-GET and templated links are actions
					if (!("GET".equals(actionDescriptor.getHttpMethod())) || affordance.isTemplated()) {
						ret.add(link);
						// add just once for eligible link
						break;
					}
				}
			} else {
				if (!navigationalRels.contains(link.getRel()) && link.isTemplated()) {
					ret.add(link);
				}
			}
		}
		return ret;
	}

	private void createRecursiveSirenEntitiesFromPropertiesAndFields(SirenEntityContainer objectNode,
			Map<String, Object> propertiesNode, Object object) throws InvocationTargetException, IllegalAccessException {
		Map<String, PropertyDescriptor> propertyDescriptors = PropertyUtils.getPropertyDescriptors(object);
		for (PropertyDescriptor propertyDescriptor : propertyDescriptors.values()) {
			String name = propertyDescriptor.getName();
			if (FILTER_RESOURCE_SUPPORT.contains(name)) {
				continue;
			}

			Method readMethod = propertyDescriptor.getReadMethod();
			if (readMethod != null) {
				Object content = readMethod.invoke(object);
				String docUrl = documentationProvider.getDocumentationUrl(readMethod, content);
				traverseAttribute(objectNode, propertiesNode, name, docUrl, content);
			}
		}

		Field[] fields = object.getClass().getFields();
		for (Field field : fields) {
			String name = field.getName();
			if (!propertyDescriptors.containsKey(name)) {
				Object content = field.get(object);
				String docUrl = documentationProvider.getDocumentationUrl(field, content);
				traverseAttribute(objectNode, propertiesNode, name, docUrl, content);
			}
		}
	}

	private void traverseAttribute(SirenEntityContainer objectNode, Map<String, Object> propertiesNode, String name,
			String docUrl, Object content) throws InvocationTargetException, IllegalAccessException {
		Object value = getContentAsScalarValue(content);

		if (value != NULL_VALUE) {
			if (value != null) {
				// for each scalar property of a simple bean, add valuepair
				propertiesNode.put(name, value);
			} else {
				if (content instanceof Resources) {
					toSirenEntity(objectNode, content);
				} else if (content instanceof ResourceSupport) {
					traverseSingleSubEntity(objectNode, content, name, docUrl);
				} else if (content instanceof Collection) {
					Collection<?> collection = (Collection<?>) content;
					for (Object item : collection) {
						if (DataType.isSingleValueType(item.getClass())) {
							Object listObject = propertiesNode.get(name);
							if (listObject == null) {
								listObject = new ArrayList();
								propertiesNode.put(name, listObject);
							}
							if (listObject instanceof Collection) {
								((Collection) listObject).add(item);
							}
						} else if (item != null) {
							traverseSingleSubEntity(objectNode, item, name, docUrl);
						}
					}
				} else if (content instanceof Map) {
					Set<Map.Entry<String, Object>> entries = ((Map<String, Object>) content).entrySet();
					Map<String, Object> subProperties = new HashMap<String, Object>();
					propertiesNode.put(name, subProperties);
					for (Map.Entry<String, Object> entry : entries) {
						traverseAttribute(objectNode, subProperties, entry.getKey(), docUrl, entry.getValue());
					}
				} else {
					Map<String, Object> nestedProperties = new HashMap<String, Object>();
					propertiesNode.put(name, nestedProperties);
					createRecursiveSirenEntitiesFromPropertiesAndFields(objectNode, nestedProperties, content);
				}
			}
		}
	}

	private void traverseSingleSubEntity(SirenEntityContainer objectNode, Object content, String name, String docUrl)
			throws InvocationTargetException, IllegalAccessException {

		Object bean;
		List<Link> links;
		if (content instanceof Resource) {
			bean = ((Resource) content).getContent();
			links = ((Resource) content).getLinks();
		} else if (content instanceof ResourceSupport) {
			bean = content;
			links = ((ResourceSupport) content).getLinks();
		} else {
			bean = content;
			links = Collections.emptyList();
		}

		Map<String, Object> properties = new HashMap<String, Object>();
		List<String> rels = Collections.singletonList(docUrl != null ? docUrl : name);

		SirenEmbeddedRepresentation subEntity = new SirenEmbeddedRepresentation(getSirenClasses(bean), properties, null,
				toSirenActions(getActions(links)), toSirenLinks(getNavigationalLinks(links)), rels, null);
		// subEntity.setProperties(properties);
		objectNode.addSubEntity(subEntity);
		List<SirenEmbeddedLink> sirenEmbeddedLinks = toSirenEmbeddedLinks(getEmbeddedLinks(links));
		for (SirenEmbeddedLink sirenEmbeddedLink : sirenEmbeddedLinks) {
			subEntity.addSubEntity(sirenEmbeddedLink);
		}
		createRecursiveSirenEntitiesFromPropertiesAndFields(subEntity, properties, bean);
	}

	private List<SirenAction> toSirenActions(List<Link> links) {
		List<SirenAction> ret = new ArrayList<SirenAction>();
		for (Link link : links) {
			if (link instanceof Affordance) {
				Affordance affordance = (Affordance) link;
				List<ActionDescriptor> actionDescriptors = affordance.getActionDescriptors();
				for (ActionDescriptor actionDescriptor : actionDescriptors) {
					List<SirenField> fields = toSirenFields(actionDescriptor);
					// TODO integrate getActions and this method so we do not need this check:
					// only templated affordances or non-get affordances are actions
					if (!"GET".equals(actionDescriptor.getHttpMethod()) || affordance.isTemplated()) {
						String href;
						if (affordance.isTemplated()) {
							href = affordance.getUriTemplateComponents().getBaseUri();
						} else {
							href = affordance.getHref();
						}

						SirenAction sirenAction = new SirenAction(null, actionDescriptor.getActionName(), null,
								actionDescriptor.getHttpMethod(), href, requestMediaType, fields);
						ret.add(sirenAction);
					}
				}
			} else if (link.isTemplated()) {
				List<SirenField> fields = new ArrayList<SirenField>();
				List<TemplateVariable> variables = link.getVariables();
				boolean queryOnly = false;
				for (TemplateVariable variable : variables) {
					queryOnly = isQueryParam(variable);
					if (!queryOnly) {
						break;
					}
					fields.add(new SirenField(variable.getName(), "text", (String) null, variable.getDescription(), null));
				}
				// no support for non-query fields in siren
				if (queryOnly) {
					String baseUri = new UriTemplate(link.getHref()).expand().toASCIIString();
					SirenAction sirenAction = new SirenAction(null, null, null, "GET", baseUri, null, fields);
					ret.add(sirenAction);
				}
			}
		}
		return ret;
	}

	private boolean isQueryParam(TemplateVariable variable) {
		boolean queryOnly;
		switch (variable.getType()) {
			case REQUEST_PARAM:
			case REQUEST_PARAM_CONTINUED:
				queryOnly = true;
				break;
			default:
				queryOnly = false;
		}
		return queryOnly;
	}

	private List<SirenField> toSirenFields(ActionDescriptor actionDescriptor) {
		List<SirenField> ret = new ArrayList<SirenField>();
		actionDescriptor.accept(new SirenActionInputParameterVisitor(actionDescriptor, ret));
		return ret;
	}

	private class SirenActionInputParameterVisitor implements ActionInputParameterVisitor {

		private final List<SirenField> fields;
		private final ActionDescriptor actionDescriptor;

		public SirenActionInputParameterVisitor(ActionDescriptor actionDescriptor, List<SirenField> fields) {
			this.actionDescriptor = actionDescriptor;
			this.fields = fields;
		}

		@Override
		public void visit(ActionInputParameter inputParameter) {
			final Suggest<?>[] possibleValues = inputParameter.getPossibleValues(actionDescriptor);

			// dot-separated property path as field name
			SirenField sirenField = createSirenField(inputParameter.getName(), inputParameter.getValue(), inputParameter,
					possibleValues);

			fields.add(sirenField);
		}
	}

	private SirenField createSirenField(String paramName, Object propertyValue, ActionInputParameter inputParameter,
			Suggest<?>[] possibleValues) {
		SirenField sirenField;
		if (possibleValues.length == 0) {
			String propertyValueAsString = propertyValue == null ? null : propertyValue.toString();
			Type htmlInputFieldType = inputParameter.getHtmlInputFieldType();
			// TODO: null -> array or bean parameter without possible values
			String type = htmlInputFieldType == null ? "text" : htmlInputFieldType.name().toLowerCase();
			sirenField = new SirenField(paramName, type, propertyValueAsString, null, null);
		} else {
			List<SirenFieldValue> sirenPossibleValues = new ArrayList<SirenFieldValue>();
			String type;
			if (inputParameter.isArrayOrCollection()) {
				type = "checkbox";
				for (Suggest<?> possibleValue : possibleValues) {
					boolean selected = ObjectUtils.containsElement(inputParameter.getValues(), possibleValue.getUnwrappedValue());
					// TODO have more useful value title
					sirenPossibleValues
							.add(new SirenFieldValue(possibleValue.getText(), possibleValue.getUnwrappedValue(), selected));
				}
			} else {
				type = "radio";
				for (Suggest<?> possibleValue : possibleValues) {
					boolean selected = possibleValue.getUnwrappedValue().equals(propertyValue);
					sirenPossibleValues
							.add(new SirenFieldValue(possibleValue.getText(), possibleValue.getUnwrappedValue(), selected));
				}
			}
			sirenField = new SirenField(paramName, type, sirenPossibleValues, null, null);
		}
		return sirenField;
	}

	private List<SirenLink> toSirenLinks(List<Link> links) {
		List<SirenLink> ret = new ArrayList<SirenLink>();
		for (Link link : links) {
			if (link instanceof Affordance) {
				ret.add(new SirenLink(null, ((Affordance) link).getRels(), link.getHref(), null, null));
			} else {
				ret.add(new SirenLink(null, Collections.singletonList(link.getRel()), link.getHref(), null, null));
			}
		}
		return ret;
	}

	private List<SirenEmbeddedLink> toSirenEmbeddedLinks(List<Link> links) {
		List<SirenEmbeddedLink> ret = new ArrayList<SirenEmbeddedLink>();
		for (Link link : links) {
			if (link instanceof Affordance) {
				// TODO: how to determine classes? type of target resource? collection/item?
				ret.add(new SirenEmbeddedLink(null, ((Affordance) link).getRels(), link.getHref(), null, null));
			} else {
				ret.add(new SirenEmbeddedLink(null, Collections.singletonList(link.getRel()), link.getHref(), null, null));
			}
		}
		return ret;
	}

	static class NullValue {

	}

	public static final NullValue NULL_VALUE = new NullValue();

	private Object getContentAsScalarValue(Object content) {
		Object value = null;

		if (content == null) {
			value = NULL_VALUE;
		} else if (DataType.isSingleValueType(content.getClass())) {
			value = DataType.asScalarValue(content);
		}
		return value;
	}

	public void setRequestMediaType(String requestMediaType) {
		this.requestMediaType = requestMediaType;
	}

	public void setRelProvider(RelProvider relProvider) {
		this.relProvider = relProvider;
	}

	public void setDocumentationProvider(DocumentationProvider documentationProvider) {
		this.documentationProvider = documentationProvider;
	}

	public void setAdditionalNavigationalRels(Collection<String> additionalNavigationalRels) {
		navigationalRels.addAll(additionalNavigationalRels);
	}
}
