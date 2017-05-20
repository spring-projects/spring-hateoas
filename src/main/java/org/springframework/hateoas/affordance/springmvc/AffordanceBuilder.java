/*
 * Copyright 2014-2017 the original author or authors.
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

package org.springframework.hateoas.affordance.springmvc;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.UriTemplateComponents;
import org.springframework.hateoas.affordance.ActionDescriptor;
import org.springframework.hateoas.affordance.Affordance;
import org.springframework.hateoas.affordance.TypedResource;
import org.springframework.hateoas.core.DummyInvocationUtils;
import org.springframework.hateoas.mvc.UriComponentsSupport;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Builder for hypermedia affordances, usable as RFC-5988 web links and optionally holding information about request
 * body requirements.
 * 
 * @author Dietrich Schulten
 * @author Oliver Gierke
 */
public class AffordanceBuilder implements LinkBuilder {

	private static final AffordanceBuilderFactory FACTORY = new AffordanceBuilderFactory();

	private final UriTemplateComponents partialUriTemplateComponents;
	private final List<ActionDescriptor> actionDescriptors = new ArrayList<ActionDescriptor>();
	private final MultiValueMap<String, String> linkParams = new LinkedMultiValueMap<String, String>();
	private final List<String> rels = new ArrayList<String>();
	private final List<String> reverseRels = new ArrayList<String>();

	private TypedResource collectionHolder;

	/**
	 * Creates a new {@link AffordanceBuilder} with a base of the mapping annotated to the given controller class.
	 *
	 * @param controller the class to discover the annotation on, must not be {@literal null}.
	 * @return builder
	 */
	public static AffordanceBuilder linkTo(Class<?> controller) {
		return FACTORY.linkTo(controller, new Object[0]);
	}

	/**
	 * Creates a new {@link AffordanceBuilder} with a base of the mapping annotated to the given controller class. The
	 * additional parameters are used to fill up potentially available path variables in the class scope request mapping.
	 *
	 * @param controller the class to discover the annotation on, must not be {@literal null}.
	 * @param parameters additional parameters to bind to the URI template declared in the annotation, must not be
	 *          {@literal null}.
	 * @return builder
	 */
	public static AffordanceBuilder linkTo(Class<?> controller, Object... parameters) {
		return FACTORY.linkTo(controller, parameters);
	}

	/**
	 * @see org.springframework.hateoas.MethodLinkBuilderFactory#linkTo(Method, Object...)
	 */
	public static AffordanceBuilder linkTo(Method method, Object... parameters) {
		return FACTORY.linkTo(method.getDeclaringClass(), method, parameters);
	}

	/**
	 * Creates a new {@link AffordanceBuilder} with a base of the mapping annotated to the given controller class. The
	 * additional parameters are used to fill up potentially available path variables in the class scop request mapping.
	 *
	 * @param controller the class to discover the annotation on, must not be {@literal null}.
	 * @param parameters additional parameters to bind to the URI template declared in the annotation, must not be
	 *          {@literal null}.
	 * @return builder
	 */
	public static AffordanceBuilder linkTo(Class<?> controller, Map<String, ?> parameters) {
		return FACTORY.linkTo(controller, parameters);
	}

	/**
	 * @see org.springframework.hateoas.MethodLinkBuilderFactory#linkTo(Class, Method, Object...)
	 */
	public static AffordanceBuilder linkTo(Class<?> controller, Method method, Object... parameters) {
		return FACTORY.linkTo(controller, method, parameters);
	}

	/**
	 * Creates an {@link AffordanceBuilder} pointing to a controller method. Hand in a dummy method invocation result you
	 * can create via {@link #methodOn(Class, Object...)} or {@link DummyInvocationUtils#methodOn(Class, Object...)}.
	 * 
	 * <pre>
	 * &#64;RequestMapping("/customers")
	 * class CustomerController {
	 *   &#64;RequestMapping("/{id}/addresses")
	 *   HttpEntity&lt;Addresses&gt; showAddresses(@PathVariable Long id) { � }
	 * }
	 * Link link = linkTo(methodOn(CustomerController.class).showAddresses(2L)).withRel("addresses");
	 * </pre>
	 * 
	 * The resulting {@link Link} instance will point to {@code /customers/2/addresses} and have a rel of
	 * {@code addresses}. For more details on the method invocation constraints, see
	 * {@link DummyInvocationUtils#methodOn(Class, Object...)}.
	 *
	 * @param methodInvocation to use for link building
	 * @return builder
	 */
	public static AffordanceBuilder linkTo(Object methodInvocation) {
		return FACTORY.linkTo(methodInvocation);
	}

	/**
	 * Creates a new {@link AffordanceBuilder} pointing to this server, but without ActionDescriptor.
	 */
	AffordanceBuilder() {
		this(new UriTemplate(UriComponentsSupport.getBuilder().build().toString()).expand(Collections.<String, Object>emptyMap()),
				Collections.<ActionDescriptor>emptyList());
	}

	/**
	 * Creates a new {@link AffordanceBuilder} using the given {@link ActionDescriptor}.
	 *
	 * @param partialUriTemplate must not be {@literal null}
	 * @param actionDescriptors must not be {@literal null}
	 */
	AffordanceBuilder(UriTemplate partialUriTemplate, List<ActionDescriptor> actionDescriptors) {

		Assert.notNull(partialUriTemplate, "partialUritemplate must not be null!");
		Assert.notNull(actionDescriptors, "actionDescriptors must not be null!");

		this.partialUriTemplateComponents = partialUriTemplate.asComponents();

		for (ActionDescriptor actionDescriptor : actionDescriptors) {
			this.actionDescriptors.add(actionDescriptor);
		}
	}

	public static <T> T methodOn(Class<T> clazz, Object... parameters) {
		return DummyInvocationUtils.methodOn(clazz, parameters);
	}

	/**
	 * Builds affordance with one or multiple rels which must have been defined previously using {@link #rel(String)} or
	 * {@link #reverseRel(String, String)}.
	 * <p>
	 * The motivation for multiple rels is this statement in the web linking rfc-5988: &quot;Note that link-values can
	 * convey multiple links between the same target and context IRIs; for example:
	 * </p>
	 * 
	 * <pre>
	 * Link: &lt;http://example.org/&gt;
	 *       rel="start http://example.net/relation/other"
	 * </pre>
	 * 
	 * Here, the link to 'http://example.org/' has the registered relation type 'start' and the extension relation type
	 * 'http://example.net/relation/other'.&quot;
	 *
	 * @return affordance
	 * @see <a href="https://tools.ietf.org/html/rfc5988#section-5.5">Web Linking Examples</a>
	 */
	public Affordance build() {

		Assert.state(!(rels.isEmpty() && reverseRels.isEmpty()),
				"no rels or reverse rels found, call rel() or rev() before building the affordance");

		Affordance affordance = new Affordance(new UriTemplate(toString()), actionDescriptors, collectionHolder,
				rels.toArray(new String[rels.size()]));

		for (Entry<String, List<String>> linkParamEntry : linkParams.entrySet()) {

			List<String> values = linkParamEntry.getValue();

			for (String value : values) {
				affordance.addLinkParam(linkParamEntry.getKey(), value);
			}
		}

		for (String reverseRel : reverseRels) {
			affordance.addRev(reverseRel);
		}

		return affordance;
	}

	/**
	 * Allows to define one or more reverse link relations (a "rev" in terms of rfc-5988), where the resource that has the
	 * affordance will be considered the object in a subject-predicate-object statement.
	 * <p>
	 * E.g. if you had a rel <code>ex:parent</code> which connects a child to its father, you could also use ex:parent on
	 * the father to point to the child by reverting the direction of ex:parent. This is mainly useful when you have no
	 * other way to express in your context that the direction of a relationship is inverted.
	 * </p>
	 *
	 * @param rev to be used as reverse relationship
	 * @param revertedRel to be used in contexts which have no notion of reverse relationships. E.g. for a reverse rel
	 *          <code>ex:parent</code> you can use a made-up rel name <code>ex:child</code> which will be used as rel when
	 *          rendering HAL.
	 * @return builder
	 */
	public AffordanceBuilder reverseRel(String rev, String revertedRel) {

		rels.add(revertedRel);
		reverseRels.add(rev);
		return this;
	}

	/**
	 * Allows to define one or more reverse link relations (a "rev" in terms of rfc-5988) to collections in cases where
	 * the resource that has the affordance is not the object in a subject-predicate-object statement about each
	 * collection item. See {@link #rel(TypedResource, String)} for explanation.
	 *
	 * @param rev to be used as reverse relationship
	 * @param revertedRel to be used in contexts which have no notion of reverse relationships, e.g. HAL
	 * @param object describing the object
	 * @return builder
	 */
	public AffordanceBuilder reverseRel(String rev, String revertedRel, TypedResource object) {

		collectionHolder = object;
		rels.add(0, revertedRel);
		reverseRels.add(rev);
		return this;
	}

	/**
	 * Allows to define one or more link relations for the affordance.
	 *
	 * @param rel to be used as link relation
	 * @return builder
	 */
	public AffordanceBuilder rel(String rel) {

		rels.add(rel);
		return this;
	}

	/**
	 * Allows to define one or more link relations for affordances that point to collections in cases where the resource
	 * that has the affordance is not the subject in a subject-predicate-object statement about each collection item. E.g.
	 * a product might have a loose relationship to ordered items where it can be POSTed, but the ordered items do not
	 * belong to the product, but to an order. You can express that by saying:
	 * 
	 * <pre>
	 * TypedResource order = new TypedResource("http://schema.org/Order"); // holds the ordered items
	 * Resource&lt;Product&gt; product = new Resource&lt;&gt;(); // has a loose relationship to ordered items
	 * product.add(linkTo(methodOn(OrderController.class).postOrderedItem()
	 *    .rel(order, "orderedItem")); // order has ordered items, not product has ordered items
	 * </pre>
	 * 
	 * If the order doesn't exist yet, it cannot be identified. In that case use a TypedResource without identifying URI.
	 *
	 * @param rel to be used as link relation
	 * @param subject describing the subject
	 * @return builder
	 */
	public AffordanceBuilder rel(TypedResource subject, String rel) {

		collectionHolder = subject;
		rels.add(rel);
		return this;
	}

	public AffordanceBuilder withTitle(String title) {

		linkParams.set("title", title);
		return this;
	}

	public AffordanceBuilder withTitleStar(String titleStar) {

		linkParams.set("title*", titleStar);
		return this;
	}

	/**
	 * Allows to define link header params (not UriTemplate variables).
	 *
	 * @param name of the link header param
	 * @param value of the link header param
	 * @return builder
	 */
	public AffordanceBuilder withLinkParam(String name, String value) {

		linkParams.add(name, value);
		return this;
	}

	public AffordanceBuilder withAnchor(String anchor) {

		this.linkParams.set("anchor", anchor);
		return this;
	}

	public AffordanceBuilder withHreflang(String hreflang) {

		this.linkParams.add("hreflang", hreflang);
		return this;
	}

	public AffordanceBuilder withMedia(String media) {

		this.linkParams.set("media", media);
		return this;
	}

	public AffordanceBuilder withType(String type) {

		this.linkParams.set("type", type);
		return this;
	}

	@Override
	public AffordanceBuilder slash(Object object) {

		if (object == null) {
			return this;
		}

		if (object instanceof Identifiable) {
			return slash((Identifiable<?>) object);
		}

		String urlPart = object.toString();

		// make sure one cannot delete the fragment
		if (urlPart.endsWith("#")) {
			urlPart = urlPart.substring(0, urlPart.length() - 1);
		}

		if (!StringUtils.hasText(urlPart)) {
			return this;
		}

		UriTemplateComponents urlPartComponents = new UriTemplate(urlPart).expand().asComponents();
		UriTemplateComponents affordanceComponents = this.partialUriTemplateComponents;

		String path = !affordanceComponents.getBaseUri().endsWith("/") && !urlPartComponents.getBaseUri().startsWith("/")
				? affordanceComponents.getBaseUri() + "/" + urlPartComponents.getBaseUri()
				: affordanceComponents.getBaseUri() + urlPartComponents.getBaseUri();
		String queryHead = affordanceComponents.getQueryHead() + (StringUtils.hasText(urlPartComponents.getQueryHead())
				? "&" + urlPartComponents.getQueryHead().substring(1) : "");
		String queryTail = affordanceComponents.getQueryTail()
				+ (StringUtils.hasText(urlPartComponents.getQueryTail()) ? "," + urlPartComponents.getQueryTail() : "");
		String fragmentIdentifier = StringUtils.hasText(urlPartComponents.getFragmentIdentifier())
				? urlPartComponents.getFragmentIdentifier() : affordanceComponents.getFragmentIdentifier();

		List<String> variableNames = new ArrayList<String>();
		variableNames.addAll(affordanceComponents.getVariableNames());
		variableNames.addAll(urlPartComponents.getVariableNames());

		UriTemplateComponents mergedUriComponents = new UriTemplateComponents(path, queryHead, queryTail,
				fragmentIdentifier, variableNames);

		return new AffordanceBuilder(new UriTemplate(mergedUriComponents.toString()), this.actionDescriptors);
	}

	@Override
	public AffordanceBuilder slash(Identifiable<?> identifiable) {

		if (identifiable == null) {
			return this;
		}

		return slash(identifiable.getId());
	}

	@Override
	public URI toUri() {

		UriTemplate partialUriTemplate = new UriTemplate(partialUriTemplateComponents.toString());

		String actionLink = partialUriTemplate.stripOptionalVariables(this.actionDescriptors).toString();

		if (actionLink == null || actionLink.contains("{")) {
			throw new IllegalStateException("cannot convert template to URI");
		}
		return UriComponentsBuilder.fromUriString(actionLink).build().toUri();
	}

	@Override
	public Affordance withRel(String rel) {
		return rel(rel).build();
	}

	@Override
	public Affordance withSelfRel() {
		return rel(Link.REL_SELF).build();
	}

	@Override
	public String toString() {
		return this.partialUriTemplateComponents.toString();
	}

	/**
	 * Adds actionDescriptors and linkParams of the given AffordanceBuilder to this affordanceBuilder.
	 *
	 * @param affordanceBuilder whose action descriptors should be added to this one
	 * @return builder
	 */
	public AffordanceBuilder and(AffordanceBuilder affordanceBuilder) {

		this.actionDescriptors.addAll(affordanceBuilder.getActionDescriptors());
		this.linkParams.putAll(affordanceBuilder.linkParams);
		return this;
	}

	public List<ActionDescriptor> getActionDescriptors() {
		return this.actionDescriptors;
	}

}
