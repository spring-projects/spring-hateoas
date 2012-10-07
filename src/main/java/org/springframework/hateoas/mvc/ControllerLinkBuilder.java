/*
 * Copyright 2012 the original author or authors.
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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkTemplate;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

/**
 * Builder to ease building {@link Link} instances pointing to Spring MVC
 * controllers.
 *
 * @author Oliver Gierke
 */
public class ControllerLinkBuilder extends
        UriComponentsLinkBuilder<ControllerLinkBuilder> {

    /**
     * Creates a new {@link ControllerLinkBuilder} using the given
     * {@link UriComponentsBuilder}.
     *
     * @param builder
     *            must not be {@literal null}.
     */
    private ControllerLinkBuilder(UriComponentsBuilder builder) {
        super(builder);
    }

    /**
     * Creates a new {@link ControllerLinkBuilder} with a base of the mapping
     * annotated to the given controller class.
     *
     * @param controller
     *            the class to discover the annotation on, must not be
     *            {@literal null}.
     * @return
     */
    public static ControllerLinkBuilder linkTo(Class<?> controller) {
        return linkTo(controller, new Object[0]);
    }

    /**
     * Creates a new {@link ControllerLinkBuilder} with a base of the mapping
     * annotated to the given controller class. The additional parameters are
     * used to fill up potentially available path variables in the class scope
     * request mapping.
     *
     * @param controller
     *            the class to discover the annotation on, must not be
     *            {@literal null}.
     * @param parameters
     *            additional parameters to bind to the URI template declared in
     *            the annotation, must not be {@literal null}.
     * @return
     */
    public static ControllerLinkBuilder linkTo(Class<?> controller,
            Object... parameters) {

        Assert.notNull(controller);

        RequestMapping annotation = AnnotationUtils.findAnnotation(controller,
                RequestMapping.class);
        String[] mapping = annotation == null ? new String[0]
                : (String[]) AnnotationUtils.getValue(annotation);

        if (mapping.length > 1) {
            throw new IllegalStateException(
                    "Multiple controller mappings defined! Unable to build URI!");
        }

        ControllerLinkBuilder builder = new ControllerLinkBuilder(
                ServletUriComponentsBuilder.fromCurrentServletMapping());

        if (mapping.length == 0) {
            return builder;
        }

        UriTemplate template = new UriTemplate(mapping[0]);
        return builder.slash(template.expand(parameters));
    }

    /**
     * Allows to create a representation of a method on the given controller,
     * for use with {@link ControllerLinkBuilder#linkTo(Object)}. Define the
     * method representation by simply calling the desired method as shown
     * below.
     * <p>
     * This example creates a representation of the method
     * <code>PersonController.showAll()</code>:
     *
     * <pre>
     * methodOn(PersonController.class).showAll();
     * </pre>
     *
     * @param controller
     * @return
     * @see #linkTo(Object)
     */
    public static <T> T methodOn(Class<T> controller) {

        Invocations invocations = new InvocationsImpl();

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(controller);
        enhancer.setCallback(new RecordingMethodInterceptor(invocations));
        enhancer.setInterfaces(new Class<?>[] { Invocations.class });
        @SuppressWarnings("unchecked")
        T ret = (T) enhancer.create();
        return ret;
    }

    /**
     * Creates a new {@link ControllerLinkBuilder} based on the given controller
     * method, resolving URI templates if necessary. The controller method is
     * created by {@link #methodOn(Class)}.
     * <p>
     * Consider the following PersonController with a class level mapping and a
     * method level mapping:
     *
     * <pre>
     * &#064;Controller
     * &#064;RequestMapping(&quot;/people&quot;)
     * public class PersonController {
     *
     *     &#064;RequestMapping(value = &quot;/{personId}/address&quot;, method = RequestMethod.GET)
     *     public HttpEntity&lt;PersonResource&gt; showAddress(@PathVariable Long personId) {
     *         (...)
     *     }
     * }
     * </pre>
     *
     * You may link to this person controller's <code>/{personId}/address</code>
     * resource from another controller. Assuming we are within a method where
     * we produce the personResource for a given personId, e.g. from a form
     * request for www.example.com/people/search?id=42, we can do:
     *
     * <pre>
     * Link address = linkTo(methodOn(PersonController.class).show(personId).withRel("address");
     * PersonResource personResource = (...);
     * personResource.addLink(address);
     * </pre>
     *
     * The <code>linkTo</code> method above gives us a
     * <code>Link</link> to the person's address, which we can add to the personResource. Note that the path
     * variable <code>{personId}</code> will be expanded to its actual value:
     *
     * <pre>
     * http://www.example.com/people/42/address
     * </pre>
     *
     * @param method
     *            representation of a method on the target controller, created
     *            by {@link #methodOn(Class)}.
     * @return link builder which expects you to set a rel, e.g. using
     *         {@link #withRel(String)}.
     * @see #methodOn(Class)
     */
    public static ControllerLinkBuilder linkTo(Object method) {
        Invocations invocations = (Invocations) method;
        List<Invocation> recorded = invocations.getInvocations();
        Invocation invocation = recorded.get(0);
        String classLevelMapping = getClassLevelMapping(invocation.target
                .getClass());
        LinkTemplate template = createLinkTemplate(classLevelMapping,
                invocation.method);

        ControllerLinkBuilder builder = new ControllerLinkBuilder(
                ServletUriComponentsBuilder.fromCurrentServletMapping());

        UriTemplate uriTemplate = new UriTemplate(template.getHref());
        return builder.slash(uriTemplate.expand(invocation.args));
    }

    /**
     * Extracts all resources with placeholders as link templates, containing
     * the path variables and request params of the resource.
     *
     * Useful for two things:
     * <ul>
     * <li>for request params, allows building forms to request the resource</li>
     * <li>in case of known variable values, allows to replace path variables.</li>
     * </ul>
     *
     * @param controller
     * @return
     */
    public static List<LinkTemplate> linksToResources(Class<?> controller) {
        List<LinkTemplate> ret = new ArrayList<LinkTemplate>();

        final String classLevelMapping = getClassLevelMapping(controller);

        Method[] declaredMethods = controller.getDeclaredMethods();
        for (Method method : declaredMethods) {
            final LinkTemplate linkTemplate = createLinkTemplate(
                    classLevelMapping, method);
            ret.add(linkTemplate);
        }
        return ret;
    }

    static class Invocation {

        final Object target;
        final Method method;
        final Object[] args;

        public Invocation(Object target, Method method, Object[] args) {
            super();
            this.target = target;
            this.method = method;
            this.args = args;
        }

    }

    public static interface Invocations {
        List<Invocation> getInvocations();
    }

    static class InvocationsImpl implements Invocations {

        List<Invocation> invocations = new ArrayList<Invocation>();

        @Override
        public List<Invocation> getInvocations() {
            return invocations;
        }

    }

    private static class RecordingMethodInterceptor implements
            MethodInterceptor {

        Invocations invocations = new InvocationsImpl();

        public RecordingMethodInterceptor(Invocations invocations) {
            super();
            this.invocations = invocations;
        }

        @Override
        public Object intercept(Object obj, Method method, Object[] args,
                MethodProxy proxy) throws Throwable {

            Method getInvocations = Invocations.class
                    .getMethod("getInvocations");
            if (getInvocations.equals(method)) {
                return invocations.getInvocations();
            } else {
                Invocation invocation = new Invocation(obj, method, args);
                invocations.getInvocations().add(invocation);
                Class<?> returnType = method.getReturnType();
                Object returnProxy = Enhancer.create(returnType,
                        new Class<?>[] { Invocations.class }, this);
                return returnProxy;
            }

        }

    }

    /**
     * Creates a link template based on the given class level mapping and the
     * requestmapping of the given method.
     *
     * @param classLevelMapping
     * @param method
     * @return link template or empty String, if the controller is completely
     *         unmapped.
     */
    private static LinkTemplate createLinkTemplate(
            final String classLevelMapping, Method method) {
        RequestMapping annotation = AnnotationUtils.findAnnotation(method,
                RequestMapping.class);
        String[] params = (String[]) AnnotationUtils.getValue(annotation,
                "params");
        String[] mappings = annotation == null ? new String[0]
                : (String[]) AnnotationUtils.getValue(annotation);

        if (mappings.length > 1) {
            throw new IllegalStateException(
                    "Multiple mappings defined on method" + method.getName());
        }

        final LinkTemplate linkTemplate;
        if (classLevelMapping.length() == 0 && mappings.length == 0) {
            return null;
        } else {
            final String methodMapping;
            if (mappings.length == 1) {
                methodMapping = mappings[0];
            } else {
                methodMapping = "";
            }
            linkTemplate = new LinkTemplate(classLevelMapping + methodMapping,
                    method.getName());
            for (String param : params) {
                linkTemplate.addParam(param, Object.class);
            }
        }
        return linkTemplate;
    }

    /**
     * Gets class level mapping of the given controller.
     *
     * @param controller
     * @return mapping or empty string if there is no class level mapping
     */
    private static String getClassLevelMapping(Class<?> controller) {
        RequestMapping classLevelAnnotation = AnnotationUtils.findAnnotation(
                controller, RequestMapping.class);

        String[] classLevelMappings = classLevelAnnotation == null ? new String[0]
                : (String[]) AnnotationUtils.getValue(classLevelAnnotation);

        if (classLevelMappings.length > 1) {
            throw new IllegalStateException(
                    "Multiple controller mappings defined! Unable to build URI!");
        }

        final String classLevelMapping;
        if (classLevelMappings.length == 1) {
            classLevelMapping = classLevelMappings[0];
        } else {
            classLevelMapping = "";
        }
        return classLevelMapping;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.hateoas.UriComponentsLinkBuilder#getThis()
     */
    @Override
    protected ControllerLinkBuilder getThis() {
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.springframework.hateoas.UriComponentsLinkBuilder#createNewInstance
     * (org.springframework.web.util.UriComponentsBuilder)
     */
    @Override
    protected ControllerLinkBuilder createNewInstance(
            UriComponentsBuilder builder) {
        return new ControllerLinkBuilder(builder);
    }
}
