package org.springframework.hateoas.server.core;

import org.springframework.hateoas.TemplateVariables;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Réda Housni Alaoui
 */
public interface AdditionalUriHandler {

	UriComponentsBuilder apply(UriComponentsBuilder uriComponentsBuilder, MethodInvocation methodInvocation);

	TemplateVariables apply(TemplateVariables templateVariables, UriComponents uriComponents, MethodInvocation methodInvocation);
}
