/*
 * Copyright (c) 2015. Escalon System-Entwicklung, Dietrich Schulten
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the License.
 */

package de.escalon.hypermedia.spring.uber;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.*;
import java.util.Map.Entry;

import de.escalon.hypermedia.affordance.*;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;

public class UberUtils {

	private UberUtils() {

	}

	static final Set<String> FILTER_RESOURCE_SUPPORT = new HashSet<String>(Arrays.asList("class", "links", "id"));
	static final Set<String> FILTER_BEAN = new HashSet<String>(Arrays.asList("class"));


	/**
	 * Recursively converts object to nodes of uber data.
	 *
	 * @param objectNode to append to
	 * @param object to convert
	 */
	public static void toUberData(AbstractUberNode objectNode, Object object) {
		Set<String> filtered = FILTER_RESOURCE_SUPPORT;
		if (object == null) {
			return;
		}
		try {
			// TODO: move all returns to else branch of property descriptor handling
			if (object instanceof Resource) {
				Resource<?> resource = (Resource<?>) object;
				objectNode.addLinks(resource.getLinks());
				toUberData(objectNode, resource.getContent());
				return;
			} else if (object instanceof Resources) {
				Resources<?> resources = (Resources<?>) object;

				// TODO set name using EVO see HypermediaSupportBeanDefinitionRegistrar

				objectNode.addLinks(resources.getLinks());

				Collection<?> content = resources.getContent();
				toUberData(objectNode, content);
				return;
			} else if (object instanceof ResourceSupport) {
				ResourceSupport resource = (ResourceSupport) object;

				objectNode.addLinks(resource.getLinks());

				// wrap object attributes below to avoid endless loop

			} else if (object instanceof Collection) {
				Collection<?> collection = (Collection<?>) object;
				for (Object item : collection) {
					// TODO name must be repeated for each collection item
					UberNode itemNode = new UberNode();
					objectNode.addData(itemNode);
					toUberData(itemNode, item);

//                    toUberData(objectNode, item);
				}
				return;
			}
			if (object instanceof Map) {
				Map<?, ?> map = (Map<?, ?>) object;
				for (Entry<?, ?> entry : map.entrySet()) {
					String key = entry.getKey()
							.toString();
					Object content = entry.getValue();
					Object value = getContentAsScalarValue(content);
					UberNode entryNode = new UberNode();
					objectNode.addData(entryNode);
					entryNode.setName(key);
					if (value != null) {
						entryNode.setValue(value);
					} else {
						toUberData(entryNode, content);
					}
				}
			} else {
				PropertyDescriptor[] propertyDescriptors = getPropertyDescriptors(object);// BeanUtils
				// .getPropertyDescriptors(bean.getClass());
				for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
					String name = propertyDescriptor.getName();
					if (filtered.contains(name)) {
						continue;
					}
					UberNode propertyNode = new UberNode();
					Object content = propertyDescriptor.getReadMethod()
							.invoke(object);

					Object value = getContentAsScalarValue(content);
					propertyNode.setName(name);
					objectNode.addData(propertyNode);
					if (value != null) {
						// for each scalar property of a simple bean, add valuepair nodes to data
						propertyNode.setValue(value);
					} else {
						toUberData(propertyNode, content);
					}
				}
			}
		} catch (Exception ex) {
			throw new RuntimeException("failed to transform object " + object, ex);
		}
	}

	private static PropertyDescriptor[] getPropertyDescriptors(Object bean) {
		try {
			return Introspector.getBeanInfo(bean.getClass())
					.getPropertyDescriptors();
		} catch (IntrospectionException e) {
			throw new RuntimeException("failed to get property descriptors of bean " + bean, e);
		}
	}

	private static Object getContentAsScalarValue(Object content) {
		final Object value;
		if (content == null) {
			value = UberNode.NULL_VALUE;
		} else if (DataType.isSingleValueType(content.getClass())) {
			value = content.toString();
		} else {
			value = null;
		}
		return value;
	}


	/**
	 * Converts single link to uber node.
	 *
	 * @param href to use
	 * @param actionDescriptor to use for action and model, never null
	 * @param rels of the link
	 * @return uber link
	 */
	public static UberNode toUberLink(String href, ActionDescriptor actionDescriptor, String... rels) {
		return toUberLink(href, actionDescriptor, Arrays.asList(rels));
	}

	/**
	 * Converts single link to uber node.
	 *
	 * @param href to use
	 * @param actionDescriptor to use for action and model, never null
	 * @param rels of the link
	 * @return uber link
	 */
	public static UberNode toUberLink(String href, ActionDescriptor actionDescriptor, List<String> rels) {
		Assert.notNull(actionDescriptor, "actionDescriptor must not be null");
		UberNode uberLink = new UberNode();
		uberLink.setRel(rels);
		PartialUriTemplateComponents partialUriTemplateComponents = new PartialUriTemplate(href).expand(Collections
				.<String, Object>emptyMap());
		uberLink.setUrl(partialUriTemplateComponents.getBaseUri());
		uberLink.setModel(getModelProperty(href, actionDescriptor));
		if (actionDescriptor != null) {
			RequestMethod requestMethod = RequestMethod.valueOf(actionDescriptor.getHttpMethod());
			uberLink.setAction(UberAction.forRequestMethod(requestMethod));
		}
		return uberLink;
	}

	private static String getModelProperty(String href, ActionDescriptor actionDescriptor) {

		PartialUriTemplate uriTemplate = new PartialUriTemplate(href);
		RequestMethod httpMethod = RequestMethod.valueOf(actionDescriptor.getHttpMethod());
		final String model;
		switch (httpMethod) {
			case GET:
			case DELETE: {
				model = buildModel(uriTemplate.getVariableNames(), "{?", ",", "}", "%s");
				break;
			}
			case POST:
			case PUT:
			case PATCH: {
				model = buildModel(uriTemplate.getVariableNames(), "", "&", "", "%s={%s}");
				break;
			}
			default:
				model = null;
		}
		return StringUtils.isEmpty(model) ? null : model;
	}

	public static List<ActionDescriptor> getActionDescriptors(Link link) {
		List<ActionDescriptor> actionDescriptors;
		if (link instanceof Affordance) {
			actionDescriptors = ((Affordance) link).getActionDescriptors();
		} else {
			actionDescriptors = Arrays.asList((ActionDescriptor) new ActionDescriptorImpl("get", RequestMethod.GET.name()));
		}
		return actionDescriptors;
	}

	public static List<String> getRels(Link link) {
		List<String> rels;
		if (link instanceof Affordance) {
			rels = ((Affordance) link).getRels();
		} else {
			rels = Arrays.asList(link.getRel());
		}
		return rels;
	}

	private static String getUrlProperty(Link link) {
		PartialUriTemplateComponents partialUriTemplateComponents = new PartialUriTemplate(link.getHref()).expand
				(Collections.<String, Object>emptyMap());
		return partialUriTemplateComponents.getBaseUri();
//		return UriComponentsBuilder.fromUriString(baseUri).build().normalize().toString();
	}

	private static String buildModel(List<String> variableNames, String prefix, String separator, String suffix,
									 String parameterTemplate) {
		StringBuilder sb = new StringBuilder();
		for (String variable : variableNames) {
			if (sb.length() == 0) {
				sb.append(prefix);
			} else {
				sb.append(separator);
			}
			sb.append(String.format(parameterTemplate, variable, variable));
		}
		if (sb.length() > 0) {
			sb.append(suffix);
		}
		return sb.toString();
	}
}
