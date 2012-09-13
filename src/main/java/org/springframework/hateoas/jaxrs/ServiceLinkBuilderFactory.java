package org.springframework.hateoas.jaxrs;

import javax.ws.rs.Path;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.hateoas.LinkBuilderFactory;
import org.springframework.hateoas.UriComponentsLinkBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

/**
 * Factory for {@link UriComponentsLinkBuilder} instances based on the path mapping annotated on the given JAX-RS
 * service.
 * 
 * @author Ricardo Gladwell
 */
@Service
public class ServiceLinkBuilderFactory implements LinkBuilderFactory {

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilderFactory#linkTo(java.lang.Class)
	 */
	public LinkBuilder linkTo(Class<?> service) {
		return linkTo(service, new Object[0]);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilderFactory#linkTo(java.lang.Class, java.lang.Object[])
	 */
	@Override
    public LinkBuilder linkTo(Class<?> service, Object... parameters) {
	    Path annotation = AnnotationUtils.findAnnotation(service, Path.class);
		String path = (String) AnnotationUtils.getValue(annotation);

		LinkBuilder builder = new UriComponentsLinkBuilder(ServletUriComponentsBuilder.fromCurrentServletMapping());

		UriTemplate template = new UriTemplate(path);
		return builder.slash(template.expand(parameters));
    }

}
