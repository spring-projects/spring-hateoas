package org.springframework.hateoas.mvc;

import org.springframework.hateoas.LinkBuilder;
import org.springframework.hateoas.LinkBuilderFactory;

/**
 * Factory for {@link ControllerLinkBuilder} instances.
 * 
 * @author Ricardo Gladwell
 */
public class ControllerLinkBuilderFactory implements LinkBuilderFactory {

	@Override
	public LinkBuilder linkTo(Class<?> controller) {
		return ControllerLinkBuilder.linkTo(controller);
	}

	@Override
	public LinkBuilder linkTo(Class<?> controller, Object... parameters) {
		return ControllerLinkBuilder.linkTo(controller, parameters);
	}

}
