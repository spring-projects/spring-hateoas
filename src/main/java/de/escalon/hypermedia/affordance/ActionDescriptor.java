/*
 * Copyright (c) 2014. Escalon System-Entwicklung, Dietrich Schulten
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package de.escalon.hypermedia.affordance;

import de.escalon.hypermedia.action.Action;
import de.escalon.hypermedia.action.Cardinality;
import org.springframework.util.Assert;


import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Describes an HTTP method independently of a specific rest framework.
 * Has knowledge about possible request data, i.e. which types and values
 * are suitable for an action. For example, an action descriptor can be used to create a form with select options and
 * typed input fields that calls a POST handler. It has {@link AnnotatedParameter}s which represent method handler
 * arguments. Supported method handler arguments are:
 * <ul>
 * <li>path variables</li>
 * <li>request params (url query params)</li>
 * <li>request headers</li>
 * <li>request body</li>
 * </ul>
 *
 * @author Dietrich Schulten
 */
public class ActionDescriptor implements AnnotatedParameters {

    private String httpMethod;
    private String actionName;

    private String semanticActionType;
    private Map<String, AnnotatedParameter> requestParams = new LinkedHashMap<String, AnnotatedParameter>();
    private Map<String, AnnotatedParameter> pathVariables = new LinkedHashMap<String, AnnotatedParameter>();
    private Map<String, AnnotatedParameter> requestHeaders = new LinkedHashMap<String, AnnotatedParameter>();

    private AnnotatedParameter requestBody;
    private Cardinality cardinality = Cardinality.SINGLE;

    /**
     * Creates an {@link ActionDescriptor}.
     *
     * @param actionName name of the action, e.g. the method name of the handler method.
     *                   Can be used by an action representation, e.g. to identify the action using a form name.
     * @param httpMethod used during submit
     */
    public ActionDescriptor(String actionName, String httpMethod) {
        Assert.notNull(actionName);
        Assert.notNull(httpMethod);
        this.httpMethod = httpMethod;
        this.actionName = actionName;
    }



    /**
     * The name of the action, usually the method name of the handler method.
     *
     * @return action name, never null
     */
    public String getActionName() {
        return actionName;
    }

    /**
     * Gets the http method of this action.
     * @return method, never null
     */
    public String getHttpMethod() {
        return httpMethod;
    }

    /**
     * Gets the path variable names.
     * @return names or empty collection, never null
     */
    public Collection<String> getPathVariableNames() {
        return pathVariables.keySet();
    }

    /**
     * Gets the request header names.
     * @return names or empty collection, never null
     */
    public Collection<String> getRequestHeaderNames() {
        return requestHeaders.keySet();
    }

    /**
     * Gets the request parameter (query param) names.
     * @return names or empty collection, never null
     */
    public Collection<String> getRequestParamNames() {
        return requestParams.keySet();
    }

    /**
     * Adds descriptor for request param.
     * @param annotatedParameter descriptor
     * @param key name of request param
     */
    public void addRequestParam(String key, AnnotatedParameter annotatedParameter) {
        requestParams.put(key, annotatedParameter);
    }

    /**
     * Adds descriptor for path variable.
     * @param annotatedParameter descriptor
     * @param key name of path variable
     */

    public void addPathVariable(String key, AnnotatedParameter annotatedParameter) {
        pathVariables.put(key, annotatedParameter);
    }

    /**
     * Adds descriptor for request header.
     * @param annotatedParameter descriptor
     * @param key name of request header
     */
    public void addRequestHeader(String key, AnnotatedParameter annotatedParameter) {
        requestHeaders.put(key, annotatedParameter);
    }

    /**
     * Gets input parameter info which is part of the URL mapping, both request parameters and path variables.
     *
     * @param name to retrieve
     * @return parameter descriptor or null
     */
    @Override
    public AnnotatedParameter getAnnotatedParameter(String name) {
        AnnotatedParameter ret = requestParams.get(name);
        if (ret == null) {
            ret = pathVariables.get(name);
        }
        return ret;
    }

    /**
     * Gets request header info.
     *
     * @param name of the request header parameter name
     * @return request header descriptor or null
     */
    public AnnotatedParameter getRequestHeader(String name) {
        return requestHeaders.get(name);
    }

    /**
     * Gets request body info.
     *
     * @return request body descriptor or null
     */
    public AnnotatedParameter getRequestBody() {
        return requestBody;
    }

    /**
     * Determines if this descriptor has a request body.
     *
     * @return true if request body is present
     */
    public boolean hasRequestBody() {
        return requestBody != null;
    }

    /**
     * Allows to set request body descriptor.
     *
     * @param requestBody descripto
     */
    public void setRequestBody(AnnotatedParameter requestBody) {
        this.requestBody = requestBody;
    }

    /**
     * Gets semantic type of action, e.g. a subtype of hydra:Operation or schema:Action.
     * Use {@link Action} on a method handler to define the semantic type of an action.
     *
     * @return URL identifying the type
     */
    public String getSemanticActionType() {
        return semanticActionType;
    }

    /**
     * Sets semantic type of action, e.g. a subtype of hydra:Operation or schema:Action.
     *
     * @param semanticActionType URL identifying the type
     */
    public void setSemanticActionType(String semanticActionType) {
        this.semanticActionType = semanticActionType;
    }

    /**
     * Determines action input parameters for required url variables.
     *
     * @return required url variables
     */
    public Map<String, AnnotatedParameter> getRequiredParameters() {
        Map<String, AnnotatedParameter> ret = new HashMap<String, AnnotatedParameter>();
        for (Map.Entry<String, AnnotatedParameter> entry : requestParams.entrySet()) {
            AnnotatedParameter actionInputParameter = entry.getValue();
            if (actionInputParameter.isRequired()) {
                ret.put(entry.getKey(), actionInputParameter);
            }
        }
        for (Map.Entry<String, AnnotatedParameter> entry : pathVariables.entrySet()) {
            AnnotatedParameter actionInputParameter = entry.getValue();
            ret.put(entry.getKey(), actionInputParameter);
        }
        // requestBody not supported, would have to use exploded modifier
        return ret;
    }


    /**
     * Allows to set the cardinality, i.e. specify if the action refers to a collection or a single resource.
     * Default is {@link Cardinality#SINGLE}
     * @param cardinality to set
     */
    public void setCardinality(Cardinality cardinality) {
        this.cardinality = cardinality;
    }

    /**
     * Allows to decide whether or not the action refers to a collection resource.
     * @return cardinality
     */
    public Cardinality getCardinality() {
        return cardinality;
    }
}
