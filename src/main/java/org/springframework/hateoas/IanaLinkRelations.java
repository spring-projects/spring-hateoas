/*
 * Copyright 2019-2020 the original author or authors.
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

import lombok.experimental.UtilityClass;

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
 * @since 1.0
 */
@UtilityClass
public class IanaLinkRelations {

	/**
	 * Refers to a resource that is the subject of the link's context.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc6903}
	 */
	public static final LinkRelation ABOUT = LinkRelation.of("about");

	/**
	 * Refers to a substitute for this context
	 *
	 * @see {@link https://www.w3.org/TR/html5/links.html#link-type-alternate}
	 */
	public static final LinkRelation ALTERNATE = LinkRelation.of("alternate");

	/**
	 * Refers to an appendix.
	 *
	 * @see {@link https://www.w3.org/TR/1999/REC-html401-19991224}
	 */
	public static final LinkRelation APPENDIX = LinkRelation.of("appendix");

	/**
	 * Refers to a collection of records, documents, or other materials of historical interest.
	 *
	 * @see {@link https://www.w3.org/TR/2011/WD-html5-20110113/links.html#rel-archives}
	 */
	public static final LinkRelation ARCHIVES = LinkRelation.of("archives");

	/**
	 * Refers to the context's author.
	 *
	 * @see {@link https://www.w3.org/TR/html5/links.html#link-type-author}
	 */
	public static final LinkRelation AUTHOR = LinkRelation.of("author");

	/**
	 * Identifies the entity that blocks access to a resource following receipt of a legal demand.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc7725}
	 */
	public static final LinkRelation BLOCKED_BY = LinkRelation.of("blocked-by");

	/**
	 * Gives a permanent link to use for bookmarking purposes.
	 *
	 * @see {@link https://www.w3.org/TR/html5/links.html#link-type-bookmark}
	 */
	public static final LinkRelation BOOKMARK = LinkRelation.of("bookmark");

	/**
	 * Designates the preferred version of a resource (the IRI and its contents).
	 *
	 * @see {@link https://tools.ietf.org/html/rfc6596}
	 */
	public static final LinkRelation CANONICAL = LinkRelation.of("canonical");

	/**
	 * Refers to a chapter in a collection of resources.
	 *
	 * @see {@link https://www.w3.org/TR/1999/REC-html401-19991224}
	 */
	public static final LinkRelation CHAPTER = LinkRelation.of("chapter");

	/**
	 * Indicates that the link target is preferred over the link context for the purpose of referencing.
	 *
	 * @see {@link https://datatracker.ietf.org/doc/draft-vandesompel-citeas/}
	 */
	public static final LinkRelation CITE_AS = LinkRelation.of("cite-as");

	/**
	 * The target IRI points to a resource which represents the collection resource for the context IRI.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc6573}
	 */
	public static final LinkRelation COLLECTION = LinkRelation.of("collection");

	/**
	 * Refers to a table of contents.
	 *
	 * @see {@link https://www.w3.org/TR/1999/REC-html401-19991224}
	 */
	public static final LinkRelation CONTENTS = LinkRelation.of("contents");

	/**
	 * The document linked to was later converted to the document that contains this link relation. For example, an RFC
	 * can have a link to the Internet-Draft that became the RFC; in that case, the link relation would be
	 * "convertedFrom".
	 *
	 * @see {@link https://tools.ietf.org/html/rfc7991}
	 */
	public static final LinkRelation CONVERTED_FROM = LinkRelation.of("convertedFrom");

	/**
	 * Refers to a copyright statement that applies to the link's context.
	 *
	 * @see {@link https://www.w3.org/TR/1999/REC-html401-19991224}
	 */
	public static final LinkRelation COPYRIGHT = LinkRelation.of("copyright");

	/**
	 * The target IRI points to a resource where a submission form can be obtained.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc6861}
	 */
	public static final LinkRelation CREATE_FORM = LinkRelation.of("create-form");

	/**
	 * Refers to a resource containing the most recent item(s) in a collection of resources.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc5005}
	 */
	public static final LinkRelation CURRENT = LinkRelation.of("current");

	/**
	 * Refers to a resource providing information about the link's context.
	 *
	 * @see {@link https://www.w3.org/TR/powder-dr/#assoc-linking}
	 */
	public static final LinkRelation DESCRIBED_BY = LinkRelation.of("describedBy");

	/**
	 * The relationship A 'describes' B asserts that resource A provides a description of resource B. There are no
	 * constraints on the format or representation of either A or B, neither are there any further constraints on either
	 * resource.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc6892}
	 */
	public static final LinkRelation DESCRIBES = LinkRelation.of("describes");

	/**
	 * Refers to a list of patent disclosures made with respect to material for which 'disclosure' relation is specified.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc6579}
	 */
	public static final LinkRelation DISCLOSURE = LinkRelation.of("disclosure");

	/**
	 * Used to indicate an origin that will be used to fetch required resources for the link context, and that the user
	 * agent ought to resolve as early as possible.
	 *
	 * @see {@link https://www.w3.org/TR/resource-hints/}
	 */
	public static final LinkRelation DNS_PREFETCH = LinkRelation.of("dns-prefetch");

	/**
	 * Refers to a resource whose available representations are byte-for-byte identical with the corresponding
	 * representations of the context IRI.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc6249}
	 */
	public static final LinkRelation DUPLICATE = LinkRelation.of("duplicate");

	/**
	 * Refers to a resource that can be used to edit the link's context.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc5023}
	 */
	public static final LinkRelation EDIT = LinkRelation.of("edit");

	/**
	 * The target IRI points to a resource where a submission form for editing associated resource can be obtained.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc6861}
	 */
	public static final LinkRelation EDIT_FORM = LinkRelation.of("edit-form");

	/**
	 * Refers to a resource that can be used to edit media associated with the link's context.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc5023}
	 */
	public static final LinkRelation EDIT_MEDIA = LinkRelation.of("edit-media");

	/**
	 * Identifies a related resource that is potentially large and might require special handling.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc4287}
	 */
	public static final LinkRelation ENCLOSURE = LinkRelation.of("enclosure");

	/**
	 * An IRI that refers to the furthest preceding resource in a series of resources.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc8288}
	 */
	public static final LinkRelation FIRST = LinkRelation.of("first");

	/**
	 * Refers to a glossary of terms.
	 *
	 * @see {@link https://www.w3.org/TR/1999/REC-html401-19991224}
	 */
	public static final LinkRelation GLOSSARY = LinkRelation.of("glossary");

	/**
	 * Refers to context-sensitive help.
	 *
	 * @see {@link https://www.w3.org/TR/html5/links.html#link-type-help}
	 */
	public static final LinkRelation HELP = LinkRelation.of("help");

	/**
	 * Refers to a resource hosted by the server indicated by the link context.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc6690}
	 */
	public static final LinkRelation HOSTS = LinkRelation.of("hosts");

	/**
	 * Refers to a hub that enables registration for notification of updates to the context.
	 *
	 * @see {@link https://pubsubhubbub.googlecode.com}
	 */
	public static final LinkRelation HUB = LinkRelation.of("hub");

	/**
	 * Refers to an icon representing the link's context.
	 *
	 * @see {@link https://www.w3.org/TR/html5/links.html#link-type-icon}
	 */
	public static final LinkRelation ICON = LinkRelation.of("icon");

	/**
	 * Refers to an index.
	 *
	 * @see {@link https://www.w3.org/TR/1999/REC-html401-19991224}
	 */
	public static final LinkRelation INDEX = LinkRelation.of("index");

	/**
	 * refers to a resource associated with a time interval that ends before the beginning of the time interval associated
	 * with the context resource
	 *
	 * @see {@link https://www.w3.org/TR/owl-time/#time:intervalAfter section 4.2.21}
	 */
	public static final LinkRelation INTERVAL_AFTER = LinkRelation.of("intervalAfter");

	/**
	 * refers to a resource associated with a time interval that begins after the end of the time interval associated with
	 * the context resource
	 *
	 * @see {@link https://www.w3.org/TR/owl-time/#time:intervalBefore section 4.2.22}
	 */
	public static final LinkRelation INTERVAL_BEFORE = LinkRelation.of("intervalBefore");

	/**
	 * refers to a resource associated with a time interval that begins after the beginning of the time interval
	 * associated with the context resource, and ends before the end of the time interval associated with the context
	 * resource
	 *
	 * @see {@link https://www.w3.org/TR/owl-time/#time:intervalContains section 4.2.23}
	 */
	public static final LinkRelation INTERVAL_CONTAINS = LinkRelation.of("intervalContains");

	/**
	 * refers to a resource associated with a time interval that begins after the end of the time interval associated with
	 * the context resource, or ends before the beginning of the time interval associated with the context resource
	 *
	 * @see {@link https://www.w3.org/TR/owl-time/#time:intervalDisjoint section 4.2.24}
	 */
	public static final LinkRelation INTERVAL_DISJOINT = LinkRelation.of("intervalDisjoint");

	/**
	 * refers to a resource associated with a time interval that begins before the beginning of the time interval
	 * associated with the context resource, and ends after the end of the time interval associated with the context
	 * resource
	 *
	 * @see {@link https://www.w3.org/TR/owl-time/#time:intervalDuring section 4.2.25}
	 */
	public static final LinkRelation INTERVAL_DURING = LinkRelation.of("intervalDuring");

	/**
	 * refers to a resource associated with a time interval whose beginning coincides with the beginning of the time
	 * interval associated with the context resource, and whose end coincides with the end of the time interval associated
	 * with the context resource
	 *
	 * @see {@link https://www.w3.org/TR/owl-time/#time:intervalEquals section 4.2.26}
	 */
	public static final LinkRelation INTERVAL_EQUALS = LinkRelation.of("intervalEquals");

	/**
	 * refers to a resource associated with a time interval that begins after the beginning of the time interval
	 * associated with the context resource, and whose end coincides with the end of the time interval associated with the
	 * context resource
	 *
	 * @see {@link https://www.w3.org/TR/owl-time/#time:intervalFinishedBy section 4.2.27}
	 */
	public static final LinkRelation INTERVAL_FINISHED_BY = LinkRelation.of("intervalFinishedBy");

	/**
	 * refers to a resource associated with a time interval that begins before the beginning of the time interval
	 * associated with the context resource, and whose end coincides with the end of the time interval associated with the
	 * context resource
	 *
	 * @see {@link https://www.w3.org/TR/owl-time/#time:intervalFinishes section 4.2.28}
	 */
	public static final LinkRelation INTERVAL_FINISHES = LinkRelation.of("intervalFinishes");

	/**
	 * refers to a resource associated with a time interval that begins before or is coincident with the beginning of the
	 * time interval associated with the context resource, and ends after or is coincident with the end of the time
	 * interval associated with the context resource
	 *
	 * @see {@link https://www.w3.org/TR/owl-time/#time:intervalIn section 4.2.29}
	 */
	public static final LinkRelation INTERVAL_IN = LinkRelation.of("intervalIn");

	/**
	 * refers to a resource associated with a time interval whose beginning coincides with the end of the time interval
	 * associated with the context resource
	 *
	 * @see {@link https://www.w3.org/TR/owl-time/#time:intervalMeets section 4.2.30}
	 */
	public static final LinkRelation INTERVAL_MEETS = LinkRelation.of("intervalMeets");

	/**
	 * refers to a resource associated with a time interval whose beginning coincides with the end of the time interval
	 * associated with the context resource
	 *
	 * @see {@link https://www.w3.org/TR/owl-time/#time:intervalMetBy section 4.2.31}
	 */
	public static final LinkRelation INTERVAL_MET_BY = LinkRelation.of("intervalMetBy");

	/**
	 * refers to a resource associated with a time interval that begins before the beginning of the time interval
	 * associated with the context resource, and ends after the beginning of the time interval associated with the context
	 * resource
	 *
	 * @see {@link https://www.w3.org/TR/owl-time/#time:intervalOverlappedBy section 4.2.32}
	 */
	public static final LinkRelation INTERVAL_OVERLAPPED_BY = LinkRelation.of("intervalOverlappedBy");

	/**
	 * refers to a resource associated with a time interval that begins before the end of the time interval associated
	 * with the context resource, and ends after the end of the time interval associated with the context resource
	 *
	 * @see {@link https://www.w3.org/TR/owl-time/#time:intervalOverlaps section 4.2.33}
	 */
	public static final LinkRelation INTERVAL_OVERLAPS = LinkRelation.of("intervalOverlaps");

	/**
	 * refers to a resource associated with a time interval whose beginning coincides with the beginning of the time
	 * interval associated with the context resource, and ends before the end of the time interval associated with the
	 * context resource
	 *
	 * @see {@link https://www.w3.org/TR/owl-time/#time:intervalStartedBy section 4.2.34}
	 */
	public static final LinkRelation INTERVAL_STARTED_BY = LinkRelation.of("intervalStartedBy");

	/**
	 * refers to a resource associated with a time interval whose beginning coincides with the beginning of the time
	 * interval associated with the context resource, and ends after the end of the time interval associated with the
	 * context resource
	 *
	 * @see {@link https://www.w3.org/TR/owl-time/#time:intervalStarts section 4.2.35}
	 */
	public static final LinkRelation INTERVAL_STARTS = LinkRelation.of("intervalStarts");

	/**
	 * The target IRI points to a resource that is a member of the collection represented by the context IRI.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc6573}
	 */
	public static final LinkRelation ITEM = LinkRelation.of("item");

	/**
	 * An IRI that refers to the furthest following resource in a series of resources.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc8288}
	 */
	public static final LinkRelation LAST = LinkRelation.of("last");

	/**
	 * Points to a resource containing the latest (e.g., current) version of the context.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc5829}
	 */
	public static final LinkRelation LATEST_VERSION = LinkRelation.of("latest-version");

	/**
	 * Refers to a license associated with this context.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc4946}
	 */
	public static final LinkRelation LICENSE = LinkRelation.of("license");

	/**
	 * Refers to further information about the link's context, expressed as a LRDD ("Link-based Resource Descriptor
	 * Document") resource. See RFC6415 for information about processing this relation type in host-meta documents. When
	 * used elsewhere, it refers to additional links and other metadata. Multiple instances indicate additional LRDD
	 * resources. LRDD resources MUST have an "application/xrd+xml" representation, and MAY have others.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc6415}
	 */
	public static final LinkRelation LRDD = LinkRelation.of("lrdd");

	/**
	 * The Target IRI points to a Memento, a fixed resource that will not change state anymore.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc7089}
	 */
	public static final LinkRelation MEMENTO = LinkRelation.of("memento");

	/**
	 * Refers to a resource that can be used to monitor changes in an HTTP resource.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc5989}
	 */
	public static final LinkRelation MONITOR = LinkRelation.of("monitor");

	/**
	 * Refers to a resource that can be used to monitor changes in a specified group of HTTP resources.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc5989}
	 */
	public static final LinkRelation MONITOR_GROUP = LinkRelation.of("monitor-group");

	/**
	 * Indicates that the link's context is a part of a series, and that the next in the series is the link target.
	 *
	 * @see {@link https://www.w3.org/TR/html5/links.html#link-type-next}
	 */
	public static final LinkRelation NEXT = LinkRelation.of("next");

	/**
	 * Refers to the immediately following archive resource.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc5005}
	 */
	public static final LinkRelation NEXT_ARCHIVE = LinkRelation.of("next-archive");

	/**
	 * Indicates that the context‚Äôs original author or publisher does not endorse the link target.
	 *
	 * @see {@link https://www.w3.org/TR/html5/links.html#link-type-nofollow}
	 */
	public static final LinkRelation NOFOLLOW = LinkRelation.of("nofollow");

	/**
	 * Indicates that no referrer information is to be leaked when following the link.
	 *
	 * @see {@link https://www.w3.org/TR/html5/links.html#link-type-noreferrer}
	 */
	public static final LinkRelation NOREFERRER = LinkRelation.of("noreferrer");

	/**
	 * The Target IRI points to an Original Resource.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc7089}
	 */
	public static final LinkRelation ORIGINAL = LinkRelation.of("original");

	/**
	 * Indicates a resource where payment is accepted.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc8288}
	 */
	public static final LinkRelation PAYMENT = LinkRelation.of("payment");

	/**
	 * Gives the address of the pingback resource for the link context.
	 *
	 * @see {@link https://www.hixie.ch/specs/pingback/pingback}
	 */
	public static final LinkRelation PINGBACK = LinkRelation.of("pingback");

	/**
	 * Used to indicate an origin that will be used to fetch required resources for the link context. Initiating an early
	 * connection, which includes the DNS lookup, TCP handshake, and optional TLS negotiation, allows the user agent to
	 * mask the high latency costs of establishing a connection.
	 *
	 * @see {@link https://www.w3.org/TR/resource-hints/}
	 */
	public static final LinkRelation PRECONNECT = LinkRelation.of("preconnect");

	/**
	 * Points to a resource containing the predecessor version in the version history.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc5829}
	 */
	public static final LinkRelation PREDECESSOR_VERSION = LinkRelation.of("predecessor-version");

	/**
	 * The prefetch link relation type is used to identify a resource that might be required by the next navigation from
	 * the link context, and that the user agent ought to fetch, such that the user agent can deliver a faster response
	 * once the resource is requested in the future.
	 *
	 * @see {@link https://www.w3.org/TR/resource-hints/}
	 */
	public static final LinkRelation PREFETCH = LinkRelation.of("prefetch");

	/**
	 * Refers to a resource that should be loaded early in the processing of the link's context, without blocking
	 * rendering.
	 *
	 * @see {@link https://www.w3.org/TR/preload/}
	 */
	public static final LinkRelation PRELOAD = LinkRelation.of("preload");

	/**
	 * Used to identify a resource that might be required by the next navigation from the link context, and that the user
	 * agent ought to fetch and execute, such that the user agent can deliver a faster response once the resource is
	 * requested in the future.
	 *
	 * @see {@link https://www.w3.org/TR/resource-hints/}
	 */
	public static final LinkRelation PRERENDER = LinkRelation.of("prerender");

	/**
	 * Indicates that the link's context is a part of a series, and that the previous in the series is the link target.
	 *
	 * @see {@link https://www.w3.org/TR/html5/links.html#link-type-prev}
	 */
	public static final LinkRelation PREV = LinkRelation.of("prev");

	/**
	 * Refers to a resource that provides a preview of the link's context.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc6903, section 3}
	 */
	public static final LinkRelation PREVIEW = LinkRelation.of("preview");

	/**
	 * Refers to the previous resource in an ordered series of resources. Synonym for "prev".
	 *
	 * @see {@link https://www.w3.org/TR/1999/REC-html401-19991224}
	 */
	public static final LinkRelation PREVIOUS = LinkRelation.of("previous");

	/**
	 * Refers to the immediately preceding archive resource.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc5005}
	 */
	public static final LinkRelation PREV_ARCHIVE = LinkRelation.of("prev-archive");

	/**
	 * Refers to a privacy policy associated with the link's context.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc6903, section 4}
	 */
	public static final LinkRelation PRIVACY_POLICY = LinkRelation.of("privacy-policy");

	/**
	 * Identifying that a resource representation conforms to a certain profile, without affecting the non-profile
	 * semantics of the resource representation.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc6906}
	 */
	public static final LinkRelation PROFILE = LinkRelation.of("profile");

	/**
	 * Identifies a related resource.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc4287}
	 */
	public static final LinkRelation RELATED = LinkRelation.of("related");

	/**
	 * Identifies the root of RESTCONF API as configured on this HTTP server. The "restconf" relation defines the root of
	 * the API defined in RFC8040. Subsequent revisions of RESTCONF will use alternate relation values to support protocol
	 * versioning.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc8040}
	 */
	public static final LinkRelation RESTCONF = LinkRelation.of("restconf");

	/**
	 * Identifies a resource that is a reply to the context of the link.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc4685}
	 */
	public static final LinkRelation REPLIES = LinkRelation.of("replies");

	/**
	 * Refers to a resource that can be used to search through the link's context and related resources.
	 *
	 * @see {@link http://www.opensearch.org/Specifications/OpenSearch/1.1}
	 */
	public static final LinkRelation SEARCH = LinkRelation.of("search");

	/**
	 * Refers to a section in a collection of resources.
	 *
	 * @see {@link https://www.w3.org/TR/1999/REC-html401-19991224}
	 */
	public static final LinkRelation SECTION = LinkRelation.of("section");

	/**
	 * Conveys an identifier for the link's context.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc4287}
	 */
	public static final LinkRelation SELF = LinkRelation.of("self");

	/**
	 * Indicates a URI that can be used to retrieve a service document.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc5023}
	 */
	public static final LinkRelation SERVICE = LinkRelation.of("service");

	/**
	 * Refers to the first resource in a collection of resources.
	 *
	 * @see {@link https://www.w3.org/TR/1999/REC-html401-19991224}
	 */
	public static final LinkRelation START = LinkRelation.of("start");

	/**
	 * Refers to a stylesheet.
	 *
	 * @see {@link https://www.w3.org/TR/html5/links.html#link-type-stylesheet}
	 */
	public static final LinkRelation STYLESHEET = LinkRelation.of("stylesheet");

	/**
	 * Refers to a resource serving as a subsection in a collection of resources.
	 *
	 * @see {@link https://www.w3.org/TR/1999/REC-html401-19991224}
	 */
	public static final LinkRelation SUBSECTION = LinkRelation.of("subsection");

	/**
	 * Points to a resource containing the successor version in the version history.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc5829}
	 */
	public static final LinkRelation SUCCESSOR_VERSION = LinkRelation.of("successor-versions");

	/**
	 * Gives a tag (identified by the given address) that applies to the current document.
	 *
	 * @see {@link https://www.w3.org/TR/html5/links.html#link-type-tag}
	 */
	public static final LinkRelation TAG = LinkRelation.of("tag");

	/**
	 * Refers to the terms of service associated with the link's context.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc6903, section 5}
	 */
	public static final LinkRelation TERMS_OF_SERVICE = LinkRelation.of("terms-of-service");

	/**
	 * The Target IRI points to a TimeGate for an Original Resource.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc7089}
	 */
	public static final LinkRelation TIMEGATE = LinkRelation.of("timegate");

	/**
	 * The Target IRI points to a TimeMap for an Original Resource.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc7089}
	 */
	public static final LinkRelation TIMEMAP = LinkRelation.of("timemap");

	/**
	 * Refers to a resource identifying the abstract semantic type of which the link's context is considered to be an
	 * instance.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc6903, section 6}
	 */
	public static final LinkRelation TYPE = LinkRelation.of("type");

	/**
	 * Refers to a parent document in a hierarchy of documents.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc8288}
	 */
	public static final LinkRelation UP = LinkRelation.of("up");

	/**
	 * Points to a resource containing the version history for the context.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc5829}
	 */
	public static final LinkRelation VERSION_HISTORY = LinkRelation.of("version-history");

	/**
	 * Identifies a resource that is the source of the information in the link's context.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc4287}
	 */
	public static final LinkRelation VIA = LinkRelation.of("via");

	/**
	 * Identifies a target URI that supports the Webmention protcol. This allows clients that mention a resource in some
	 * form of publishing process to contact that endpoint and inform it that this resource has been mentioned.
	 *
	 * @see {@link https://www.w3.org/TR/webmention/}
	 */
	public static final LinkRelation WEBMENTION = LinkRelation.of("webmention");

	/**
	 * Points to a working copy for this resource.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc5829}
	 */
	public static final LinkRelation WORKING_COPY = LinkRelation.of("working-copy");

	/**
	 * Points to the versioned resource from which this working copy was obtained.
	 *
	 * @see {@link https://tools.ietf.org/html/rfc5829}
	 */
	public static final LinkRelation WORKING_COPY_OF = LinkRelation.of("working-copy-of");

	/**
	 * Consolidated collection of {@link IanaLinkRelations}s.
	 */
	private final Set<LinkRelation> LINK_RELATIONS;

	static {

		LINK_RELATIONS = Arrays.stream(IanaLinkRelations.class.getDeclaredFields()) //
				.filter(ReflectionUtils::isPublicStaticFinal) //
				.map(it -> ReflectionUtils.getField(it, null)) //
				.map(LinkRelation.class::cast) //
				.collect(Collectors.toSet());
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
