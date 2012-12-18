package org.springframework.hateoas.util;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * LinkTemplate to represent a resource on a controller which must be represented by a URI template and not a dereferencable URI.
 *
 * @author Dietrich Schulten
 *
 * @param <P> Annotation for path parameters, e.g. PathVariable or PathParam
 */
public class LinkTemplate<P extends Annotation, R extends Annotation> {

	private String linkTemplate;
	private List<AnnotatedParam<P>> pathVariables = new ArrayList<AnnotatedParam<P>>();
	private List<AnnotatedParam<R>> requestParams = new ArrayList<AnnotatedParam<R>>();

	public LinkTemplate(String linkTemplate, List<AnnotatedParam<P>> pathVariables, List<AnnotatedParam<R>> requestParams) {
		this.linkTemplate = linkTemplate;
		this.pathVariables = pathVariables;
		this.requestParams = requestParams;
	}

	public String getLinkTemplate() {
		return linkTemplate;
	}

	public List<AnnotatedParam<P>> getPathVariables() {
		return pathVariables;
	}

	public List<AnnotatedParam<R>> getRequestParams() {
		return requestParams;
	}

	@Override
	public String toString() {
		return "LinkTemplate [linkTemplate=" + linkTemplate + ", pathVariables=" + pathVariables + ", requestParams="
				+ requestParams + "]";
	}

}
