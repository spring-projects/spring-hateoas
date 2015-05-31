package de.escalon.hypermedia.affordance;

import de.escalon.hypermedia.action.Type;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Interface to represent an input parameter to a resource handler method, independent of a particular ReST framework.
 * Created by Dietrich on 05.04.2015.
 */
public interface AnnotatedParameter {

    Object getCallValue();

    String getCallValueFormatted();

    Type getHtmlInputFieldType();

    boolean isRequestBody();

    boolean isRequestParam();

    boolean isPathVariable();

    String getRequestHeaderName();

    boolean hasInputConstraints();

    <T extends Annotation> T getAnnotation(Class<T> annotation);

    Object[] getPossibleValues(AnnotatedParameters annotatedParameters);

    Object[] getPossibleValues(Method method, int parameterIndex, AnnotatedParameters annotatedParameters);

    Object[] getPossibleValues(Constructor constructor, int parameterIndex, AnnotatedParameters annotatedParameters);

    boolean isArrayOrCollection();

    boolean isRequired();

    String getDefaultValue();

    Object[] getCallValues();

    boolean hasCallValue();

    String getParameterName();
    Class<?> getDeclaringClass();

    Class<?> getParameterType();

    java.lang.reflect.Type getGenericParameterType();

    Map<String, Object> getInputConstraints();
}
