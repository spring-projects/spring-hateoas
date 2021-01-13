/*
 * Copyright 2019-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * Capture standard IANA-based link relations.
 *
 * @see {@link https://www.iana.org/assignments/link-relations/link-relations.xhtml}
 * @see {@link https://tools.ietf.org/html/rfc8288}
 * @see {@link https://github.com/link-relations/registry}
 * @author Greg Turnquist
 * @author Roland Kulcsár
 * @author Oliver Gierke
 * @author Vedran Pavic
 * @since 1.0
 */
public final class IanaLinkRelations {

	/**
	 * A String equivalent of {@link IanaLinkRelations#ABOUT}.
	 */
	public static final String ABOUT_VALUE = "about";

	/**
	 * Refers to a resource that is the subject of the link's context.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc6903}
	 */
	public static final LinkRelation ABOUT = LinkRelation.of(ABOUT_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#ALTERNATE}.
	 */
	public static final String ALTERNATE_VALUE = "alternate";

	/**
	 * Refers to a substitute for this context
	 *
	 * @see {@link https://www.w3.org/TR/html5/links.html#link-type-alternate}
	 */
	public static final LinkRelation ALTERNATE = LinkRelation.of(ALTERNATE_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#APPENDIX}.
	 */
	public static final String APPENDIX_VALUE = "appendix";

	/**
	 * Refers to an appendix.
	 *
	 * @see {@link https://www.w3.org/TR/1999/REC-html401-19991224}
	 */
	public static final LinkRelation APPENDIX = LinkRelation.of(APPENDIX_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#ARCHIVES}.
	 */
	public static final String ARCHIVES_VALUE = "archives";

	/**
	 * Refers to a collection of records, documents, or other materials of historical interest.
	 *
	 * @see {@link https://www.w3.org/TR/2011/WD-html5-20110113/links.html#rel-archives}
	 */
	public static final LinkRelation ARCHIVES = LinkRelation.of(ARCHIVES_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#AUTHOR}.
	 */
	public static final String AUTHOR_VALUE = "author";

	/**
	 * Refers to the context's author.
	 *
	 * @see {@link https://www.w3.org/TR/html5/links.html#link-type-author}
	 */
	public static final LinkRelation AUTHOR = LinkRelation.of(AUTHOR_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#BLOCKED_BY}.
	 */
	public static final String BLOCKED_BY_VALUE = "blocked-by";

	/**
	 * Identifies the entity that blocks access to a resource following receipt of a legal demand.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc7725}
	 */
	public static final LinkRelation BLOCKED_BY = LinkRelation.of(BLOCKED_BY_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#BOOKMARK}.
	 */
	public static final String BOOKMARK_VALUE = "bookmark";

	/**
	 * Gives a permanent link to use for bookmarking purposes.
	 *
	 * @see {@link https://www.w3.org/TR/html5/links.html#link-type-bookmark}
	 */
	public static final LinkRelation BOOKMARK = LinkRelation.of(BOOKMARK_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#CANONICAL}.
	 */
	public static final String CANONICAL_VALUE = "canonical";

	/**
	 * Designates the preferred version of a resource (the IRI and its contents).
	 *
	 * @see {@link https://tools.ietf.org/html/rfc6596}
	 */
	public static final LinkRelation CANONICAL = LinkRelation.of(CANONICAL_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#CHAPTER}.
	 */
	public static final String CHAPTER_VALUE = "chapter";

	/**
	 * Refers to a chapter in a collection of resources.
	 *
	 * @see {@link https://www.w3.org/TR/1999/REC-html401-19991224}
	 */
	public static final LinkRelation CHAPTER = LinkRelation.of(CHAPTER_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#CITE_AS}.
	 */
	public static final String CITE_AS_VALUE = "cite-as";

	/**
	 * Indicates that the link target is preferred over the link context for the purpose of referencing.
	 *
	 * @see {@link https://datatracker.ietf.org/doc/draft-vandesompel-citeas/}
	 */
	public static final LinkRelation CITE_AS = LinkRelation.of(CITE_AS_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#COLLECTION}.
	 */
	public static final String COLLECTION_VALUE = "collection";

	/**
	 * The target IRI points to a resource which represents the collection resource for the context IRI.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc6573}
	 */
	public static final LinkRelation COLLECTION = LinkRelation.of(COLLECTION_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#CONTENTS}.
	 */
	public static final String CONTENTS_VALUE = "contents";

	/**
	 * Refers to a table of contents.
	 *
	 * @see {@link https://www.w3.org/TR/1999/REC-html401-19991224}
	 */
	public static final LinkRelation CONTENTS = LinkRelation.of(CONTENTS_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#CONVERTED_FROM}.
	 */
	public static final String CONVERTED_FROM_VALUE = "convertedFrom";

	/**
	 * The document linked to was later converted to the document that contains this link relation. For example, an RFC
	 * can have a link to the Internet-Draft that became the RFC; in that case, the link relation would be
	 * "convertedFrom".
	 *
	 * @see {@link https://tools.ietf.org/html/rfc7991}
	 */
	public static final LinkRelation CONVERTED_FROM = LinkRelation.of(CONVERTED_FROM_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#COPYRIGHT}.
	 */
	public static final String COPYRIGHT_VALUE = "copyright";

	/**
	 * Refers to a copyright statement that applies to the link's context.
	 *
	 * @see {@link https://www.w3.org/TR/1999/REC-html401-19991224}
	 */
	public static final LinkRelation COPYRIGHT = LinkRelation.of(COPYRIGHT_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#CREATE_FORM}.
	 */
	public static final String CREATE_FORM_VALUE = "create-form";

	/**
	 * The target IRI points to a resource where a submission form can be obtained.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc6861}
	 */
	public static final LinkRelation CREATE_FORM = LinkRelation.of(CREATE_FORM_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#CURRENT}.
	 */
	public static final String CURRENT_VALUE = "current";

	/**
	 * Refers to a resource containing the most recent item(s) in a collection of resources.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc5005}
	 */
	public static final LinkRelation CURRENT = LinkRelation.of(CURRENT_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#DESCRIBED_BY}.
	 */
	public static final String DESCRIBED_BY_VALUE = "describedBy";

	/**
	 * Refers to a resource providing information about the link's context.
	 *
	 * @see {@link https://www.w3.org/TR/powder-dr/#assoc-linking}
	 */
	public static final LinkRelation DESCRIBED_BY = LinkRelation.of(DESCRIBED_BY_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#DESCRIBES}.
	 */
	public static final String DESCRIBES_VALUE = "describes";

	/**
	 * The relationship A 'describes' B asserts that resource A provides a description of resource B. There are no
	 * constraints on the format or representation of either A or B, neither are there any further constraints on either
	 * resource.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc6892}
	 */
	public static final LinkRelation DESCRIBES = LinkRelation.of(DESCRIBES_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#DISCLOSURE}.
	 */
	public static final String DISCLOSURE_VALUE = "disclosure";

	/**
	 * Refers to a list of patent disclosures made with respect to material for which 'disclosure' relation is specified.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc6579}
	 */
	public static final LinkRelation DISCLOSURE = LinkRelation.of(DISCLOSURE_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#DNS_PREFETCH}.
	 */
	public static final String DNS_PREFETCH_VALUE = "dns-prefetch";

	/**
	 * Used to indicate an origin that will be used to fetch required resources for the link context, and that the user
	 * agent ought to resolve as early as possible.
	 *
	 * @see {@link https://www.w3.org/TR/resource-hints/}
	 */
	public static final LinkRelation DNS_PREFETCH = LinkRelation.of(DNS_PREFETCH_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#DUPLICATE}.
	 */
	public static final String DUPLICATE_VALUE = "duplicate";

	/**
	 * Refers to a resource whose available representations are byte-for-byte identical with the corresponding
	 * representations of the context IRI.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc6249}
	 */
	public static final LinkRelation DUPLICATE = LinkRelation.of(DUPLICATE_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#EDIT}.
	 */
	public static final String EDIT_VALUE = "edit";

	/**
	 * Refers to a resource that can be used to edit the link's context.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc5023}
	 */
	public static final LinkRelation EDIT = LinkRelation.of(EDIT_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#EDIT_FORM}.
	 */
	public static final String EDIT_FORM_VALUE = "edit-form";

	/**
	 * The target IRI points to a resource where a submission form for editing associated resource can be obtained.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc6861}
	 */
	public static final LinkRelation EDIT_FORM = LinkRelation.of(EDIT_FORM_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#EDIT_MEDIA}.
	 */
	public static final String EDIT_MEDIA_VALUE = "edit-media";

	/**
	 * Refers to a resource that can be used to edit media associated with the link's context.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc5023}
	 */
	public static final LinkRelation EDIT_MEDIA = LinkRelation.of(EDIT_MEDIA_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#ENCLOSURE}.
	 */
	public static final String ENCLOSURE_VALUE = "enclosure";

	/**
	 * Identifies a related resource that is potentially large and might require special handling.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc4287}
	 */
	public static final LinkRelation ENCLOSURE = LinkRelation.of(ENCLOSURE_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#FIRST}.
	 */
	public static final String FIRST_VALUE = "first";

	/**
	 * An IRI that refers to the furthest preceding resource in a series of resources.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc8288}
	 */
	public static final LinkRelation FIRST = LinkRelation.of(FIRST_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#GLOSSARY}.
	 */
	public static final String GLOSSARY_VALUE = "glossary";

	/**
	 * Refers to a glossary of terms.
	 *
	 * @see {@link https://www.w3.org/TR/1999/REC-html401-19991224}
	 */
	public static final LinkRelation GLOSSARY = LinkRelation.of(GLOSSARY_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#HELP}.
	 */
	public static final String HELP_VALUE = "help";

	/**
	 * Refers to context-sensitive help.
	 *
	 * @see {@link https://www.w3.org/TR/html5/links.html#link-type-help}
	 */
	public static final LinkRelation HELP = LinkRelation.of(HELP_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#HOSTS}.
	 */
	public static final String HOSTS_VALUE = "hosts";

	/**
	 * Refers to a resource hosted by the server indicated by the link context.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc6690}
	 */
	public static final LinkRelation HOSTS = LinkRelation.of(HOSTS_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#HUB}.
	 */
	public static final String HUB_VALUE = "hub";

	/**
	 * Refers to a hub that enables registration for notification of updates to the context.
	 *
	 * @see {@link https://pubsubhubbub.googlecode.com}
	 */
	public static final LinkRelation HUB = LinkRelation.of(HUB_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#ICON}.
	 */
	public static final String ICON_VALUE = "icon";

	/**
	 * Refers to an icon representing the link's context.
	 *
	 * @see {@link https://www.w3.org/TR/html5/links.html#link-type-icon}
	 */
	public static final LinkRelation ICON = LinkRelation.of(ICON_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#INDEX}.
	 */
	public static final String INDEX_VALUE = "index";

	/**
	 * Refers to an index.
	 *
	 * @see {@link https://www.w3.org/TR/1999/REC-html401-19991224}
	 */
	public static final LinkRelation INDEX = LinkRelation.of(INDEX_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#INTERVAL_AFTER}.
	 */
	public static final String INTERVAL_AFTER_VALUE = "intervalAfter";

	/**
	 * refers to a resource associated with a time interval that ends before the beginning of the time interval associated
	 * with the context resource
	 *
	 * @see {@link https://www.w3.org/TR/owl-time/#time:intervalAfter section 4.2.21}
	 */
	public static final LinkRelation INTERVAL_AFTER = LinkRelation.of(INTERVAL_AFTER_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#INTERVAL_BEFORE}.
	 */
	public static final String INTERVAL_BEFORE_VALUE = "intervalBefore";

	/**
	 * refers to a resource associated with a time interval that begins after the end of the time interval associated with
	 * the context resource
	 *
	 * @see {@link https://www.w3.org/TR/owl-time/#time:intervalBefore section 4.2.22}
	 */
	public static final LinkRelation INTERVAL_BEFORE = LinkRelation.of(INTERVAL_BEFORE_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#INTERVAL_CONTAINS}.
	 */
	public static final String INTERVAL_CONTAINS_VALUE = "intervalContains";

	/**
	 * refers to a resource associated with a time interval that begins after the beginning of the time interval
	 * associated with the context resource, and ends before the end of the time interval associated with the context
	 * resource
	 *
	 * @see {@link https://www.w3.org/TR/owl-time/#time:intervalContains section 4.2.23}
	 */
	public static final LinkRelation INTERVAL_CONTAINS = LinkRelation.of(INTERVAL_CONTAINS_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#INTERVAL_DISJOINT}.
	 */
	public static final String INTERVAL_DISJOINT_VALUE = "intervalDisjoint";

	/**
	 * refers to a resource associated with a time interval that begins after the end of the time interval associated with
	 * the context resource, or ends before the beginning of the time interval associated with the context resource
	 *
	 * @see {@link https://www.w3.org/TR/owl-time/#time:intervalDisjoint section 4.2.24}
	 */
	public static final LinkRelation INTERVAL_DISJOINT = LinkRelation.of(INTERVAL_DISJOINT_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#INTERVAL_DURING}.
	 */
	public static final String INTERVAL_DURING_VALUE = "intervalDuring";

	/**
	 * refers to a resource associated with a time interval that begins before the beginning of the time interval
	 * associated with the context resource, and ends after the end of the time interval associated with the context
	 * resource
	 *
	 * @see {@link https://www.w3.org/TR/owl-time/#time:intervalDuring section 4.2.25}
	 */
	public static final LinkRelation INTERVAL_DURING = LinkRelation.of(INTERVAL_DURING_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#INTERVAL_EQUALS}.
	 */
	public static final String INTERVAL_EQUALS_VALUE = "intervalEquals";

	/**
	 * refers to a resource associated with a time interval whose beginning coincides with the beginning of the time
	 * interval associated with the context resource, and whose end coincides with the end of the time interval associated
	 * with the context resource
	 *
	 * @see {@link https://www.w3.org/TR/owl-time/#time:intervalEquals section 4.2.26}
	 */
	public static final LinkRelation INTERVAL_EQUALS = LinkRelation.of(INTERVAL_EQUALS_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#INTERVAL_FINISHED_BY}.
	 */
	public static final String INTERVAL_FINISHED_BY_VALUE = "intervalFinishedBy";

	/**
	 * refers to a resource associated with a time interval that begins after the beginning of the time interval
	 * associated with the context resource, and whose end coincides with the end of the time interval associated with the
	 * context resource
	 *
	 * @see {@link https://www.w3.org/TR/owl-time/#time:intervalFinishedBy section 4.2.27}
	 */
	public static final LinkRelation INTERVAL_FINISHED_BY = LinkRelation.of(INTERVAL_FINISHED_BY_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#INTERVAL_FINISHES}.
	 */
	public static final String INTERVAL_FINISHES_VALUE = "intervalFinishes";

	/**
	 * refers to a resource associated with a time interval that begins before the beginning of the time interval
	 * associated with the context resource, and whose end coincides with the end of the time interval associated with the
	 * context resource
	 *
	 * @see {@link https://www.w3.org/TR/owl-time/#time:intervalFinishes section 4.2.28}
	 */
	public static final LinkRelation INTERVAL_FINISHES = LinkRelation.of(INTERVAL_FINISHES_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#INTERVAL_IN}.
	 */
	public static final String INTERVAL_IN_VALUE = "intervalIn";

	/**
	 * refers to a resource associated with a time interval that begins before or is coincident with the beginning of the
	 * time interval associated with the context resource, and ends after or is coincident with the end of the time
	 * interval associated with the context resource
	 *
	 * @see {@link https://www.w3.org/TR/owl-time/#time:intervalIn section 4.2.29}
	 */
	public static final LinkRelation INTERVAL_IN = LinkRelation.of(INTERVAL_IN_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#INTERVAL_MEETS}.
	 */
	public static final String INTERVAL_MEETS_VALUE = "intervalMeets";

	/**
	 * refers to a resource associated with a time interval whose beginning coincides with the end of the time interval
	 * associated with the context resource
	 *
	 * @see {@link https://www.w3.org/TR/owl-time/#time:intervalMeets section 4.2.30}
	 */
	public static final LinkRelation INTERVAL_MEETS = LinkRelation.of(INTERVAL_MEETS_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#INTERVAL_MET_BY}.
	 */
	public static final String INTERVAL_MET_BY_VALUE = "intervalMetBy";

	/**
	 * refers to a resource associated with a time interval whose beginning coincides with the end of the time interval
	 * associated with the context resource
	 *
	 * @see {@link https://www.w3.org/TR/owl-time/#time:intervalMetBy section 4.2.31}
	 */
	public static final LinkRelation INTERVAL_MET_BY = LinkRelation.of(INTERVAL_MET_BY_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#INTERVAL_OVERLAPPED_BY}.
	 */
	public static final String INTERVAL_OVERLAPPED_BY_VALUE = "intervalOverlappedBy";

	/**
	 * refers to a resource associated with a time interval that begins before the beginning of the time interval
	 * associated with the context resource, and ends after the beginning of the time interval associated with the context
	 * resource
	 *
	 * @see {@link https://www.w3.org/TR/owl-time/#time:intervalOverlappedBy section 4.2.32}
	 */
	public static final LinkRelation INTERVAL_OVERLAPPED_BY = LinkRelation.of(INTERVAL_OVERLAPPED_BY_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#INTERVAL_OVERLAPS}.
	 */
	public static final String INTERVAL_OVERLAPS_VALUE = "intervalOverlaps";

	/**
	 * refers to a resource associated with a time interval that begins before the end of the time interval associated
	 * with the context resource, and ends after the end of the time interval associated with the context resource
	 *
	 * @see {@link https://www.w3.org/TR/owl-time/#time:intervalOverlaps section 4.2.33}
	 */
	public static final LinkRelation INTERVAL_OVERLAPS = LinkRelation.of(INTERVAL_OVERLAPS_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#INTERVAL_STARTED_BY}.
	 */
	public static final String INTERVAL_STARTED_BY_VALUE = "intervalStartedBy";

	/**
	 * refers to a resource associated with a time interval whose beginning coincides with the beginning of the time
	 * interval associated with the context resource, and ends before the end of the time interval associated with the
	 * context resource
	 *
	 * @see {@link https://www.w3.org/TR/owl-time/#time:intervalStartedBy section 4.2.34}
	 */
	public static final LinkRelation INTERVAL_STARTED_BY = LinkRelation.of(INTERVAL_STARTED_BY_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#INTERVAL_STARTS}.
	 */
	public static final String INTERVAL_STARTS_VALUE = "intervalStarts";

	/**
	 * refers to a resource associated with a time interval whose beginning coincides with the beginning of the time
	 * interval associated with the context resource, and ends after the end of the time interval associated with the
	 * context resource
	 *
	 * @see {@link https://www.w3.org/TR/owl-time/#time:intervalStarts section 4.2.35}
	 */
	public static final LinkRelation INTERVAL_STARTS = LinkRelation.of(INTERVAL_STARTS_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#ITEM}.
	 */
	public static final String ITEM_VALUE = "item";

	/**
	 * The target IRI points to a resource that is a member of the collection represented by the context IRI.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc6573}
	 */
	public static final LinkRelation ITEM = LinkRelation.of(ITEM_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#LAST}.
	 */
	public static final String LAST_VALUE = "last";

	/**
	 * An IRI that refers to the furthest following resource in a series of resources.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc8288}
	 */
	public static final LinkRelation LAST = LinkRelation.of(LAST_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#LATEST_VERSION}.
	 */
	public static final String LATEST_VERSION_VALUE = "latest-version";

	/**
	 * Points to a resource containing the latest (e.g., current) version of the context.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc5829}
	 */
	public static final LinkRelation LATEST_VERSION = LinkRelation.of(LATEST_VERSION_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#LICENSE}.
	 */
	public static final String LICENSE_VALUE = "license";

	/**
	 * Refers to a license associated with this context.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc4946}
	 */
	public static final LinkRelation LICENSE = LinkRelation.of(LICENSE_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#LRDD}.
	 */
	public static final String LRDD_VALUE = "lrdd";

	/**
	 * Refers to further information about the link's context, expressed as a LRDD ("Link-based Resource Descriptor
	 * Document") resource. See RFC6415 for information about processing this relation type in host-meta documents. When
	 * used elsewhere, it refers to additional links and other metadata. Multiple instances indicate additional LRDD
	 * resources. LRDD resources MUST have an "application/xrd+xml" representation, and MAY have others.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc6415}
	 */
	public static final LinkRelation LRDD = LinkRelation.of(LRDD_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#MEMENTO}.
	 */
	public static final String MEMENTO_VALUE = "memento";

	/**
	 * The Target IRI points to a Memento, a fixed resource that will not change state anymore.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc7089}
	 */
	public static final LinkRelation MEMENTO = LinkRelation.of(MEMENTO_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#MONITOR}.
	 */
	public static final String MONITOR_VALUE = "monitor";

	/**
	 * Refers to a resource that can be used to monitor changes in an HTTP resource.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc5989}
	 */
	public static final LinkRelation MONITOR = LinkRelation.of(MONITOR_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#MONITOR_GROUP}.
	 */
	public static final String MONITOR_GROUP_VALUE = "monitor-group";

	/**
	 * Refers to a resource that can be used to monitor changes in a specified group of HTTP resources.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc5989}
	 */
	public static final LinkRelation MONITOR_GROUP = LinkRelation.of(MONITOR_GROUP_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#NEXT}.
	 */
	public static final String NEXT_VALUE = "next";

	/**
	 * Indicates that the link's context is a part of a series, and that the next in the series is the link target.
	 *
	 * @see {@link https://www.w3.org/TR/html5/links.html#link-type-next}
	 */
	public static final LinkRelation NEXT = LinkRelation.of(NEXT_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#NEXT_ARCHIVE}.
	 */
	public static final String NEXT_ARCHIVE_VALUE = "next-archive";

	/**
	 * Refers to the immediately following archive resource.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc5005}
	 */
	public static final LinkRelation NEXT_ARCHIVE = LinkRelation.of(NEXT_ARCHIVE_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#NOFOLLOW}.
	 */
	public static final String NOFOLLOW_VALUE = "nofollow";

	/**
	 * Indicates that the context‚Äôs original author or publisher does not endorse the link target.
	 *
	 * @see {@link https://www.w3.org/TR/html5/links.html#link-type-nofollow}
	 */
	public static final LinkRelation NOFOLLOW = LinkRelation.of(NOFOLLOW_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#NOREFERRER}.
	 */
	public static final String NOREFERRER_VALUE = "noreferrer";

	/**
	 * Indicates that no referrer information is to be leaked when following the link.
	 *
	 * @see {@link https://www.w3.org/TR/html5/links.html#link-type-noreferrer}
	 */
	public static final LinkRelation NOREFERRER = LinkRelation.of(NOREFERRER_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#ORIGINAL}.
	 */
	public static final String ORIGINAL_VALUE = "original";

	/**
	 * The Target IRI points to an Original Resource.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc7089}
	 */
	public static final LinkRelation ORIGINAL = LinkRelation.of(ORIGINAL_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#PAYMENT}.
	 */
	public static final String PAYMENT_VALUE = "payment";

	/**
	 * Indicates a resource where payment is accepted.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc8288}
	 */
	public static final LinkRelation PAYMENT = LinkRelation.of(PAYMENT_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#PINGBACK}.
	 */
	public static final String PINGBACK_VALUE = "pingback";

	/**
	 * Gives the address of the pingback resource for the link context.
	 *
	 * @see {@link https://www.hixie.ch/specs/pingback/pingback}
	 */
	public static final LinkRelation PINGBACK = LinkRelation.of(PINGBACK_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#PRECONNECT}.
	 */
	public static final String PRECONNECT_VALUE = "preconnect";

	/**
	 * Used to indicate an origin that will be used to fetch required resources for the link context. Initiating an early
	 * connection, which includes the DNS lookup, TCP handshake, and optional TLS negotiation, allows the user agent to
	 * mask the high latency costs of establishing a connection.
	 *
	 * @see {@link https://www.w3.org/TR/resource-hints/}
	 */
	public static final LinkRelation PRECONNECT = LinkRelation.of(PRECONNECT_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#PREDECESSOR_VERSION}.
	 */
	public static final String PREDECESSOR_VERSION_VALUE = "predecessor-version";

	/**
	 * Points to a resource containing the predecessor version in the version history.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc5829}
	 */
	public static final LinkRelation PREDECESSOR_VERSION = LinkRelation.of(PREDECESSOR_VERSION_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#PREFETCH}.
	 */
	public static final String PREFETCH_VALUE = "prefetch";

	/**
	 * The prefetch link relation type is used to identify a resource that might be required by the next navigation from
	 * the link context, and that the user agent ought to fetch, such that the user agent can deliver a faster response
	 * once the resource is requested in the future.
	 *
	 * @see {@link https://www.w3.org/TR/resource-hints/}
	 */
	public static final LinkRelation PREFETCH = LinkRelation.of(PREFETCH_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#PRELOAD}.
	 */
	public static final String PRELOAD_VALUE = "preload";

	/**
	 * Refers to a resource that should be loaded early in the processing of the link's context, without blocking
	 * rendering.
	 *
	 * @see {@link https://www.w3.org/TR/preload/}
	 */
	public static final LinkRelation PRELOAD = LinkRelation.of(PRELOAD_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#PRERENDER}.
	 */
	public static final String PRERENDER_VALUE = "prerender";

	/**
	 * Used to identify a resource that might be required by the next navigation from the link context, and that the user
	 * agent ought to fetch and execute, such that the user agent can deliver a faster response once the resource is
	 * requested in the future.
	 *
	 * @see {@link https://www.w3.org/TR/resource-hints/}
	 */
	public static final LinkRelation PRERENDER = LinkRelation.of(PRERENDER_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#PREV}.
	 */
	public static final String PREV_VALUE = "prev";

	/**
	 * Indicates that the link's context is a part of a series, and that the previous in the series is the link target.
	 *
	 * @see {@link https://www.w3.org/TR/html5/links.html#link-type-prev}
	 */
	public static final LinkRelation PREV = LinkRelation.of(PREV_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#PREVIEW}.
	 */
	public static final String PREVIEW_VALUE = "preview";

	/**
	 * Refers to a resource that provides a preview of the link's context.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc6903, section 3}
	 */
	public static final LinkRelation PREVIEW = LinkRelation.of(PREVIEW_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#PREVIOUS}.
	 */
	public static final String PREVIOUS_VALUE = "previous";

	/**
	 * Refers to the previous resource in an ordered series of resources. Synonym for "prev".
	 *
	 * @see {@link https://www.w3.org/TR/1999/REC-html401-19991224}
	 */
	public static final LinkRelation PREVIOUS = LinkRelation.of(PREVIOUS_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#PREV_ARCHIVE}.
	 */
	public static final String PREV_ARCHIVE_VALUE = "prev-archive";

	/**
	 * Refers to the immediately preceding archive resource.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc5005}
	 */
	public static final LinkRelation PREV_ARCHIVE = LinkRelation.of(PREV_ARCHIVE_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#PRIVACY_POLICY}.
	 */
	public static final String PRIVACY_POLICY_VALUE = "privacy-policy";

	/**
	 * Refers to a privacy policy associated with the link's context.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc6903, section 4}
	 */
	public static final LinkRelation PRIVACY_POLICY = LinkRelation.of(PRIVACY_POLICY_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#PROFILE}.
	 */
	public static final String PROFILE_VALUE = "profile";

	/**
	 * Identifying that a resource representation conforms to a certain profile, without affecting the non-profile
	 * semantics of the resource representation.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc6906}
	 */
	public static final LinkRelation PROFILE = LinkRelation.of(PROFILE_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#RELATED}.
	 */
	public static final String RELATED_VALUE = "related";

	/**
	 * Identifies a related resource.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc4287}
	 */
	public static final LinkRelation RELATED = LinkRelation.of(RELATED_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#RESTCONF}.
	 */
	public static final String RESTCONF_VALUE = "restconf";

	/**
	 * Identifies the root of RESTCONF API as configured on this HTTP server. The "restconf" relation defines the root of
	 * the API defined in RFC8040. Subsequent revisions of RESTCONF will use alternate relation values to support protocol
	 * versioning.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc8040}
	 */
	public static final LinkRelation RESTCONF = LinkRelation.of(RESTCONF_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#REPLIES}.
	 */
	public static final String REPLIES_VALUE = "replies";

	/**
	 * Identifies a resource that is a reply to the context of the link.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc4685}
	 */
	public static final LinkRelation REPLIES = LinkRelation.of(REPLIES_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#SEARCH}.
	 */
	public static final String SEARCH_VALUE = "search";

	/**
	 * Refers to a resource that can be used to search through the link's context and related resources.
	 *
	 * @see {@link http://www.opensearch.org/Specifications/OpenSearch/1.1}
	 */
	public static final LinkRelation SEARCH = LinkRelation.of(SEARCH_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#SECTION}.
	 */
	public static final String SECTION_VALUE = "section";

	/**
	 * Refers to a section in a collection of resources.
	 *
	 * @see {@link https://www.w3.org/TR/1999/REC-html401-19991224}
	 */
	public static final LinkRelation SECTION = LinkRelation.of(SECTION_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#SELF}.
	 */
	public static final String SELF_VALUE = "self";

	/**
	 * Conveys an identifier for the link's context.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc4287}
	 */
	public static final LinkRelation SELF = LinkRelation.of(SELF_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#SERVICE}.
	 */
	public static final String SERVICE_VALUE = "service";

	/**
	 * Indicates a URI that can be used to retrieve a service document.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc5023}
	 */
	public static final LinkRelation SERVICE = LinkRelation.of(SERVICE_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#START}.
	 */
	public static final String START_VALUE = "start";

	/**
	 * Refers to the first resource in a collection of resources.
	 *
	 * @see {@link https://www.w3.org/TR/1999/REC-html401-19991224}
	 */
	public static final LinkRelation START = LinkRelation.of(START_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#STYLESHEET}.
	 */
	public static final String STYLESHEET_VALUE = "stylesheet";

	/**
	 * Refers to a stylesheet.
	 *
	 * @see {@link https://www.w3.org/TR/html5/links.html#link-type-stylesheet}
	 */
	public static final LinkRelation STYLESHEET = LinkRelation.of(STYLESHEET_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#SUBSECTION}.
	 */
	public static final String SUBSECTION_VALUE = "subsection";

	/**
	 * Refers to a resource serving as a subsection in a collection of resources.
	 *
	 * @see {@link https://www.w3.org/TR/1999/REC-html401-19991224}
	 */
	public static final LinkRelation SUBSECTION = LinkRelation.of(SUBSECTION_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#SUCCESSOR_VERSION}.
	 */
	public static final String SUCCESSOR_VERSION_VALUE = "successor-versions";

	/**
	 * Points to a resource containing the successor version in the version history.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc5829}
	 */
	public static final LinkRelation SUCCESSOR_VERSION = LinkRelation.of(SUCCESSOR_VERSION_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#TAG}.
	 */
	public static final String TAG_VALUE = "tag";

	/**
	 * Gives a tag (identified by the given address) that applies to the current document.
	 *
	 * @see {@link https://www.w3.org/TR/html5/links.html#link-type-tag}
	 */
	public static final LinkRelation TAG = LinkRelation.of(TAG_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#TERMS_OF_SERVICE}.
	 */
	public static final String TERMS_OF_SERVICE_VALUE = "terms-of-service";

	/**
	 * Refers to the terms of service associated with the link's context.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc6903, section 5}
	 */
	public static final LinkRelation TERMS_OF_SERVICE = LinkRelation.of(TERMS_OF_SERVICE_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#TIMEGATE}.
	 */
	public static final String TIMEGATE_VALUE = "timegate";

	/**
	 * The Target IRI points to a TimeGate for an Original Resource.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc7089}
	 */
	public static final LinkRelation TIMEGATE = LinkRelation.of(TIMEGATE_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#TIMEMAP}.
	 */
	public static final String TIMEMAP_VALUE = "timemap";

	/**
	 * The Target IRI points to a TimeMap for an Original Resource.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc7089}
	 */
	public static final LinkRelation TIMEMAP = LinkRelation.of(TIMEMAP_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#TYPE}.
	 */
	public static final String TYPE_VALUE = "type";

	/**
	 * Refers to a resource identifying the abstract semantic type of which the link's context is considered to be an
	 * instance.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc6903, section 6}
	 */
	public static final LinkRelation TYPE = LinkRelation.of(TYPE_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#UP}.
	 */
	public static final String UP_VALUE = "up";

	/**
	 * Refers to a parent document in a hierarchy of documents.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc8288}
	 */
	public static final LinkRelation UP = LinkRelation.of(UP_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#VERSION_HISTORY}.
	 */
	public static final String VERSION_HISTORY_VALUE = "version-history";

	/**
	 * Points to a resource containing the version history for the context.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc5829}
	 */
	public static final LinkRelation VERSION_HISTORY = LinkRelation.of(VERSION_HISTORY_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#VIA}.
	 */
	public static final String VIA_VALUE = "via";

	/**
	 * Identifies a resource that is the source of the information in the link's context.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc4287}
	 */
	public static final LinkRelation VIA = LinkRelation.of(VIA_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#WEBMENTION}.
	 */
	public static final String WEBMENTION_VALUE = "webmention";

	/**
	 * Identifies a target URI that supports the Webmention protcol. This allows clients that mention a resource in some
	 * form of publishing process to contact that endpoint and inform it that this resource has been mentioned.
	 *
	 * @see {@link https://www.w3.org/TR/webmention/}
	 */
	public static final LinkRelation WEBMENTION = LinkRelation.of(WEBMENTION_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#WORKING_COPY}.
	 */
	public static final String WORKING_COPY_VALUE = "working-copy";

	/**
	 * Points to a working copy for this resource.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc5829}
	 */
	public static final LinkRelation WORKING_COPY = LinkRelation.of(WORKING_COPY_VALUE);

	/**
	 * A String equivalent of {@link IanaLinkRelations#WORKING_COPY_OF}.
	 */
	public static final String WORKING_COPY_OF_VALUE = "working-copy-of";

	/**
	 * Points to the versioned resource from which this working copy was obtained.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc5829}
	 */
	public static final LinkRelation WORKING_COPY_OF = LinkRelation.of(WORKING_COPY_OF_VALUE);

	/**
	 * Consolidated collection of {@link IanaLinkRelations}s.
	 */
	private static final Set<LinkRelation> LINK_RELATIONS;

	static {

		LINK_RELATIONS = Arrays.stream(IanaLinkRelations.class.getDeclaredFields()) //
				.filter(ReflectionUtils::isPublicStaticFinal) //
				.filter(field -> LinkRelation.class.equals(field.getType())) //
				.map(it -> ReflectionUtils.getField(it, null)) //
				.map(LinkRelation.class::cast) //
				.collect(Collectors.toSet());
	}

	private IanaLinkRelations() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	/**
	 * Is this relation an IANA standard? Per RFC 8288, parsing of link relations is case insensitive.
	 *
	 * @param relation must not be {@literal null}.
	 * @return boolean
	 */
	public static boolean isIanaRel(String relation) {

		Assert.notNull(relation, "Link relation must not be null!");

		return LINK_RELATIONS.stream() //
				.anyMatch(it -> it.value().equalsIgnoreCase(relation));
	}

	/**
	 * Is this relation an IANA standard? Per RFC 8288, parsing of link relations is case insensitive.
	 *
	 * @param relation must not be {@literal null}.
	 * @return
	 */
	public static boolean isIanaRel(LinkRelation relation) {

		Assert.notNull(relation, "Link relation must not be null!");

		return LINK_RELATIONS.contains(relation) //
				|| LINK_RELATIONS.stream().anyMatch(it -> it.isSameAs(relation));

	}

	/**
	 * Convert a string-based link relation to a {@link IanaLinkRelations}. Per RFC8288, parsing of link relations is case
	 * insensitive.
	 *
	 * @param relation as a string
	 * @return the link relation as a {@link LinkRelation}
	 */
	public static LinkRelation parse(String relation) {

		return LINK_RELATIONS.stream() //
				.filter(it -> it.value().equalsIgnoreCase(relation)) //
				.findFirst() //
				.orElseThrow(() -> new IllegalArgumentException(relation + " is not a valid IANA link relation!"));
	}
}
