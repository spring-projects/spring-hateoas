package org.springframework.hateoas;

import java.net.URI;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

public class UriComponentsLinkBuilder implements LinkBuilder {

	private UriComponents uriComponents;

	public UriComponentsLinkBuilder(UriComponentsBuilder builder) {
	    super();
		Assert.notNull(builder);
	    this.uriComponents = builder.build();
    }

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilder#slash(java.lang.Object)
	 */
	public UriComponentsLinkBuilder slash(Object object) {

		if (object == null) {
			return this;
		}

		String[] segments = StringUtils.tokenizeToStringArray(object.toString(), "/");
		return new UriComponentsLinkBuilder(UriComponentsBuilder.fromUri(uriComponents.toUri()).pathSegment(segments));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilder#slash(org.springframework.hateoas.Identifiable)
	 */
	public UriComponentsLinkBuilder slash(Identifiable<?> identifyable) {

		if (identifyable == null) {
			return this;
		}

		return slash(identifyable.getId());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilder#toUri()
	 */
	public URI toUri() {
		return uriComponents.encode().toUri();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilder#withRel(java.lang.String)
	 */
	public Link withRel(String rel) {
		return new Link(this.toString(), rel);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilder#withSelfRel()
	 */
	public Link withSelfRel() {
		return new Link(this.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return toUri().normalize().toASCIIString();
	}

}
