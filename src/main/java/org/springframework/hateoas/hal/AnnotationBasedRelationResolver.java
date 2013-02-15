package org.springframework.hateoas.hal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.hateoas.Resource;

public class AnnotationBasedRelationResolver implements RelationResolver {

	@SuppressWarnings("rawtypes")
	@Override
	public String getResourceRelation(Object resource) {

		// check for hateoas wrapper type
		if (Resource.class.isInstance(resource)) {
			resource = ((Resource) resource).getContent();
		}

		HateoasRelation annotation = resource.getClass().getAnnotation(HateoasRelation.class);
		if (annotation == null || HateoasRelation.NO_RELATION.equals(annotation.value())) {
			return null;
		}
		return annotation.value();
	}

	@Target({ ElementType.TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface HateoasRelation {

		public static final String NO_RELATION = "";

		String value() default NO_RELATION;

	}

}
