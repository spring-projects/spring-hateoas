package org.springframework.hateoas;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.util.MethodAnnotationUtils.AnnotatedParam;

/**
 * LinkTemplate to represent all resources on a controller. The resources on a controller may not only be links,
 * but a mix of link templates and links.
 *
 * @author qqybk2l
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
		return "LinkTemplate [linkTemplate=" + linkTemplate + ", pathVariables=" + pathVariables + "]";
	}



}
