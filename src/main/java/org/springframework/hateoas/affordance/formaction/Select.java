/*
 * Copyright 2013-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.hateoas.affordance.formaction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.hateoas.affordance.SuggestType;

/**
 * Specifies possible values for an argument on a controller method.
 *
 * @author Dietrich Schulten
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Select {

	/**
	 * Allows to pass String arguments to the Options implementation. By default, a String array can be used to define
	 * possible values, since the default Options implementation is {@link StringOptions}
	 *
	 * @return arguments to the Options implementation. For the default {@link StringOptions}, an array of possible
	 *         values.
	 */
	String[] value() default {};

	/**
	 * Specifies an implementation of the {@link Options} interface which provides possible values.
	 *
	 * @return implementation class of {@link Options}
	 */
	Class<? extends Options<?>> options() default StringOptions.class;

	/**
	 * When getting possible values using {@link Options#get}, pass the arguments having these names.
	 *
	 * @return names of the arguments whose value should be passed to {@link Options#get}
	 */
	String[] args() default {};

	/**
	 * Marks the type of select, in case of {@link SuggestType#EXTERNAL} the data may be outside the select, for example
	 * as a variable in HAL response rather than in HAL-FORMS document
	 * 
	 * @return
	 */
	SuggestType type() default SuggestType.INTERNAL;

	/**
	 * Establish the property as required
	 * 
	 * @return
	 */
	boolean required() default false;

	/**
	 * Entire parameter is not editable, refers both to single values and to all properties of a bean parameter.
	 *
	 * @return
	 */
	boolean editable() default true;

}
