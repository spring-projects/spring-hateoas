package de.escalon.hypermedia.action;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows to explicitly qualify a method handler as resource with defined cardinality.
 * Normally a Collection or a Resources return type (optionally wrapped into an HttpEntity)
 * or the presence of a POST method implicitly qualifies a resource a collection.
 * Created by Dietrich on 02.05.2015.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ResourceHandler {

	/**
	 * Allows to disambiguate if the annotated method handler manages a single or a collection resource.
	 * This can be helpful when there is a return type which doesn't allow to decide the cardinality
	 * of a resource, or when the default recognition comes to the wrong result.
	 * E.g. one can annotate a POST handler so that renderers can render the related resource as a single resource.
	 * <pre>
	 * &#64;ResourceHandler(Cardinality.SINGLE)
	 * &#64;RequestMapping(method=RequestMethod.POST)
	 * public ResponseEntity&lt;String&gt; myPostHandler() {}
	 * </pre>
	 *
	 * @return cardinality
	 */
	Cardinality value();
}
