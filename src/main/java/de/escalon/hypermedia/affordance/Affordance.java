/*
 * Copyright (c) 2014. Escalon System-Entwicklung, Dietrich Schulten
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

package de.escalon.hypermedia.affordance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.escalon.hypermedia.action.Cardinality;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.TemplateVariable;
import org.springframework.hateoas.UriTemplate;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * Represents an http affordance for purposes of a ReST service as described by <a
 * href="http://tools.ietf.org/html/rfc5988">Web Linking rfc-5988</a>. Additionally includes {@link ActionDescriptor}s
 * for http methods and expected request bodies. <p>Also supports templated affordances, in which case it is represented
 * as a <a href="http://tools.ietf.org/html/draft-nottingham-link-template-01">Link-Template Header</a></p> <p>This
 * class can be created manually or via one of the {@link de.escalon.hypermedia.spring.AffordanceBuilder#linkTo}
 * methods. In the latter case the affordance should be created with pre-expanded variables (using {@link
 * PartialUriTemplate#expand} on the given uri template). In the former case one may use {@link #expandPartially} to
 * expand the Affordance variables as far as possible, while keeping unsatisified variables.</p>
 * <p>Created by dschulten on 07.09.2014.</p>
 */
public class Affordance extends Link {

	private boolean selfRel = false;
	private List<ActionDescriptor> actionDescriptors = new ArrayList<ActionDescriptor>();
	private MultiValueMap<String, String> linkParams = new LinkedMultiValueMap<String, String>();
	private PartialUriTemplate partialUriTemplate;
	private Cardinality cardinality = Cardinality.SINGLE;
	private TypedResource collectionHolder;

	/**
	 * Creates affordance. Action descriptors and link header params may be added later.
	 *
	 * @param uriTemplate uri or uritemplate of the affordance
	 * @param rels describing the link relation type
	 */
	public Affordance(String uriTemplate, String... rels) {
		this(new PartialUriTemplate(uriTemplate), new ArrayList<ActionDescriptor>(), rels);
	}

	/**
	 * Creates affordance. Rels, action descriptors and link header params may be added later.
	 *
	 * @param uriTemplate uri or uritemplate of the affordance
	 */
	public Affordance(String uriTemplate) {
		this(uriTemplate, new String[]{});
	}

	/**
	 * Creates affordance, usually for a pre-expanded uriTemplate. Link header params may be added later. Optional
	 * variables will be stripped before passing it to the underlying link. Use {@link #getUriTemplateComponents()} to
	 * access the base uri, query head, query tail with optional variables etc.
	 *
	 * @param uriTemplate pre-expanded uri or uritemplate of the affordance
	 * @param actionDescriptors describing the possible http methods on the affordance
	 * @param rels describing the link relation type
	 * @see PartialUriTemplate#expand
	 */
	public Affordance(PartialUriTemplate uriTemplate, List<ActionDescriptor> actionDescriptors, String... rels) {
		// Since AffordanceBuilder creates variables for undefined arguments,
		// we would get a link-template where ControllerLinkBuilder only sees a link.
		// For compatibility we strip variables deemed to be not required by the actionDescriptors before passing on
		// the template to the underlying Link. That way the href of an Affordance stays compatible with a Link that
		// has been created with ControllerLinkBuilder. Only serializers that make use of Affordance will see the
		// optional variables, too.
		// They can access the base uri, query etc. via getUriTemplateComponents.
		super(uriTemplate.stripOptionalVariables(actionDescriptors)
				.toString());
		this.partialUriTemplate = uriTemplate;

		Assert.noNullElements(rels, "null rels are not allowed");

		for (String rel : rels) {
			addRel(rel);
			if ("self".equals(rel)) {
				selfRel = true;
			}
		}
		// if any action refers to a collection resource, make the affordance a collection affordance
		for (ActionDescriptor actionDescriptor : actionDescriptors) {
			if (Cardinality.COLLECTION == actionDescriptor.getCardinality()) {
				this.cardinality = Cardinality.COLLECTION;
				break;
			}
		}
		this.actionDescriptors.addAll(actionDescriptors);
	}


	private Affordance(String uriTemplate, MultiValueMap<String, String> linkParams,
					   List<ActionDescriptor> actionDescriptors) {
		this(new PartialUriTemplate(uriTemplate), actionDescriptors); // no rels to pass
		this.linkParams = linkParams; // takes care of rels
	}


	/**
	 * The relation type of the link.
	 *
	 * @param rel IANA-registered type or extension relation type.
	 */
	public void addRel(String rel) {
		Assert.hasLength(rel);
		linkParams.add("rel", rel);
	}

	/**
	 * The "type" parameter, when present, is a hint indicating what the media type of the result of dereferencing the
	 * link should be.  Note that this is only a hint; for example, it does not override the Content-Type header of a
	 * HTTP response obtained by actually following the link.  There MUST NOT be more than one type parameter in a link-
	 * value.
	 *
	 * @param mediaType to set
	 */
	public void setType(String mediaType) {
		if (mediaType != null)
			linkParams.set("type", mediaType);
		else
			linkParams.remove("type");
	}

	/**
	 * The "hreflang" parameter, when present, is a hint indicating what the language of the result of dereferencing the
	 * link should be.  Note that this is only a hint; for example, it does not override the Content-Language header of
	 * a HTTP response obtained by actually following the link.  Multiple "hreflang" parameters on a single link- value
	 * indicate that multiple languages are available from the indicated resource.
	 *
	 * @param hreflang to add
	 */
	public void addHreflang(String hreflang) {
		Assert.hasLength(hreflang);
		linkParams.add("hreflang", hreflang);
	}

	/**
	 * The "title" parameter, when present, is used to label the destination of a link such that it can be used as a
	 * human-readable identifier (e.g., a menu entry) in the language indicated by the Content- Language header (if
	 * present).  The "title" parameter MUST NOT appear more than once in a given link-value; occurrences after the
	 * first MUST be ignored by parsers.
	 *
	 * @param title to set
	 */
	public void setTitle(String title) {
		if (title != null)
			linkParams.set("title", title);
		else {
			linkParams.remove("title");
		}
	}

	@JsonIgnore
	public boolean isBaseUriTemplated() {
		return partialUriTemplate.asComponents()
				.isBaseUriTemplated();
	}

	/**
	 * Gets the 'title' link parameter
	 *
	 * @return title of link
	 */
	public String getTitle() {
		return linkParams.getFirst("title");
	}

	/**
	 * The "title*" parameter can be used to encode this label in a different character set, and/or contain language
	 * information as per [RFC5987].  The "title*" parameter MUST NOT appear more than once in a given link-value;
	 * occurrences after the first MUST be ignored by parsers.  If the parameter does not contain language information,
	 * its language is indicated by the Content-Language header (when present).
	 *
	 * @param titleStar to set
	 */
	public void setTitleStar(String titleStar) {
		if (titleStar != null)
			linkParams.set("title*", titleStar);
		else
			linkParams.remove("title*");
	}

	/**
	 * The "media" parameter, when present, is used to indicate intended destination medium or media for style
	 * information (see [W3C.REC-html401-19991224], Section 6.13).  Note that this may be updated by
	 * [W3C.CR-css3-mediaqueries-20090915]).  Its value MUST be quoted if it contains a semicolon (";") or comma (","),
	 * and there MUST NOT be more than one "media" parameter in a link-value.
	 *
	 * @param mediaDesc to set
	 */
	public void setMedia(String mediaDesc) {
		if (mediaDesc != null)
			linkParams.set("media", mediaDesc);
		else
			linkParams.remove("media");
	}

	/**
	 * The "rev" parameter has been used in the past to indicate that the semantics of the relationship are in the
	 * reverse direction.  That is, a link from A to B with REL="X" expresses the same relationship as a link from B to
	 * A with REV="X". "rev" is deprecated by this specification because it often confuses authors and readers; in most
	 * cases, using a separate relation type is preferable.
	 *
	 * @param rev to add
	 */
	public void addRev(String rev) {
		Assert.hasLength(rev);
		linkParams.add("rev", rev);
	}

	/**
	 * By default, the context of a link conveyed in the Link header field is the IRI of the requested resource. When
	 * present, the anchor parameter overrides this with another URI, such as a fragment of this resource, or a third
	 * resource (i.e., when the anchor value is an absolute URI).  If the anchor parameter's value is a relative URI,
	 * parsers MUST resolve it as per [RFC3986], Section 5.  Note that any base URI from the body's content is not
	 * applied.
	 *
	 * @param anchor base uri to define
	 */
	public void setAnchor(String anchor) {
		if (anchor != null)
			linkParams.set("anchor", anchor);
		else
			linkParams.remove("anchor");
	}

	/**
	 * Adds link-extension params, i.e. custom params which are not described in the web linking rfc.
	 *
	 * @param paramName of link-extension
	 * @param values one or more values to add
	 */
	public void addLinkParam(String paramName, String... values) {
		Assert.notEmpty(values);
		for (String value : values) {
			Assert.hasLength(value);
			linkParams.add(paramName, value);
		}
	}

	/**
	 * Gets header name of the affordance, either Link or Link-Template depending on the presence of template
	 * variables.
	 *
	 * @return header name
	 * @see <a href="http://tools.ietf.org/html/rfc5988">Web Linking rfc-5988</a>
	 * @see <a href="http://tools.ietf.org/html/draft-nottingham-link-template-01">Link-Template Header</a>
	 */
	@JsonIgnore
	public String getHeaderName() {
		String headerName;
		if (super.isTemplated()) {
			headerName = "Link-Template";
		} else {
			headerName = "Link";
		}
		return headerName;
	}

	/**
	 * Affordance represented as http link header value.
	 *
	 * @return link header value
	 */
	public String asHeader() {
		StringBuilder result = new StringBuilder();
		for (Map.Entry<String, List<String>> linkParamEntry : linkParams.entrySet()) {
			if (result.length() != 0) {
				result.append("; ");
			}
			String linkParamEntryKey = linkParamEntry.getKey();
			if ("rel".equals(linkParamEntryKey) || "rev".equals(linkParamEntryKey)) {
				result.append(linkParamEntryKey)
						.append("=");
				result.append("\"")
						.append(StringUtils.collectionToDelimitedString(linkParamEntry.getValue(), " "))
						.append("\"");
			} else {
				StringBuilder linkParams = new StringBuilder();
				for (String value : linkParamEntry.getValue()) {
					if (linkParams.length() != 0) {
						linkParams.append("; ");
					}
					linkParams.append(linkParamEntryKey)
							.append("=");
					linkParams.append("\"")
							.append(value)
							.append("\"");
				}
				result.append(linkParams);
			}
		}

		String linkHeader = "<" + partialUriTemplate.asComponents()
				.toString() + ">; ";

		return result.insert(0, linkHeader)
				.toString();
	}

	/**
	 * Returns template variables contained in the underlying Link with basic distinction of required and optional
	 * variables based on the variable type. If actionDescriptors are present, they should be preferred over variables
	 * because they consider the handler methods to determine if a variable is required or optional.
	 *
	 * @return variables
	 */
	@Override
	public List<TemplateVariable> getVariables() {
		return super.getVariables();
	}

	@Override
	public String toString() {
		return getHeaderName() + ": " + asHeader();
	}

	@Override
	public Affordance withRel(String rel) {
		linkParams.set("rel", rel);
		return new Affordance(this.getHref(), linkParams, actionDescriptors);
	}

	@Override
	public Affordance withSelfRel() {
		if (!linkParams.get("rel")
				.contains(Link.REL_SELF)) {
			linkParams.add("rel", Link.REL_SELF);
		}
		return new Affordance(this.getHref(), linkParams, actionDescriptors);
	}

	/**
	 * Expands template variables, arguments must satisfy all required template variables,
	 * optional variables will be removed.
	 *
	 * @param arguments to expansion in the order they appear in the template
	 * @return expanded affordance
	 */
	@Override
	public Affordance expand(Object... arguments) {
		UriTemplate template = new UriTemplate(partialUriTemplate.asComponents()
				.toString());
		String expanded = template.expand(arguments)
				.toASCIIString();
		return new Affordance(expanded, linkParams, actionDescriptors);
	}

	/**
	 * Gets parts of the uri template such as base uri, expanded query part, unexpanded query part etc.
	 *
	 * @return template component parts
	 */
	@JsonIgnore
	public PartialUriTemplateComponents getUriTemplateComponents() {
		return partialUriTemplate.asComponents();
	}

	/**
	 * Expands template variables, arguments must satisfy all required template variables,
	 * unsatisfied optional arguments will be removed.
	 *
	 * @param arguments to expansion
	 * @return expanded affordance
	 */
	@Override
	public Affordance expand(Map<String, ? extends Object> arguments) {
		UriTemplate template = new UriTemplate(partialUriTemplate.asComponents()
				.toString());
		String expanded = template.expand(arguments)
				.toASCIIString();
		return new Affordance(expanded, linkParams, actionDescriptors);
	}

	/**
	 * Expands template variables as far as possible, unsatisfied variables will remain variables. This is primarily for
	 * manually created affordances. If the Affordance has been created with linkTo-methodOn, it should not be necessary
	 * to expand the affordance again.
	 *
	 * @param arguments for expansion, in the order they appear in the template
	 * @return partially expanded affordance
	 */
	public Affordance expandPartially(Object... arguments) {
		return new Affordance(partialUriTemplate.expand(arguments)
				.toString(), linkParams, actionDescriptors);
	}

	/**
	 * Expands template variables as far as possible, unsatisfied variables will remain variables. This is primarily for
	 * manually created affordances. If the Affordance has been created with linkTo-methodOn, it should not be necessary
	 * to expand the affordance again.
	 *
	 * @param arguments for expansion
	 * @return partially expanded affordance
	 */
	public Affordance expandPartially(Map<String, ? extends Object> arguments) {
		return new Affordance(partialUriTemplate.expand((Map<String, Object>) arguments)
				.toString(), linkParams, actionDescriptors);
	}


	/**
	 * Allows to retrieve all rels defined for this affordance.
	 *
	 * @return rels
	 */
	@JsonIgnore
	public List<String> getRels() {
		final List<String> rels = linkParams.get("rel");
		return rels == null ? Collections.<String>emptyList() : Collections.unmodifiableList(rels);
	}

	/**
	 * Gets the rel.
	 *
	 * @return first defined rel or null
	 */
	@Override
	public String getRel() {
		return linkParams.getFirst("rel");
	}

	/**
	 * Retrieves all revs for this affordance.
	 *
	 * @return revs
	 */
	@JsonIgnore
	public List<String> getRevs() {
		final List<String> revs = linkParams.get("rev");
		return revs == null ? Collections.<String>emptyList() : Collections.unmodifiableList(revs);
	}

	/**
	 * Gets the rev.
	 *
	 * @return first defined rev or null
	 */
	@JsonIgnore
	public String getRev() {
		return linkParams.getFirst("rev");
	}


	/**
	 * Sets action descriptors.
	 *
	 * @param actionDescriptors to set
	 */
	public void setActionDescriptors(List<ActionDescriptor> actionDescriptors) {
		if (this.actionDescriptors.isEmpty()) {
			this.actionDescriptors = actionDescriptors;
		} else {
			throw new IllegalStateException("cannot redefine existing action descriptors");
		}
	}

	/**
	 * Gets action descriptors.
	 *
	 * @return descriptors, never null
	 */
	@JsonIgnore
	public List<ActionDescriptor> getActionDescriptors() {
		return Collections.unmodifiableList(actionDescriptors);
	}

	/**
	 * Determines if the affordance points to a single or a collection resource.
	 *
	 * @return single or collection cardinality, never null
	 */
	@JsonIgnore
	public Cardinality getCardinality() {
		return cardinality;
	}

	/**
	 * Determines if the affordance is a self rel.
	 *
	 * @return true if the affordance is a self rel
	 */
	@JsonIgnore
	public boolean isSelfRel() {
		return selfRel;
	}


	/**
	 * Determines if the affordance has unsatisfied required variables. This allows to decide if the affordance can also
	 * be treated as a plain Link without template variables if the caller omits all optional variables. Serializers can
	 * use this to render it as a resource with optional search features.
	 *
	 * @return true if the affordance has unsatisfied required variables
	 */
	@JsonIgnore
	public boolean hasUnsatisfiedRequiredVariables() {

		for (ActionDescriptor actionDescriptor : actionDescriptors) {
			Map<String, ActionInputParameter> requiredParameters =
					actionDescriptor.getRequiredParameters();
			for (ActionInputParameter annotatedParameter : requiredParameters.values()) {
				if (!annotatedParameter.hasValue()) {
					return true;
				}
			}
		}
		return false;
	}


	/**
	 * Gets collection holder. If an affordance points to a collection, there are cases where the resource that has the
	 * affordance is not semantically <em>holding</em> the collection items, but just has a loose relationship to the
	 * collection. E.g. a product "has" no orderedItems, but it may have a loose relationship to a collection of ordered
	 * items where the product can be POSTed to. The thing that semantically <em>holds</em> ordered items is an order,
	 * not a product. Hence the order would be the collection holder.
	 *
	 * @return collection holder
	 */
	@JsonIgnore
	public TypedResource getCollectionHolder() {
		return collectionHolder;
	}


	public void setCollectionHolder(TypedResource collectionHolder) {
		this.collectionHolder = collectionHolder;
	}
}
