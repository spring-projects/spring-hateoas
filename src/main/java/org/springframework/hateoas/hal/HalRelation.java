package org.springframework.hateoas.hal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface HalRelation {

	public static final String NO_RELATION = "";

	String value() default NO_RELATION;

	String collectionRelation() default NO_RELATION;

}