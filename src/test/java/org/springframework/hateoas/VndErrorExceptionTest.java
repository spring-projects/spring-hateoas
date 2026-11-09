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

import org.junit.Test;

import java.nio.charset.Charset;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 * Tests the {@link VndErrorException}
 *
 * @author Jakub Narloch
 */
public class VndErrorExceptionTest {

    private static final String LOGREF = "5814b624-a66d-4ef3-9bfa-2b0d2a7baaf9";

    private static final String UNEXPECTED = "Unexpected";

    private VndErrorException exception;

    @Test
    public void shouldCreateVndErrorWithStatus() {

        exception = new VndErrorException(INTERNAL_SERVER_ERROR, new VndErrors(LOGREF, UNEXPECTED));

        assertThat(exception.getStatusCode(), is(INTERNAL_SERVER_ERROR));
        assertThat(exception.getVndErrors(), is(notNullValue()));
        VndErrors.VndError vndError = exception.getVndErrors().iterator().next();
        assertThat(vndError.getLogref(), is(LOGREF));
        assertThat(vndError.getMessage(), is(UNEXPECTED));
    }

    @Test
    public void shouldCreateVndErrorWithStatusText() {

        exception = new VndErrorException(INTERNAL_SERVER_ERROR, "500", new VndErrors(LOGREF, UNEXPECTED));

        assertThat(exception.getStatusCode(), is(INTERNAL_SERVER_ERROR));
        assertThat(exception.getStatusText(), is("500"));
        assertThat(exception.getVndErrors(), is(notNullValue()));
        VndErrors.VndError vndError = exception.getVndErrors().iterator().next();
        assertThat(vndError.getLogref(), is(LOGREF));
        assertThat(vndError.getMessage(), is(UNEXPECTED));
    }

    @Test
    public void shouldCreateVndErrorWithStatusTextAndBody() throws Exception {

        exception = new VndErrorException(INTERNAL_SERVER_ERROR, "500", UNEXPECTED.getBytes("UTF-8"),
                Charset.forName("UTF-8"), new VndErrors(LOGREF, UNEXPECTED));

        assertThat(exception.getStatusCode(), is(INTERNAL_SERVER_ERROR));
        assertThat(exception.getStatusText(), is("500"));
        assertThat(exception.getResponseBodyAsString(), is(UNEXPECTED));
        assertThat(exception.getVndErrors(), is(notNullValue()));
        VndErrors.VndError vndError = exception.getVndErrors().iterator().next();
        assertThat(vndError.getLogref(), is(LOGREF));
        assertThat(vndError.getMessage(), is(UNEXPECTED));
    }
}