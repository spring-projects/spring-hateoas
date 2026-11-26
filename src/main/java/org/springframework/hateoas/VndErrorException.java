/*
 * Copyright 2013-2016 the original author or authors.
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
package org.springframework.hateoas;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import java.nio.charset.Charset;

/**
 * A base exception used for propagating the {@link VndErrors} received from remote service call.
 *
 * @author Jakub Narloch
 * @see VndErrors
 */
public class VndErrorException extends HttpStatusCodeException {

    /**
     * The vnd errors.
     */
    private final VndErrors vndErrors;

    /**
     * Creates new instance of {@link VndErrorException} with status code and vnd errors.
     *
     * @param statusCode      the status code
     * @param vndErrors       the vnd errors
     */
    public VndErrorException(HttpStatus statusCode, VndErrors vndErrors) {
        super(statusCode);
        this.vndErrors = vndErrors;
    }

    /**
     * Creates new instance of {@link VndErrorException} with status code and vnd errors.
     *
     * @param statusCode      the status code
     * @param statusText      the status text
     * @param vndErrors       the vnd errors
     */
    public VndErrorException(HttpStatus statusCode, String statusText, VndErrors vndErrors) {
        super(statusCode, statusText);
        this.vndErrors = vndErrors;
    }

    /**
     * Creates new instance of {@link VndErrorException} with status code, response body and vnd errors.
     *
     * @param statusCode      the status code
     * @param statusText      the status text
     * @param responseBody    the response body
     * @param responseCharset the response charset
     * @param vndErrors       the vnd errors
     */
    public VndErrorException(HttpStatus statusCode, String statusText, byte[] responseBody, Charset responseCharset,
                             VndErrors vndErrors) {
        super(statusCode, statusText, responseBody, responseCharset);
        this.vndErrors = vndErrors;
    }

    /**
     * Creates new instance of {@link VndErrorException} with status code, http headers, response body and vnd errors.
     *
     * @param statusCode      the status code
     * @param statusText      the status text
     * @param responseHeaders the response headers
     * @param responseBody    the response body
     * @param responseCharset the response charset
     * @param vndErrors       the vnd errors
     */
    public VndErrorException(HttpStatus statusCode, String statusText, HttpHeaders responseHeaders, byte[] responseBody,
                             Charset responseCharset, VndErrors vndErrors) {
        super(statusCode, statusText, responseHeaders, responseBody, responseCharset);
        this.vndErrors = vndErrors;
    }

    /**
     * Retrieves the vnd errors.
     *
     * @return the vnd errors
     */
    public VndErrors getVndErrors() {
        return vndErrors;
    }
}
