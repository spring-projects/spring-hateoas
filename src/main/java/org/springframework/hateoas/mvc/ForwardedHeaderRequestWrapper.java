/*
 * Copyright 2014 the original author or authors.
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
package org.springframework.hateoas.mvc;

import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.*;

import static org.springframework.util.StringUtils.hasText;

/**
 * A RequestWrapper, which provides static utility methods to facilitate creating a {@link OncePerRequestFilter}, to
 * auto add X-Forwarded-* HTTP headers to incoming HTTP requests.
 *
 * <pre>
 * import static org.springframework.hateoas.mvc.FowardedHeaderRequestWrapper.buildForwardedHeadersFilter;
 * ...
 *     @Value("${proxy.url:https://mydefaultproxy.net:1234/somecontext}")
 *     URI baseProxyUrl;
 *
 *     @Bean
 *     Filter interceptor() {
 *         return buildForwardedHeadersFilter(baseProxyUrl);
 *     }
 * ...
 * </pre>
 *
 * @author Nick Grealy
 */
public class ForwardedHeaderRequestWrapper extends HttpServletRequestWrapper {

    Map<String, List<String>> headerOverrides = new HashMap<String, List<String>>();

    public ForwardedHeaderRequestWrapper(ServletRequest request, Map<String, List<String>> overrides) {
        super((HttpServletRequest) request);
        this.headerOverrides = overrides;
    }

    @Override
    public String getHeader(String name) {
        if (headerOverrides.containsKey(name)) {
            return headerOverrides.get(name).get(0);
        } else {
            return super.getHeader(name);
        }
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        List<String> headers = new ArrayList<String>();
        if (headerOverrides.containsKey(name)){
            headers.addAll(headerOverrides.get(name));
        }
        headers.addAll(Collections.list(super.getHeaders(name)));
        return Collections.enumeration(headers);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        List<String> headers = new ArrayList<String>();
        headers.addAll(headerOverrides.keySet());
        headers.addAll(Collections.list(super.getHeaderNames()));
        return Collections.enumeration(headers);
    }

    /**
     * Creates a Filter which adds 'X-Forwarded-*' headers to the HTTP request.
     *
     * @param uri must not be {@literal null}.
     * @return
     */
    public static OncePerRequestFilter buildForwardedHeadersFilter(URI uri) {
        return buildForwardedHeadersFilter(uri.getScheme(), uri.getHost(), uri.getPort(), uri.getPath());
    }


    /**
     * Creates a Filter which adds 'X-Forwarded-*' headers to the HTTP request.
     *
     * @param protocol
     * @param hostname
     * @param port
     * @param path
     * @return
     */
    public static OncePerRequestFilter buildForwardedHeadersFilter(String protocol, String hostname, Integer port, String path) {
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        if (hasText(protocol)){
            headers.put("X-Forwarded-Proto", Arrays.asList(protocol));
        }
        if (hasText(hostname)){
            headers.put("X-Forwarded-Host", Arrays.asList(hostname));
        }
        if (port != null){
            headers.put("X-Forwarded-Port", Arrays.asList(String.valueOf(port)));
        }
        if (hasText(path)){
            headers.put("X-Forwarded-Path", Arrays.asList(path));
        }
        return buildHeaderOverridesFilter(headers);
    }

    protected static OncePerRequestFilter buildHeaderOverridesFilter(final Map<String, List<String>> headerOverrides) {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain
                    filterChain) throws ServletException, IOException {
                filterChain.doFilter(new ForwardedHeaderRequestWrapper(request, headerOverrides), response);
            }
        };
    }
}
