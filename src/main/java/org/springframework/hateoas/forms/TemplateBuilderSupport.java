package org.springframework.hateoas.forms;

import static org.springframework.web.util.UriComponentsBuilder.fromUri;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

import java.net.URI;
import java.util.List;

import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.core.LinkBuilderSupport;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Base class to implement {@link TemplateBuilder}s based on a Spring MVC {@link UriComponentsBuilder}.
 * 
 */
public abstract class TemplateBuilderSupport<T extends TemplateBuilder> implements TemplateBuilder {

	private final UriComponents uriComponents;

	/**
	 * Creates a new {@link LinkBuilderSupport} using the given {@link UriComponentsBuilder}.
	 * 
	 * @param builder must not be {@literal null}.
	 */
	public TemplateBuilderSupport(UriComponentsBuilder builder) {

		Assert.notNull(builder, "UriComponentsBuilder must not be null!");
		this.uriComponents = builder.build();
	}

	/**
	 * Creates a new {@link LinkBuilderSupport} using the given {@link UriComponents}.
	 * 
	 * @param uriComponents must not be {@literal null}.
	 */
	public TemplateBuilderSupport(UriComponents uriComponents) {

		Assert.notNull(uriComponents, "UriComponents must not be null!");
		this.uriComponents = uriComponents;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.hateoas.LinkBuilder#slash(java.lang.Object)
	 */
	public T slash(Object object) {

		if (object == null) {
			return getThis();
		}

		if (object instanceof Identifiable) {
			return slash((Identifiable<?>) object);
		}

		String path = object.toString();

		if (path.endsWith("#")) {
			path = path.substring(0, path.length() - 1);
		}

		if (!StringUtils.hasText(path)) {
			return getThis();
		}

		String uriString = uriComponents.toUriString();
		UriComponentsBuilder builder = uriString.isEmpty() ? fromUri(uriComponents.toUri()) : fromUriString(uriString);

		UriComponents components = UriComponentsBuilder.fromUriString(path).build();

		for (String pathSegment : components.getPathSegments()) {
			builder.pathSegment(pathSegment);
		}

		String fragment = components.getFragment();
		if (StringUtils.hasText(fragment)) {
			builder.fragment(fragment);
		}

		return createNewInstance(builder.query(components.getQuery()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.hateoas.LinkBuilder#slash(org.springframework.hateoas.Identifiable)
	 */
	public T slash(Identifiable<?> identifyable) {

		if (identifyable == null) {
			return getThis();
		}

		return slash(identifyable.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.hateoas.LinkBuilder#toUri()
	 */
	public URI toUri() {
		return uriComponents.encode().toUri();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return toUri().normalize().toASCIIString();
	}

	/**
	 * Returns the current concrete instance.
	 * 
	 * @return
	 */
	protected abstract T getThis();

	/**
	 * Creates a new instance of the sub-class.
	 * 
	 * @param builder will never be {@literal null}.
	 * @return
	 */
	protected abstract T createNewInstance(UriComponentsBuilder builder);

	@Override
	public Template withKey(String key) {
		Form form = new Form(toString(), key);
		form.setProperties(getProperties());
		form.setMethod(getMethod());
		return form;
	}

	@Override
	public Template withDefaultKey() {
		return withKey(Template.DEFAULT_KEY);
	}

	public abstract List<Property> getProperties();

	public abstract RequestMethod[] getMethod();

}
