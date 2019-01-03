/*
 * Copyright 2019 the original author or authors.
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
package org.springframework.hateoas;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Capture standard IANA-based link relations.
 *
 * @see https://www.iana.org/assignments/link-relations/link-relations.xhtml
 * @see https://tools.ietf.org/html/rfc8288
 * @see https://github.com/link-relations/registry
 * 
 * @author Greg Turnquist
 * @author Roland Kulcsár
 * @since 1.0
 */
public enum IanaLinkRelation implements LinkRelation {

	/**
	 * Refers to a resource that is the subject of the link's context.
	 *
	 * @see https://tools.ietf.org/html/rfc6903
	 */
	ABOUT("about"),

	/**
	 * Refers to a substitute for this context
	 *
	 * @see http://www.w3.org/TR/html5/links.html#link-type-alternate
	 */
	ALTERNATE("alternate"),

	/**
	 * Refers to an appendix.
	 * 
	 * @see http://www.w3.org/TR/1999/REC-html401-19991224
	 */
	APPENDIX("appendix"),

	/**
	 * Refers to a collection of records, documents, or other materials of historical interest.
	 *
	 * @see http://www.w3.org/TR/2011/WD-html5-20110113/links.html#rel-archives
	 */
	ARCHIVES("archives"),

	/**
	 * Refers to the context's author.
	 *
	 * @see http://www.w3.org/TR/html5/links.html#link-type-author
	 */
	AUTHOR("author"),

	/**
	 * Identifies the entity that blocks access to a resource following receipt of a legal demand.
	 * 
	 * @see https://tools.ietf.org/html/rfc7725
	 */
	BLOCKED_BY("blocked-by"),

	/**
	 * Gives a permanent link to use for bookmarking purposes.
	 *
	 * @see http://www.w3.org/TR/html5/links.html#link-type-bookmark
	 */
	BOOKMARK("bookmark"),

	/**
	 * Designates the preferred version of a resource (the IRI and its contents).
	 *
	 * @see https://tools.ietf.org/html/rfc6596
	 */
	CANONICAL("canonical"),

	/**
	 * Refers to a chapter in a collection of resources.
	 *
	 * @see http://www.w3.org/TR/1999/REC-html401-19991224
	 */
	CHAPTER("chapter"),

	/**
	 * Indicates that the link target is preferred over the link context for the purpose of referencing.
	 *
	 * @see https://datatracker.ietf.org/doc/draft-vandesompel-citeas/
	 */
	CITE_AS("cite-as"),

	/**
	 * The target IRI points to a resource which represents the collection resource for the context IRI.
	 *
	 * @see https://tools.ietf.org/html/rfc6573
	 */
	COLLECTION("collection"),

	/**
	 * Refers to a table of contents.
	 * @see http://www.w3.org/TR/1999/REC-html401-19991224
	 */
	CONTENTS("contents"),

	/**
	 * The document linked to was later converted to the document that contains this link relation.
	 * For example, an RFC can have a link to the Internet-Draft that became the RFC; in that case,
	 * the link relation would be "convertedFrom".
	 * 
	 * @see https://tools.ietf.org/html/rfc7991
	 */
	CONVERTED_FROM("convertedFrom"),

	/**
	 * Refers to a copyright statement that applies to the link's context.
	 *
	 * @see http://www.w3.org/TR/1999/REC-html401-19991224
	 */
	COPYRIGHT("copyright"),

	/**
	 * The target IRI points to a resource where a submission form can be obtained.
	 *
	 * @see https://tools.ietf.org/html/rfc6861
	 */
	CREATE_FORM("create-form"),

	/**
	 * Refers to a resource containing the most recent item(s) in a collection of resources.
	 *
	 * @see https://tools.ietf.org/html/rfc5005
	 */
	CURRENT("current"),

	/**
	 * Refers to a resource providing information about the link's context.
	 *
	 * @see http://www.w3.org/TR/powder-dr/#assoc-linking
	 */
	DESCRIBED_BY("describedBy"),

	/**
	 * The relationship A 'describes' B asserts that resource A provides a description of resource B. There are no
	 * constraints on the format or representation of either A or B, neither are there any further constraints on
	 * either resource.
	 *
	 * @see https://tools.ietf.org/html/rfc6892
	 */
	DESCRIBES("describes"),

	/**
	 * Refers to a list of patent disclosures made with respect to material for which 'disclosure' relation is
	 * specified.
	 *
	 * @see https://tools.ietf.org/html/rfc6579
	 */
	DISCLOSURE("disclosure"),

	/**
	 * Used to indicate an origin that will be used to fetch required resources for the link context, and that the
	 * user agent ought to resolve as early as possible.
	 *
	 * @see https://www.w3.org/TR/resource-hints/
	 */
	DNS_PREFETCH("dns-prefetch"),

	/**
	 * Refers to a resource whose available representations are byte-for-byte identical with the corresponding
	 * representations of the context IRI.
	 *
	 * @see https://tools.ietf.org/html/rfc6249
	 */
	DUPLICATE("duplicate"),

	/**
	 * Refers to a resource that can be used to edit the link's context.
	 *
	 * @see https://tools.ietf.org/html/rfc5023
	 */
	EDIT("edit"),

	/**
	 * The target IRI points to a resource where a submission form for editing associated resource can be obtained.
	 *
	 * @see https://tools.ietf.org/html/rfc6861
	 */
	EDIT_FORM("edit-form"),

	/**
	 * Refers to a resource that can be used to edit media associated with the link's context.
	 *
	 * @see https://tools.ietf.org/html/rfc5023
	 */
	EDIT_MEDIA("edit-media"),

	/**
	 * Identifies a related resource that is potentially large and might require special handling.
	 *
	 * @see https://tools.ietf.org/html/rfc4287
	 */
	ENCLOSURE("enclosure"),

	/**
	 * An IRI that refers to the furthest preceding resource in a series of resources.
	 *
	 * @see https://tools.ietf.org/html/rfc8288
	 */
	FIRST("first"),

	/**
	 * Refers to a glossary of terms.
	 *
	 * @see http://www.w3.org/TR/1999/REC-html401-19991224
	 */
	GLOSSARY("glossary"),

	/**
	 * Refers to context-sensitive help.
	 *
	 * @see http://www.w3.org/TR/html5/links.html#link-type-help
	 */
	HELP("help"),

	/**
	 * Refers to a resource hosted by the server indicated by the link context.
	 *
	 * @see https://tools.ietf.org/html/rfc6690
	 */
	HOSTS("hosts"),

	/**
	 * Refers to a hub that enables registration for notification of updates to the context.
	 *
	 * @see http://pubsubhubbub.googlecode.com
	 */
	HUB("hub"),

	/**
	 * Refers to an icon representing the link's context.
	 *
	 * @see http://www.w3.org/TR/html5/links.html#link-type-icon
	 */
	ICON("icon"),

	/**
	 * Refers to an index.
	 *
	 * @see http://www.w3.org/TR/1999/REC-html401-19991224
	 */
	INDEX("index"),

	/**
	 * refers to a resource associated with a time interval that ends before the beginning of the time interval
	 * associated with the context resource
	 *
	 * @see https://www.w3.org/TR/owl-time/#time:intervalAfter section 4.2.21
	 */
	INTERVAL_AFTER("intervalAfter"),

	/**
	 * refers to a resource associated with a time interval that begins after the end of the time interval associated
	 * with the context resource
	 *
	 * @see https://www.w3.org/TR/owl-time/#time:intervalBefore section 4.2.22
	 */
	INTERVAL_BEFORE("intervalBefore"),

	/**
	 * refers to a resource associated with a time interval that begins after the beginning of the time interval
	 * associated with the context resource, and ends before the end of the time interval associated with the context
	 * resource
	 *
	 * @see https://www.w3.org/TR/owl-time/#time:intervalContains section 4.2.23
	 */
	INTERVAL_CONTAINS("intervalContains"),

	/**
	 * refers to a resource associated with a time interval that begins after the end of the time interval associated
	 * with the context resource, or ends before the beginning of the time interval associated with the context resource
	 *
	 * @see https://www.w3.org/TR/owl-time/#time:intervalDisjoint section 4.2.24
	 */
	INTERVAL_DISJOINT("intervalDisjoint"),

	/**
	 * refers to a resource associated with a time interval that begins before the beginning of the time interval
	 * associated with the context resource, and ends after the end of the time interval associated with the context
	 * resource
	 *
	 * @see https://www.w3.org/TR/owl-time/#time:intervalDuring section 4.2.25
	 */
	INTERVAL_DURING("intervalDuring"),

	/**
	 * refers to a resource associated with a time interval whose beginning coincides with the beginning of the time
	 * interval associated with the context resource, and whose end coincides with the end of the time interval
	 * associated with the context resource
	 *
	 * @see https://www.w3.org/TR/owl-time/#time:intervalEquals section 4.2.26
	 */
	INTERVAL_EQUALS("intervalEquals"),

	/**
	 * refers to a resource associated with a time interval that begins after the beginning of the time interval
	 * associated with the context resource, and whose end coincides with the end of the time interval associated with
	 * the context resource
	 *
	 * @see https://www.w3.org/TR/owl-time/#time:intervalFinishedBy section 4.2.27
	 */
	INTERVAL_FINISHED_BY("intervalFinishedBy"),

	/**
	 * refers to a resource associated with a time interval that begins before the beginning of the time interval
	 * associated with the context resource, and whose end coincides with the end of the time interval associated with
	 * the context resource
	 *
	 * @see https://www.w3.org/TR/owl-time/#time:intervalFinishes section 4.2.28
	 */
	INTERVAL_FINISHES("intervalFinishes"),

	/**
	 * refers to a resource associated with a time interval that begins before or is coincident with the beginning
	 * of the time interval associated with the context resource, and ends after or is coincident with the end of the
	 * time interval associated with the context resource
	 *
	 * @see https://www.w3.org/TR/owl-time/#time:intervalIn section 4.2.29
	 */
	INTERVAL_IN("intervalIn"),

	/**
	 * refers to a resource associated with a time interval whose beginning coincides with the end of the time
	 * interval associated with the context resource
	 *
	 * @see https://www.w3.org/TR/owl-time/#time:intervalMeets section 4.2.30
	 */
	INTERVAL_MEETS("intervalMeets"),

	/**
	 * refers to a resource associated with a time interval whose beginning coincides with the end of the time
	 * interval associated with the context resource
	 *
	 * @see https://www.w3.org/TR/owl-time/#time:intervalMetBy section 4.2.31
	 */
	INTERVAL_MET_BY("intervalMetBy"),

	/**
	 * refers to a resource associated with a time interval that begins before the beginning of the time interval
	 * associated with the context resource, and ends after the beginning of the time interval associated with the
	 * context resource
	 *
	 * @see https://www.w3.org/TR/owl-time/#time:intervalOverlappedBy section 4.2.32
	 */
	INTERVAL_OVERLAPPED_BY("intervalOverlappedBy"),

	/**
	 * refers to a resource associated with a time interval that begins before the end of the time interval associated
	 * with the context resource, and ends after the end of the time interval associated with the context resource
	 *
	 * @see https://www.w3.org/TR/owl-time/#time:intervalOverlaps section 4.2.33
	 */
	INTERVAL_OVERLAPS("intervalOverlaps"),

	/**
	 * refers to a resource associated with a time interval whose beginning coincides with the beginning of the time
	 * interval associated with the context resource, and ends before the end of the time interval associated with
	 * the context resource
	 *
	 * @see https://www.w3.org/TR/owl-time/#time:intervalStartedBy section 4.2.34
	 */
	INTERVAL_STARTED_BY("intervalStartedBy"),

	/**
	 * refers to a resource associated with a time interval whose beginning coincides with the beginning of the time
	 * interval associated with the context resource, and ends after the end of the time interval associated with the
	 * context resource
	 *
	 * @see https://www.w3.org/TR/owl-time/#time:intervalStarts section 4.2.35
	 */
	INTERVAL_STARTS("intervalStarts"),

	/**
	 * The target IRI points to a resource that is a member of the collection represented by the context IRI.
	 *
	 * @see https://tools.ietf.org/html/rfc6573
	 */
	ITEM("item"),

	/**
	 * An IRI that refers to the furthest following resource in a series of resources.
	 *
	 * @see https://tools.ietf.org/html/rfc8288
	 */
	LAST("last"),

	/**
	 * Points to a resource containing the latest (e.g., current) version of the context.
	 *
	 * @see https://tools.ietf.org/html/rfc5829
	 */
	LATEST_VERSION("latest-version"),

	/**
	 * Refers to a license associated with this context.
	 *
	 * @see https://tools.ietf.org/html/rfc4946
	 */
	LICENSE("license"),

	/**
	 * Refers to further information about the link's context, expressed as a LRDD ("Link-based Resource Descriptor
	 * Document") resource.  See RFC6415 for information about processing this relation type in host-meta documents.
	 * When used elsewhere, it refers to additional links and other metadata. Multiple instances indicate additional
	 * LRDD resources. LRDD resources MUST have an "application/xrd+xml" representation, and MAY have others.
	 *
	 * @see https://tools.ietf.org/html/rfc6415
	 */
	LRDD("lrdd"),

	/**
	 * The Target IRI points to a Memento, a fixed resource that will not change state anymore.
	 *
	 * @see https://tools.ietf.org/html/rfc7089
	 */
	MEMENTO("memento"),

	/**
	 * Refers to a resource that can be used to monitor changes in an HTTP resource.
	 *
	 * @see https://tools.ietf.org/html/rfc5989
	 */
	MONITOR("monitor"),

	/**
	 * Refers to a resource that can be used to monitor changes in a specified group of HTTP resources.
	 *
	 * @see https://tools.ietf.org/html/rfc5989
	 */
	MONITOR_GROUP("monitor-group"),

	/**
	 * Indicates that the link's context is a part of a series, and that the next in the series is the link target.
	 *
	 * @see http://www.w3.org/TR/html5/links.html#link-type-next
	 */
	NEXT("next"),

	/**
	 * Refers to the immediately following archive resource.
	 *
	 * @see https://tools.ietf.org/html/rfc5005
	 */
	NEXT_ARCHIVE("next-archive"),

	/**
	 * Indicates that the context‚Äôs original author or publisher does not endorse the link target.
	 *
	 * @see http://www.w3.org/TR/html5/links.html#link-type-nofollow
	 */
	NOFOLLOW("nofollow"),

	/**
	 * Indicates that no referrer information is to be leaked when following the link.
	 *
	 * @see http://www.w3.org/TR/html5/links.html#link-type-noreferrer
	 */
	NOREFERRER("noreferrer"),

	/**
	 * The Target IRI points to an Original Resource.
	 *
	 * @see https://tools.ietf.org/html/rfc7089
	 */
	ORIGINAL("original"),

	/**
	 * Indicates a resource where payment is accepted.
	 *
	 * @see https://tools.ietf.org/html/rfc8288
	 */
	PAYMENT("payment"),

	/**
	 * Gives the address of the pingback resource for the link context.
	 *
	 * @see http://www.hixie.ch/specs/pingback/pingback
	 */
	PINGBACK("pingback"),

	/**
	 * Used to indicate an origin that will be used to fetch required resources for the link context. Initiating an
	 * early connection, which includes the DNS lookup, TCP handshake, and optional TLS negotiation, allows the user
	 * agent to mask the high latency costs of establishing a connection.
	 *
	 * @see https://www.w3.org/TR/resource-hints/
	 */
	PRECONNECT("preconnect"),

	/**
	 * Points to a resource containing the predecessor version in the version history.
	 *
	 * @see https://tools.ietf.org/html/rfc5829
	 */
	PREDECESSOR_VERSION("predecessor-version"),

	/**
	 * The prefetch link relation type is used to identify a resource that might be required by the next navigation
	 * from the link context, and that the user agent ought to fetch, such that the user agent can deliver a faster
	 * response once the resource is requested in the future.
	 * 
	 * @see http://www.w3.org/TR/resource-hints/
	 */
	PREFETCH("prefetch"),

	/**
	 * Refers to a resource that should be loaded early in the processing of the link's context, without blocking
	 * rendering.
	 *
	 * @see http://www.w3.org/TR/preload/
	 */
	PRELOAD("preload"),

	/**
	 * Used to identify a resource that might be required by the next navigation from the link context, and that the
	 * user agent ought to fetch and execute, such that the user agent can deliver a faster response once the resource
	 * is requested in the future.
	 *
	 * @see https://www.w3.org/TR/resource-hints/
	 */
	PRERENDER("prerender"),

	/**
	 * Indicates that the link's context is a part of a series, and that the previous in the series is the link target.
	 *
	 * @see http://www.w3.org/TR/html5/links.html#link-type-prev
	 */
	PREV("prev"),

	/**
	 * Refers to a resource that provides a preview of the link's context.
	 *
	 * @see https://tools.ietf.org/html/rfc6903, section 3
	 */
	PREVIEW("preview"),

	/**
	 * Refers to the previous resource in an ordered series of resources.  Synonym for "prev".
	 *
	 * @see http://www.w3.org/TR/1999/REC-html401-19991224
	 */
	PREVIOUS("previous"),

	/**
	 * Refers to the immediately preceding archive resource.
	 *
	 * @see https://tools.ietf.org/html/rfc5005
	 */
	PREV_ARCHIVE("prev-archive"),

	/**
	 * Refers to a privacy policy associated with the link's context.
	 *
	 * @see https://tools.ietf.org/html/rfc6903, section 4
	 */
	PRIVACY_POLICY("privacy-policy"),

	/**
	 * Identifying that a resource representation conforms to a certain profile, without affecting the non-profile
	 * semantics of the resource representation.
	 *
	 * @see https://tools.ietf.org/html/rfc6906
	 */
	PROFILE("profile"),

	/**
	 * Identifies a related resource.
	 *
	 * @see https://tools.ietf.org/html/rfc4287
	 */
	RELATED("related"),

	/**
	 * Identifies the root of RESTCONF API as configured on this HTTP server. The "restconf" relation defines the
	 * root of the API defined in RFC8040. Subsequent revisions of RESTCONF will use alternate relation values to
	 * support protocol versioning.
	 *
	 * @see https://tools.ietf.org/html/rfc8040
	 */
	RESTCONF("restconf"),

	/**
	 * Identifies a resource that is a reply to the context of the link.
	 *
	 * @see https://tools.ietf.org/html/rfc4685
	 */
	REPLIES("replies"),

	/**
	 * Refers to a resource that can be used to search through the link's context and related resources.
	 *
	 * @see http://www.opensearch.org/Specifications/OpenSearch/1.1
	 */
	SEARCH("search"),

	/**
	 * Refers to a section in a collection of resources.
	 *
	 * @see http://www.w3.org/TR/1999/REC-html401-19991224
	 */
	SECTION("section"),

	/**
	 * Conveys an identifier for the link's context.
	 *
	 * @see https://tools.ietf.org/html/rfc4287
	 */
	SELF("self"),

	/**
	 * Indicates a URI that can be used to retrieve a service document.
	 *
	 * @see https://tools.ietf.org/html/rfc5023
	 */
	SERVICE("service"),

	/**
	 * Refers to the first resource in a collection of resources.
	 *
	 * @see http://www.w3.org/TR/1999/REC-html401-19991224
	 */
	START("start"),

	/**
	 * Refers to a stylesheet.
	 *
	 * @see http://www.w3.org/TR/html5/links.html#link-type-stylesheet
	 */
	STYLESHEET("stylesheet"),

	/**
	 * Refers to a resource serving as a subsection in a collection of resources.
	 *
	 * @see http://www.w3.org/TR/1999/REC-html401-19991224
	 */
	SUBSECTION("subsection"),

	/**
	 * Points to a resource containing the successor version in the version history.
	 *
	 * @see https://tools.ietf.org/html/rfc5829
	 */
	SUCCESSOR_VERSION("successor-versions"),

	/**
	 * Gives a tag (identified by the given address) that applies to the current document.
	 *
	 * @see http://www.w3.org/TR/html5/links.html#link-type-tag
	 */
	TAG("tag"),

	/**
	 * Refers to the terms of service associated with the link's context.
	 *
	 * @see https://tools.ietf.org/html/rfc6903, section 5
	 */
	TERMS_OF_SERVICE("terms-of-service"),

	/**
	 * The Target IRI points to a TimeGate for an Original Resource.
	 *
	 * @see https://tools.ietf.org/html/rfc7089
	 */
	TIMEGATE("timegate"),

	/**
	 * The Target IRI points to a TimeMap for an Original Resource.
	 *
	 * @see https://tools.ietf.org/html/rfc7089
	 */
	TIMEMAP("timemap"),

	/**
	 * Refers to a resource identifying the abstract semantic type of which the link's context is considered to be
	 * an instance.
	 *
	 * @see https://tools.ietf.org/html/rfc6903, section 6
	 */
	TYPE("type"),

	/**
	 * Refers to a parent document in a hierarchy of documents.
	 *
	 * @see https://tools.ietf.org/html/rfc8288
	 */
	UP("up"),

	/**
	 * Points to a resource containing the version history for the context.
	 *
	 * @see https://tools.ietf.org/html/rfc5829
	 */
	VERSION_HISTORY("version-history"),

	/**
	 * Identifies a resource that is the source of the information in the link's context.
	 *
	 * @see https://tools.ietf.org/html/rfc4287
	 */
	VIA("via"),

	/**
	 * Identifies a target URI that supports the Webmention protcol. This allows clients that mention a resource in
	 * some form of publishing process to contact that endpoint and inform it that this resource has been mentioned.
	 *
	 * @see http://www.w3.org/TR/webmention/
	 */
	WEBMENTION("webmention"),

	/**
	 * Points to a working copy for this resource.
	 * @see https://tools.ietf.org/html/rfc5829
	 */
	WORKING_COPY("working-copy"),

	/**
	 * Points to the versioned resource from which this working copy was obtained.
	 * 
	 * @see https://tools.ietf.org/html/rfc5829
	 */
	WORKING_COPY_OF("working-copy-of");
	
	/**
	 * Actual IANA value for the link relation.
	 */
	private final String value;

	/**
	 * Initialize the enum value.
	 *
	 * @param value
	 */
	IanaLinkRelation(String value) {
		this.value = value;
	}

	/**
	 * Return the IANA value.
	 */
	@Override
	public String value() {
		return this.value;
	}

	/**
	 * Consolidated collection of {@link IanaLinkRelation}s.
	 */
	public static final Set<IanaLinkRelation> LINK_RELATIONS;

	/**
	 * Consolidated collection of {@link IanaLinkRelation} values.
	 */
	public static final Set<String> RELS;

	static {

		LINK_RELATIONS = Arrays.stream(IanaLinkRelation.values())
			.collect(Collectors.toSet());

		RELS = LINK_RELATIONS.stream()
			.map(LinkRelation::value)
			.collect(Collectors.toSet());
	}

	/**
	 * Is this relation an IANA standard?
	 *
	 * Per RFC8288, parsing of link relations is case insensitive.
	 * 
	 * @param rel
	 * @return boolean
	 */
	public static boolean isIanaRel(String rel) {

		return
			rel != null
			&&
			LINK_RELATIONS.stream().anyMatch(linkRelation -> linkRelation.value().equalsIgnoreCase(rel));
	}

	/**
	 * Is this relation an IANA standard?
	 *
	 * Per RFC8288, parsing of link relations is case insensitive.
	 *
	 * @param rel
	 * @return
	 */
	public static boolean isIanaRel(LinkRelation rel) {
		
		return
			rel != null
			&&
			LINK_RELATIONS.stream().anyMatch(linkRelation -> linkRelation.value.equalsIgnoreCase(rel.value()));

	}

	/**
	 * Convert a string-based link relation to a {@link IanaLinkRelation}.
	 *
	 * Per RFC8288, parsing of link relations is case insensitive.
	 *
	 * @param rel as a string
	 * @return rel as a {@link IanaLinkRelation}
	 */
	public static IanaLinkRelation parse(String rel) {

		return LINK_RELATIONS.stream()
			.filter(linkRelation -> linkRelation.value().equalsIgnoreCase(rel))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException(rel + " is not a valid IANA link relation!"));
	}
}
