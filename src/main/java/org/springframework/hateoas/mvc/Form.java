package org.springframework.hateoas.mvc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Retention(RetentionPolicy.RUNTIME)
public @interface Form {

    String value() default "";

}
