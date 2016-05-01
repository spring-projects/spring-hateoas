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

package de.escalon.hypermedia.spring;

import de.escalon.hypermedia.action.Input;
import de.escalon.hypermedia.action.Options;
import de.escalon.hypermedia.action.Select;
import de.escalon.hypermedia.action.Type;
import de.escalon.hypermedia.affordance.AnnotatedParameter;
import de.escalon.hypermedia.affordance.AnnotatedParameters;
import de.escalon.hypermedia.affordance.DataType;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Describes a Spring MVC rest services method parameter value with recorded sample call value and input constraints.
 *
 * @author Dietrich Schulten
 */
public class ActionInputParameter implements AnnotatedParameter {

    private final TypeDescriptor typeDescriptor;
    private final RequestBody requestBody;
    private final RequestParam requestParam;
    private final PathVariable pathVariable;
    private final RequestHeader requestHeader;
    private Input inputAnnotation;
    private MethodParameter methodParameter;
    private Object value;
    private Boolean arrayOrCollection = null;
    private Map<String, Object> inputConstraints = new HashMap<String, Object>();

    private ConversionService conversionService = new DefaultFormattingConversionService();

    /**
     * Creates action input parameter.
     *
     * @param methodParameter
     *         to describe
     * @param value
     *         used during sample invocation
     * @param conversionService
     *         to apply to value
     */
    public ActionInputParameter(MethodParameter methodParameter, Object value, ConversionService conversionService) {
        this.methodParameter = methodParameter;
        this.value = value;
        this.requestBody = methodParameter.getParameterAnnotation(RequestBody.class);
        this.requestParam = methodParameter.getParameterAnnotation(RequestParam.class);
        this.pathVariable = methodParameter.getParameterAnnotation(PathVariable.class);
        this.requestHeader = methodParameter.getParameterAnnotation(RequestHeader.class);
        // always determine input constraints,
        // might be a nested property which is neither requestBody, requestParam nor pathVariable
        this.inputAnnotation = methodParameter.getParameterAnnotation(Input.class);
        if (inputAnnotation != null) {
            putInputConstraint(Input.MIN, Integer.MIN_VALUE, inputAnnotation.min());
            putInputConstraint(Input.MAX, Integer.MAX_VALUE, inputAnnotation.max());
            putInputConstraint(Input.MIN_LENGTH, Integer.MIN_VALUE, inputAnnotation.minLength());
            putInputConstraint(Input.MAX_LENGTH, Integer.MAX_VALUE, inputAnnotation.maxLength());
            putInputConstraint(Input.STEP, 0, inputAnnotation.step());
            putInputConstraint(Input.PATTERN, "", inputAnnotation.pattern());
        }
        this.conversionService = conversionService;
        this.typeDescriptor = TypeDescriptor.nested(methodParameter, 0);
    }

    /**
     * Creates new ActionInputParameter with default formatting conversion service.
     *
     * @param methodParameter
     *         holding metadata about the parameter
     * @param value
     *         during sample method invocation
     */
    public ActionInputParameter(MethodParameter methodParameter, Object value) {
        this(methodParameter, value, new DefaultFormattingConversionService());
    }


    private void putInputConstraint(String key, Object defaultValue, Object value) {
        if (!value.equals(defaultValue)) {
            inputConstraints.put(key, value);
        }
    }

    /**
     * The value of the parameter at sample invocation time.
     *
     * @return value, may be null
     */
    public Object getCallValue() {
        return value;
    }

    /**
     * The value of the parameter at sample invocation time, formatted according to conversion configuration.
     *
     * @return value, may be null
     */
    public String getCallValueFormatted() {
        String ret;
        if (value == null) {
            ret = null;
        } else {
            ret = (String) conversionService.convert(value, typeDescriptor, TypeDescriptor.valueOf(String.class));
        }
        return ret;
    }

    /**
     * Gets HTML5 parameter type for input field according to {@link Type} annotation.
     *
     * @return the type
     */
    @Override
    public Type getHtmlInputFieldType() {
        final Type ret;
        if (inputAnnotation == null || inputAnnotation.value() == Type.FROM_JAVA) {
            if (isArrayOrCollection() || isRequestBody()) {
                ret = null;
            } else if (DataType.isNumber(getParameterType())) {
                ret = Type.NUMBER;
            } else {
                ret = Type.TEXT;
            }
        } else {
            ret = inputAnnotation.value();
        }
        return ret;
    }


    public boolean isRequestBody() {
        return requestBody != null;
    }

    public boolean isRequestParam() {
        return requestParam != null;
    }

    public boolean isPathVariable() {
        return pathVariable != null;
    }

    public boolean isRequestHeader() {
        return requestHeader != null;
    }

    public boolean isInputParameter() {
        return inputAnnotation != null
                && requestBody == null
                && pathVariable == null
                && requestHeader == null
                && requestParam == null;
    }


    @Override
    public String getRequestHeaderName() {
        return isRequestHeader() ? requestHeader.value() : null;
    }

    /**
     * Has constraints defined via <code>@Input</code> annotation. Note that there might also be other kinds of
     * constraints, e.g. <code>@Select</code> may define values for {@link #getPossibleValues}.
     *
     * @return true if parameter is constrained
     */
    public boolean hasInputConstraints() {
        return !inputConstraints.isEmpty();
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotation) {
        return methodParameter.getParameterAnnotation(annotation);
    }


    /**
     * Determines if request body input parameter has a hidden input property.
     *
     * @param property
     *         name or property path
     * @return true if hidden
     */
    @Override
    public boolean isHidden(String property) {
        Annotation[] paramAnnotations = methodParameter.getParameterAnnotations();
        Input inputAnnotation = methodParameter.getParameterAnnotation(Input.class);
        return inputAnnotation != null && arrayContains(inputAnnotation.hidden(), property);
    }


    @Override
    public boolean isReadOnly(String property) {
        return inputAnnotation != null && (!inputAnnotation.editable() || arrayContains(inputAnnotation.readOnly(), property));
    }


    @Override
    public boolean isIncluded(String property) {
        boolean ret;
        if (inputAnnotation == null) {
            ret = true;
        } else {
            boolean hasExplicitOrImplicitIncludes = hasExplicitOrImplicitPropertyIncludeValue();
            ret = !hasExplicitOrImplicitIncludes || containsPropertyIncludeValue(property);
        }
        return ret;
    }

    /**
     * Find out if property is included by searching through all annotations.
     *
     * @param property
     * @return
     */
    private boolean containsPropertyIncludeValue(String property) {
        return arrayContains(inputAnnotation.readOnly(), property)
                || arrayContains(inputAnnotation.hidden(), property)
                || arrayContains(inputAnnotation.include(), property);
    }

    /**
     * Has any explicit include value or might have implicit includes because there is a hidden or readOnly flag.
     *
     * @return true if explicitly or implicitly included.
     */
    private boolean hasExplicitOrImplicitPropertyIncludeValue() {
        // TODO maybe not a useful optimization
        return inputAnnotation != null && inputAnnotation.readOnly().length > 0
                || inputAnnotation.hidden().length > 0
                || inputAnnotation.include().length > 0;
    }

    /**
     * Determines if request body input parameter should be excluded, considering {@link Input#exclude}.
     *
     * @param property
     *         name or property path
     * @return true if excluded, false if no include statement found or not excluded
     */
    @Override
    public boolean isExcluded(String property) {
        return inputAnnotation != null && arrayContains(inputAnnotation.exclude(), property);
    }

    private boolean arrayContains(String[] array, String toFind) {
        if (array.length == 0) {
            return false;
        }
        for (String item : array) {
            if (toFind.equals(item)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public Object[] getPossibleValues(AnnotatedParameters actionDescriptor) {
        return getPossibleValues(methodParameter, actionDescriptor);
    }

    @Override
    public Object[] getPossibleValues(Method method, int parameterIndex, AnnotatedParameters actionDescriptor) {
        MethodParameter methodParameter = new MethodParameter(method, parameterIndex);
        return getPossibleValues(methodParameter, actionDescriptor);
    }

    @Override
    public Object[] getPossibleValues(Constructor constructor, int parameterIndex, AnnotatedParameters
            actionDescriptor) {
        MethodParameter methodParameter = new MethodParameter(constructor, parameterIndex);
        return getPossibleValues(methodParameter, actionDescriptor);
    }

    public Object[] getPossibleValues(MethodParameter methodParameter, AnnotatedParameters actionDescriptor) {
        try {
            Class<?> parameterType = methodParameter.getNestedParameterType();
            Object[] possibleValues;
            Class<?> nested;
            if (Enum[].class.isAssignableFrom(parameterType)) {
                possibleValues = parameterType.getComponentType()
                        .getEnumConstants();
            } else if (Enum.class.isAssignableFrom(parameterType)) {
                possibleValues = parameterType.getEnumConstants();
            } else if (Collection.class.isAssignableFrom(parameterType)
                    && Enum.class.isAssignableFrom(nested = TypeDescriptor.nested(methodParameter, 1)
                    .getType())) {
                possibleValues = nested.getEnumConstants();
            } else {
                Select select = methodParameter.getParameterAnnotation(Select.class);
                if (select != null) {
                    Class<? extends Options> optionsClass = select.options();
                    Options options = optionsClass.newInstance();
                    // collect call values to pass to options.get
                    List<Object> from = new ArrayList<Object>();
                    for (String paramName : select.args()) {
                        AnnotatedParameter parameterValue = actionDescriptor.getAnnotatedParameter(paramName);
                        if (parameterValue != null) {
                            from.add(parameterValue.getCallValue());
                        }
                    }

                    Object[] args = from.toArray();
                    possibleValues = options.get(select.value(), args);
                } else {
                    possibleValues = new Object[0];
                }
            }
            return possibleValues;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Determines if action input parameter is an array or collection.
     *
     * @return true if array or collection
     */
    public boolean isArrayOrCollection() {
        if (arrayOrCollection == null) {
            arrayOrCollection = DataType.isArrayOrCollection(getParameterType());
        }
        return arrayOrCollection;
    }


    /**
     * Is this action input parameter required, based on the presence of a default value, the parameter annotations and
     * the kind of input parameter.
     *
     * @return true if required
     */
    public boolean isRequired() {
        boolean ret;
        if (isRequestBody()) {
            ret = requestBody.required();
        } else if (isRequestParam()) {
            ret = !(isDefined(requestParam.defaultValue()) || !requestParam.required());
        } else if (isRequestHeader()) {
            ret = !(isDefined(requestHeader.defaultValue()) || !requestHeader.required());
        } else {
            ret = true;
        }
        return ret;
    }

    private boolean isDefined(String defaultValue) {
        return !ValueConstants.DEFAULT_NONE.equals(defaultValue);
    }

    /**
     * Determines default value of request param or request header, if available.
     *
     * @return value or null
     */
    public String getDefaultValue() {
        String ret;
        if (isRequestParam()) {
            ret = isDefined(requestParam.defaultValue()) ?
                    requestParam.defaultValue() : null;
        } else if (isRequestHeader()) {
            ret = !(ValueConstants.DEFAULT_NONE.equals(requestHeader.defaultValue())) ?
                    requestHeader.defaultValue() : null;
        } else {
            ret = null;
        }
        return ret;
    }

    /**
     * Allows convenient access to multiple call values in case that this input parameter is an array or collection.
     * Make sure to check {@link #isArrayOrCollection()} before calling this method.
     *
     * @return call values or empty array
     * @throws UnsupportedOperationException
     *         if this input parameter is not an array or collection
     */
    public Object[] getCallValues() {
        Object[] callValues;
        if (!isArrayOrCollection()) {
            throw new UnsupportedOperationException("parameter is not an array or collection");
        }
        Object callValue = getCallValue();
        if (callValue == null) {
            callValues = new Object[0];
        } else {
            Class<?> parameterType = getParameterType();
            if (parameterType.isArray()) {
                callValues = (Object[]) callValue;
            } else {
                callValues = ((Collection<?>) callValue).toArray();
            }
        }
        return callValues;
    }

    /**
     * Was a sample call value recorded for this parameter?
     *
     * @return if call value is present
     */
    public boolean hasCallValue() {
        return value != null;
    }

    /**
     * Gets parameter name of this action input parameter.
     *
     * @return name
     */
    public String getParameterName() {
        String ret;
        String parameterName = methodParameter.getParameterName();
        if (parameterName == null) {
            methodParameter.initParameterNameDiscovery(new LocalVariableTableParameterNameDiscoverer());
            ret = methodParameter.getParameterName();
        } else {
            ret = parameterName;
        }
        return ret;
    }

    /**
     * Class which declares the method to which this input parameter belongs.
     *
     * @return class
     */
    public Class<?> getDeclaringClass() {
        return methodParameter.getDeclaringClass();
    }

    /**
     * Type of parameter.
     *
     * @return type
     */
    public Class<?> getParameterType() {
        return methodParameter.getParameterType();
    }

    /**
     * Generic type of parameter.
     *
     * @return generic type
     */
    public java.lang.reflect.Type getGenericParameterType() {
        return methodParameter.getGenericParameterType();
    }

    /**
     * Gets the input constraints defined for this action input parameter.
     *
     * @return constraints
     */
    public Map<String, Object> getInputConstraints() {
        return inputConstraints;
    }

    @Override
    public String toString() {
        String kind;
        if (isRequestBody()) {
            kind = "RequestBody";
        } else if (isPathVariable()) {
            kind = "PathVariable";
        } else if (isRequestParam()) {
            kind = "RequestParam";
        } else if (isRequestHeader()) {
            kind = "RequestHeader";
        } else {
            kind = "nested bean property";
        }
        return kind + (getParameterName() != null ? " " + getParameterName() : "") + ": " + (value != null ? value
                .toString() : "no value");
    }
}
