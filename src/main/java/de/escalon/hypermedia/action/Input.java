/*
 * Copyright (c) 2014. Escalon System-Entwicklung, Dietrich Schulten
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the License.
 */

package de.escalon.hypermedia.action;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows to define the input characteristics for an input field. E.g. this is useful to specify possible value ranges
 * as in <code>&#64;Input(min=0)</code>, and it can also be used to mark a method parameter as
 * <code>&#64;Input(Type.HIDDEN)</code>, e.g. when used as a POST parameter for a form which is not supposed to be
 * changed by the client.
 *
 * @author Dietrich Schulten
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Input {

    /**
     * Input type. With the default type FROM_JAVA the type will be number or text, depending on the parameter type.
     *
     * @return input type
     */
    Type value() default Type.FROM_JAVA;

    int max() default Integer.MAX_VALUE;

    int min() default Integer.MIN_VALUE;

    int minLength() default Integer.MIN_VALUE;

    int maxLength() default Integer.MAX_VALUE;

    String pattern() default "";

    int step() default 0;

}
