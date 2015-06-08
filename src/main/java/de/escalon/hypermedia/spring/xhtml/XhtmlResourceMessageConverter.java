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

package de.escalon.hypermedia.spring.xhtml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.escalon.hypermedia.PropertyUtils;
import de.escalon.hypermedia.affordance.DataType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.*;

import javax.servlet.http.HttpServletRequest;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;

/**
 * Message converter which represents a restful API as xhtml which can be used by the browser or a rest client. Converts
 * java beans and spring-hateoas Resources to xhtml and maps the body of x-www-form-urlencoded requests to RequestBody
 * method parameters. The media-type xhtml does not officially support methods other than GET or POST, therefore we must
 * &quot;tunnel&quot; other methods when this converter is used with the browser. Spring's {@link
 * org.springframework.web.filter.HiddenHttpMethodFilter} allows to do that with relative ease.
 *
 * @author Dietrich Schulten
 */
public class XhtmlResourceMessageConverter extends AbstractHttpMessageConverter<Object> implements
        GenericHttpMessageConverter<Object> {

    private Charset charset = Charset.forName("UTF-8");
    private String methodParam = "_method";
    private List<String> stylesheets = Collections.emptyList();

    private DocumentationProvider documentationProvider = new DefaultDocumentationProvider();


    public XhtmlResourceMessageConverter() {
        this.setSupportedMediaTypes(Arrays.asList(MediaType.TEXT_HTML, MediaType.APPLICATION_FORM_URLENCODED));
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return true;
    }

    public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws
            IOException, HttpMessageNotReadableException {

        final Class clazz;
        if (type instanceof Class) {
            clazz = (Class) type;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class) {
                clazz = (Class) rawType;
            } else {
                throw new IllegalArgumentException("unexpected raw type " + rawType);
            }
        } else {
            throw new IllegalArgumentException("unexpected type " + type);
        }
        return readInternal(clazz, inputMessage);

    }


    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws IOException,
            HttpMessageNotReadableException {

        InputStream is;
        if (inputMessage instanceof ServletServerHttpRequest) {
            // this is necessary to support HiddenHttpMethodFilter
            // thanks to https://www.w3.org/html/wg/tracker/issues/195
            // but see http://dev.w3.org/html5/decision-policy/html5-2014-plan.html#issues
            // and http://cameronjones.github.io/form-http-extensions/index.html
            // and http://www.w3.org/TR/form-http-extensions/
            // TODO recognize this more safely or make the filter mandatory
            MediaType contentType = inputMessage.getHeaders()
                    .getContentType();
            Charset charset = contentType.getCharSet() != null ? contentType.getCharSet() : this.charset;
            ServletServerHttpRequest servletServerHttpRequest = (ServletServerHttpRequest) inputMessage;
            HttpServletRequest servletRequest = servletServerHttpRequest.getServletRequest();
            is = getBodyFromServletRequestParameters(servletRequest, charset.displayName(Locale.US));
        } else {
            is = inputMessage.getBody();
        }
        return readRequestBody(clazz, is, charset);

    }

    /**
     * From {@link ServletServerHttpRequest}: Use {@link javax.servlet.ServletRequest#getParameterMap()} to reconstruct
     * the body of a form 'POST' providing a predictable outcome as opposed to reading from the body, which can fail if
     * any other code has used ServletRequest to access a parameter thus causing the input stream to be "consumed".
     */
    private InputStream getBodyFromServletRequestParameters(HttpServletRequest request, String charset) throws
            IOException {


        ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
        Writer writer = new OutputStreamWriter(bos, charset);
        @SuppressWarnings("unchecked")
        Map<String, String[]> form = request.getParameterMap();
        for (Iterator<String> nameIterator = form.keySet()
                .iterator(); nameIterator.hasNext(); ) {
            String name = nameIterator.next();
            List<String> values = Arrays.asList(form.get(name));
            for (Iterator<String> valueIterator = values.iterator(); valueIterator.hasNext(); ) {
                String value = valueIterator.next();
                writer.write(URLEncoder.encode(name, charset));
                if (value != null) {
                    writer.write('=');
                    writer.write(URLEncoder.encode(value, charset));
                    if (valueIterator.hasNext()) {
                        writer.write('&');
                    }
                }
            }
            if (nameIterator.hasNext()) {
                writer.append('&');
            }
        }
        writer.flush();

        return new ByteArrayInputStream(bos.toByteArray());
    }

    private Object readRequestBody(Class<?> clazz, InputStream inputStream, Charset charset) throws
            IOException {

        String body = StreamUtils.copyToString(inputStream, charset);

        String[] pairs = StringUtils.tokenizeToStringArray(body, "&");

        MultiValueMap<String, String> formValues = new LinkedMultiValueMap<String, String>(pairs.length);

        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx == -1) {
                formValues.add(URLDecoder.decode(pair, charset.name()), null);
            } else {
                String name = URLDecoder.decode(pair.substring(0, idx), charset.name());
                String value = URLDecoder.decode(pair.substring(idx + 1), charset.name());
                formValues.add(name, value);
            }
        }

        return recursivelyCreateObject(new ArrayDeque<String>(), clazz, formValues);


    }

    Object recursivelyCreateObject(Deque<String> propertyPath, Class<?> clazz, MultiValueMap<String, String>
            formValues) {

        if (Map.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("Map not supported");
        } else if (Collection.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("Collection not supported");
        } else {
            try {
                Constructor[] constructors = clazz.getConstructors();
                Constructor constructor = findDefaultCtor(constructors);
                if (constructor == null) {
                    constructor = findJsonCreator(constructors);
                }
                Assert.notNull(constructor, "no default constructor or JsonCreator found");
                int parameterCount = constructor.getParameterTypes().length;
                Object[] args = new Object[parameterCount];
                if (parameterCount > 0) {
                    Annotation[][] annotationsOnParameters = constructor.getParameterAnnotations();
                    Class[] parameters = constructor.getParameterTypes();
                    int paramIndex = 0;
                    for (Annotation[] annotationsOnParameter : annotationsOnParameters) {
                        for (Annotation annotation : annotationsOnParameter) {
                            if (JsonProperty.class == annotation.annotationType()) {
                                JsonProperty jsonProperty = (JsonProperty) annotation;
                                String paramName = jsonProperty.value();

                                Class<?> parameterType = parameters[paramIndex];
                                if (DataType.isSingleValueType(parameterType)) {
                                    String pathPrefix = propertyPath.isEmpty() ? "" :
                                            StringUtils.collectionToDelimitedString(propertyPath, ".") + ".";
                                    List<String> formValue = formValues.get(pathPrefix + paramName);
                                    if (formValue != null) {
                                        if (formValue.size() == 1) {
                                            args[paramIndex++] = DataType.asType(parameterType, formValue.get(0));
                                        } else {
//                                        // TODO create proper collection type
                                            throw new IllegalArgumentException("variable list not supported");
//                                        List<Object> listValue = new ArrayList<Object>();
//                                        for (String item : formValue) {
//                                            listValue.add(DataType.asType(parameterType, formValue.get(0)));
//                                        }
//                                        args[paramIndex++] = listValue;
                                        }
                                    } else {
                                        args[paramIndex++] = null;
                                    }
                                } else {
                                    propertyPath.add(paramName);
                                    args[paramIndex++] = recursivelyCreateObject(propertyPath, parameterType,
                                            formValues);
                                    propertyPath.removeLast();
                                }
                            }
                        }
                    }
                    Assert.isTrue(args.length == paramIndex, "not all constructor arguments of @JsonCreator are " +
                            "annotated with @JsonProperty");
                }
                Object ret = constructor.newInstance(args);
                BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
                PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
                for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                    Method writeMethod = propertyDescriptor.getWriteMethod();
                    if (writeMethod != null) {
                        String name = propertyDescriptor.getName();
                        List<String> formValue = formValues.get(name);
                        if (formValue != null && formValue.size() == 1) {
                            writeMethod.invoke(ret, DataType.asType(propertyDescriptor.getPropertyType(),
                                    formValue.get(0)));
                        }
                        // TODO list formvalue, consume ctor args
                    }
                }
                return ret;
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate bean " + clazz.getName(), e);
            }
        }
    }

    private Constructor findDefaultCtor(Constructor[] constructors) {
        // TODO duplicate on XhtmlWriter
        Constructor constructor = null;
        for (Constructor ctor : constructors) {
            if (ctor.getParameterTypes().length == 0) {
                constructor = ctor;
            }
        }
        return constructor;
    }

    private Constructor findJsonCreator(Constructor[] constructors) {
        // TODO duplicate on XhtmlWriter
        Constructor constructor = null;
        for (Constructor ctor : constructors) {
            if (AnnotationUtils.getAnnotation(ctor, JsonCreator.class) != null) {
                constructor = ctor;
                break;
            }
        }
        return constructor;
    }

    @Override
    protected void writeInternal(Object t, HttpOutputMessage outputMessage) throws IOException,
            HttpMessageNotWritableException {

        XhtmlWriter xhtmlWriter = new XhtmlWriter(new OutputStreamWriter(outputMessage.getBody()));
        xhtmlWriter.setMethodParam(methodParam);
        xhtmlWriter.setStylesheets(stylesheets);
        xhtmlWriter.setDocumentationProvider(documentationProvider);


        xhtmlWriter.beginHtml("Input Data");
        writeNewResource(xhtmlWriter, t);
        xhtmlWriter.endHtml();
        xhtmlWriter.flush();

    }

    static final Set<String> FILTER_RESOURCE_SUPPORT = new HashSet<String>(Arrays.asList("class", "links", "id"));


    private void writeNewResource(XhtmlWriter writer, Object object) throws IOException {
        writer.beginUnorderedList();
        writeResource(writer, object);
        writer.endUnorderedList();
    }

    /**
     * Recursively converts object to xhtml data.
     *
     * @param object
     *         to convert
     * @param writer
     *         to write to
     */
    private void writeResource(XhtmlWriter writer, Object object) {
        if (object == null) {
            return;
        }
        try {
            if (object instanceof Resource) {
                Resource<?> resource = (Resource<?>) object;
                writer.beginListItem();

                writeResource(writer, resource.getContent());
                writer.writeLinks(resource.getLinks());

                writer.endListItem();
            } else if (object instanceof Resources) {
                Resources<?> resources = (Resources<?>) object;
                // TODO set name using EVO see HypermediaSupportBeanDefinitionRegistrar

                writer.beginListItem();

                writer.beginUnorderedList();
                Collection<?> content = resources.getContent();
                writeResource(writer, content);
                writer.endUnorderedList();

                writer.writeLinks(resources.getLinks());

                writer.endListItem();
            } else if (object instanceof ResourceSupport) {
                ResourceSupport resource = (ResourceSupport) object;
                writer.beginListItem();

                writeObject(writer, resource);
                writer.writeLinks(resource.getLinks());

                writer.endListItem();
            } else if (object instanceof Collection) {
                Collection<?> collection = (Collection<?>) object;
                for (Object item : collection) {
                    writeResource(writer, item);
                }
            } else { // TODO: write li for simple objects in Resources Collection
                writeObject(writer, object);
            }
        } catch (Exception ex) {
            throw new RuntimeException("failed to transform object " + object, ex);
        }

    }

    private void writeObject(XhtmlWriter writer, Object object) throws IOException, IllegalAccessException,
            InvocationTargetException {
        if (!DataType.isSingleValueType(object.getClass())) {
            writer.beginDl();
        }
        if (object instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) object;
            for (Entry<?, ?> entry : map.entrySet()) {
                String name = entry.getKey()
                        .toString();
                Object content = entry.getValue();
                String docUrl = documentationProvider.getDocumentationUrl(name, content);
                writeObjectAttributeRecursively(writer, name, content, docUrl);
            }
        } else if (object instanceof Enum) {
            writeDdForScalarValue(writer, object);
        } else if (object instanceof Currency) {
            // TODO configurable classes which should be rendered with toString
            writeDdForScalarValue(writer, object);
        } else {
            Class<?> aClass = object.getClass();
            Map<String, PropertyDescriptor> propertyDescriptors = PropertyUtils.getPropertyDescriptors(object);
            // getFields retrieves public only
            Field[] fields = aClass.getFields();
            for (Field field : fields) {
                String name = field.getName();
                if (!propertyDescriptors.containsKey(name)) {
                    Object content = field.get(object);
                    String docUrl = documentationProvider.getDocumentationUrl(field, content);
                    writeObjectAttributeRecursively(writer, name, content, docUrl);
                }
            }
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors.values()) {
                String name = propertyDescriptor.getName();
                if (FILTER_RESOURCE_SUPPORT.contains(name)) {
                    continue;
                }
                Method readMethod = propertyDescriptor.getReadMethod();
                if (readMethod != null) {
                    Object content = readMethod.invoke(object);
                    String docUrl = documentationProvider.getDocumentationUrl(readMethod, content);
                    writeObjectAttributeRecursively(writer, name, content, docUrl);
                }
            }
        }
        if (!DataType.isSingleValueType(object.getClass())) {
            writer.endDl();
        }
    }

    private void writeObjectAttributeRecursively(XhtmlWriter writer, String name, Object content, String
            documentationUrl)
            throws IOException {
        writeDtWithDoc(writer, name, documentationUrl);
        Object value = getContentAsScalarValue(content);
        if (value != null) {
            if (value != NULL_VALUE) {
                writeDdForScalarValue(writer, value);
            }
        } else if (DataType.isSingleValueType(content.getClass())) {
            writeDdForScalarValue(writer, content.toString());
        } else {
            writer.beginDd();
            writeNewResource(writer, content);
            writer.endDd();
        }
    }

    private void writeDtWithDoc(XhtmlWriter writer, String name, String documentationUrl) throws IOException {
        if (documentationUrl == null) {
            writer.beginDt();
            writer.write(name);
            writer.endDt();
        } else {
            writer.beginDt();
            writer.beginAnchor(XhtmlWriter.OptionalAttributes.attr("href", documentationUrl)
                    .and("title", documentationUrl));
            writer.write(name);
            writer.endAnchor();
            writer.endDt();
        }
    }

    private void writeDdForScalarValue(XhtmlWriter writer, Object value) throws IOException {
        writer.beginDd();
        writer.write(value.toString());
        writer.endDd();
    }


    @Override
    public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
        return MediaType.APPLICATION_FORM_URLENCODED == mediaType;
    }

    /**
     * Sets method param name for HTML PUT/DELETE/PATCH workaround.
     *
     * @param methodParam
     *         to use
     * @see org.springframework.web.filter.HiddenHttpMethodFilter
     */
    public void setMethodParam(String methodParam) {
        this.methodParam = methodParam;
    }

    /**
     * Sets css stylesheets to apply to the form.
     *
     * @param stylesheets
     *         urls of css stylesheets to include, e.g. &quot;https://maxcdn.bootstrapcdn.com/bootstrap/3.3
     *         .4/css/bootstrap.min.css&quot;
     */
    public void setStylesheets(List<String> stylesheets) {
        Assert.notNull(stylesheets);
        this.stylesheets = stylesheets;
    }

    public void setDocumentationProvider(DocumentationProvider documentationProvider) {
        this.documentationProvider = documentationProvider;
    }

    static class NullValue {

    }

    public static final NullValue NULL_VALUE = new NullValue();

    private static Object getContentAsScalarValue(Object content) {
        Object value = null;

        if (content == null) {
            value = NULL_VALUE;
        } else if (content instanceof String || content instanceof Number || content.equals(false) || content.equals
                (true)) {
            value = content;
        }
        return value;
    }


}
