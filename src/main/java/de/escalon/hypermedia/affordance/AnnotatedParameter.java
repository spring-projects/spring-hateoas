package de.escalon.hypermedia.affordance;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Interface to represent an input parameter to a resource handler method, independent of a particular ReST framework.
 * Created by Dietrich on 05.04.2015.
 */
public interface AnnotatedParameter {
    String MIN = "min";
    String MAX = "max";
    String STEP = "step";
    String MIN_LENGTH = "minLength";
    String MAX_LENGTH = "maxLength";
    String PATTERN = "pattern";

    Object getCallValue();

    String getCallValueFormatted();

    String getHtmlInputFieldType();

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
