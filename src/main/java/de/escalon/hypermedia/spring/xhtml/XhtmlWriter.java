package de.escalon.hypermedia.spring.xhtml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.escalon.hypermedia.PropertyUtils;
import de.escalon.hypermedia.action.Input;
import de.escalon.hypermedia.action.Type;
import de.escalon.hypermedia.affordance.ActionDescriptor;
import de.escalon.hypermedia.affordance.Affordance;
import de.escalon.hypermedia.affordance.AnnotatedParameter;
import de.escalon.hypermedia.affordance.DataType;
import de.escalon.hypermedia.spring.ActionInputParameter;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.Property;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.TemplateVariable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static de.escalon.hypermedia.spring.xhtml.XhtmlWriter.OptionalAttributes.attr;
import static org.springframework.util.StringUtils.collectionToDelimitedString;

/**
 * Created by Dietrich on 09.02.2015.
 */
public class XhtmlWriter extends Writer {
    private Writer writer;
    private List<String> stylesheets = Collections.emptyList();

    public static final String HTML_HEAD_START = "" + //
            //"<?xml version='1.0' encoding='UTF-8' ?>" + // formatter
            "<!DOCTYPE html>" + //
            "<html xmlns='http://www.w3.org/1999/xhtml'>" + //
            "<head>" + //
            "<title>%s</title>";


    public static final String HTML_STYLESHEET = "" + //
            "<link rel=\"stylesheet\" href=\"%s\"  />";

    public static final String HTML_HEAD_END = "" + //
            "</head>" + //
            "<body>" + //
            "<div class=\"container\">\n" + //
            "<div class=\"row\">";

    public static final String HTML_END = "" + //
            "</div>" +
            "</div>" +
            "</body>" + //
            "</html>";

    private String methodParam = "_method";
    private DocumentationProvider documentationProvider = new DefaultDocumentationProvider();

    private String formControlClass = "form-control";
    private String formGroupClass = "form-group";
    private String controlLabelClass = "control-label";

    public XhtmlWriter(Writer writer) {
        this.writer = writer;
    }

    public void setMethodParam(String methodParam) {
        this.methodParam = methodParam;
    }

    public void beginHtml(String title) throws IOException {
        write(String.format(HTML_HEAD_START, title));
        for (String stylesheet : stylesheets) {
            write(String.format(HTML_STYLESHEET, stylesheet));
        }
        write(String.format(HTML_HEAD_END, title));
    }


    public void endHtml() throws IOException {
        write(HTML_END);
    }

    public void beginDiv() throws IOException {
        writer.write("<div>");
    }

    public void beginDiv(OptionalAttributes attributes) throws IOException {
        writer.write("<div ");
        writeAttributes(attributes);
        endTag();
    }

    public void endDiv() throws IOException {
        writer.write("</div>");
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        writer.write(cbuf, off, len);
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    public void beginUnorderedList() throws IOException {
        writer.write("<ul class=\"list-group\">");
    }

    public void endUnorderedList() throws IOException {
        writer.write("</ul>");
    }

    public void beginListItem() throws IOException {
        writer.write("<li class=\"list-group-item\">");
    }

    public void endListItem() throws IOException {
        writer.write("</li>");
    }


    public void beginSpan() throws IOException {
        writer.write("<span>");
    }

    public void endSpan() throws IOException {
        writer.write("</span>");
    }

    public void beginDl() throws IOException {
        // TODO: make this configurable?
        writer.write("<dl >");
    }

    public void endDl() throws IOException {
        writer.write("</dl>");
    }


    public void beginDt() throws IOException {
        writer.write("<dt>");
    }

    public void endDt() throws IOException {
        writer.write("</dt>");
    }

    public void beginDd() throws IOException {
        writer.write("<dd>");
    }

    public void endDd() throws IOException {
        writer.write("</dd>");
    }

    public void writeSpan(Object value) throws IOException {
        beginSpan();
        writer.write(value.toString());
        endSpan();
    }

    public void writeDefinitionTerm(Object value) throws IOException {
        beginDt();
        writer.write(value.toString());
        endDt();
    }

    public void setStylesheets(List<String> stylesheets) {
        Assert.notNull(stylesheets);
        this.stylesheets = stylesheets;
    }

    public void setDocumentationProvider(DocumentationProvider documentationProvider) {
        this.documentationProvider = documentationProvider;
    }

    public static class OptionalAttributes {

        private Map<String, String> attributes = new LinkedHashMap<String, String>();

        /**
         * Creates OptionalAttributes with one optional attribute having name if value is not null.
         *
         * @param name
         *         of first attribute
         * @param value
         *         may be null
         * @return builder with one attribute, or builder without attribute if value is null
         */
        public static OptionalAttributes attr(String name, Object value) {
            Assert.isTrue(name != null && value != null || value == null);
            OptionalAttributes attributeBuilder = new OptionalAttributes();
            addAttributeIfValueNotNull(name, value, attributeBuilder);
            return attributeBuilder;
        }

        private static void addAttributeIfValueNotNull(String name, Object value, OptionalAttributes attributeBuilder) {
            if (value != null) {
                attributeBuilder.attributes.put(name, value.toString());
            }
        }


        public OptionalAttributes and(String name, String value) {
            addAttributeIfValueNotNull(name, value, this);
            return this;
        }

        public Map<String, String> build() {
            return attributes;
        }

        /**
         * Creates OptionalAttributes builder.
         *
         * @return builder
         */
        public static OptionalAttributes attr() {
            return attr(null, null);
        }
    }


    public void writeLinks(List<Link> links) throws IOException {
        for (Link link : links) {

            if (link instanceof Affordance) {
                Affordance affordance = (Affordance) link;
                List<ActionDescriptor> actionDescriptors = affordance.getActionDescriptors();
                if (actionDescriptors.isEmpty()) {
                    // treat like simple link
                    appendLinkWithoutActionDescriptor(affordance);
                } else {
                    if (affordance.isTemplated()) {
                        // TODO ensure that template expansion always takes place for base uri
                        if (!affordance.isBaseUriTemplated()) {
                            for (ActionDescriptor actionDescriptor : actionDescriptors) {
                                RequestMethod httpMethod = RequestMethod.valueOf(actionDescriptor.getHttpMethod());
                                // html does not allow templated action attr for forms, only render GET form
                                if (RequestMethod.GET == httpMethod) {
                                    // TODO: partial uritemplate query must become hidden field
                                    appendForm(affordance, actionDescriptor);
                                }
                                // TODO write human-readable description of additional methods?
                            }
                        }
                    } else {
                        for (ActionDescriptor actionDescriptor : actionDescriptors) {
                            // TODO write documentation about the supported action and maybe fields?
                            if ("GET".equals(actionDescriptor.getHttpMethod()) &&
                                    actionDescriptor.getRequestParamNames()
                                            .isEmpty()) {
                                // GET without params is simple <a href>
                                writeAnchor(OptionalAttributes.attr("href", affordance.expand()
                                        .getHref())
                                        .and("rel", affordance.getRel()), actionDescriptor.getActionName());
                            } else {
                                appendForm(affordance, actionDescriptor);
                            }
                        }
                    }
                }
            } else { // simple link, may be templated
                appendLinkWithoutActionDescriptor(link);
            }
        }
    }

    /**
     * Appends form and squashes non-GET or POST to POST. If required, adds _method field for handling by an appropriate
     * filter such as Spring's HiddenHttpMethodFilter.
     *
     * @param affordance
     *         to make into a form
     * @param actionDescriptor
     *         describing the form action
     * @throws IOException
     * @see <a href="http://docs.spring.io/spring/docs/3.0
     * .x/javadoc-api/org/springframework/web/filter/HiddenHttpMethodFilter.html">Spring
     * MVC HiddenHttpMethodFilter</a>
     */
    private void appendForm(Affordance affordance, ActionDescriptor actionDescriptor) throws IOException {
        String formName = actionDescriptor.getActionName();
        RequestMethod httpMethod = RequestMethod.valueOf(actionDescriptor.getHttpMethod());

        // Link's expand method removes non-required variables from URL
        String actionUrl = affordance.expand()
                .getHref();
        beginForm(OptionalAttributes.attr("action", actionUrl)
                .and("method", getHtmlConformingHttpMethod(httpMethod))
                .and("name", formName));
        write("<h4>");
        write("Form " + formName);
        write("</h4>");

        writeHiddenHttpMethodField(httpMethod);
        // build the form
        if (actionDescriptor.hasRequestBody()) {
            AnnotatedParameter requestBody = actionDescriptor.getRequestBody();
            Class<?> parameterType = requestBody.getParameterType();
            recurseBeanProperties(new ArrayDeque<String>(), parameterType, actionDescriptor, requestBody, requestBody
                    .getCallValue());
        } else {
            Collection<String> requestParams = actionDescriptor.getRequestParamNames();
            for (String requestParamName : requestParams) {
                AnnotatedParameter actionInputParameter = actionDescriptor.getAnnotatedParameter(requestParamName);

                // TODO support list and matrix parameters?
                // TODO can recurseBeanProperties handle this or can we share code here?
                Object[] possibleValues = actionInputParameter.getPossibleValues(actionDescriptor);
                if (possibleValues.length > 0) {
                    if (actionInputParameter.isArrayOrCollection()) {
                        appendSelectMulti(requestParamName, requestParamName, possibleValues, actionInputParameter,
                                actionInputParameter);
                    } else {
                        appendSelectOne(requestParamName, requestParamName, possibleValues, actionInputParameter,
                                actionInputParameter);
                    }
                } else {
                    if (actionInputParameter.isArrayOrCollection()) {
                        // TODO support for free list input?
                        Object[] callValues = actionInputParameter.getCallValues();
                        int items = callValues.length;
                        for (int i = 0; i < items; i++) {
                            Object value;
                            if (i < callValues.length) {
                                value = callValues[i];
                            } else {
                                value = null;
                            }
                            appendInput(requestParamName, requestParamName, actionInputParameter,
                                    actionInputParameter, value);
                        }
                    } else {
                        String callValueFormatted = actionInputParameter.getCallValueFormatted();
                        appendInput(requestParamName, requestParamName, actionInputParameter, actionInputParameter,
                                callValueFormatted);
                    }
                }
            }
        }
        inputButton(Type.SUBMIT, capitalize(httpMethod.name()
                .toLowerCase()));
        endForm();
    }

    private void appendLinkWithoutActionDescriptor(Link link) throws IOException {
        if (link.isTemplated()) {
            // TODO ensure that template expansion takes place for base uri
            Link expanded = link.expand(); // remove query variables
            beginForm(OptionalAttributes.attr("action", expanded.getHref())
                    .and("method", "GET"));
            List<TemplateVariable> variables = link.getVariables();
            for (TemplateVariable variable : variables) {
                String variableName = variable.getName();
                String label = variable.hasDescription() ? variable.getDescription() : variableName;
                writeLabelWithDoc(label, variableName, null); // no documentation url
                input(variableName, Type.TEXT);
            }
        } else {
            String rel = link.getRel();
            String title = (rel != null ? rel : link.getHref());
            // TODO: write link instead of anchor here?
            writeAnchor(OptionalAttributes.attr("href", link.getHref()), title);
            writeAnchor(OptionalAttributes.attr("href", link.getHref())
                    .and("rel", link.getRel()), title);
        }
    }

    /**
     * Classic submit or reset button.
     *
     * @param type
     *         submit or reset
     * @param value
     *         caption on the button
     * @throws IOException
     */
    private void inputButton(Type type, String value) throws IOException {
        write("<input type=\"");
        write(type.toString());
        write("\" ");
        write("value");
        write("=");
        quote();
        write(value);
        quote();
        write("/>");
    }

    private void input(String fieldName, Type type, OptionalAttributes attributes) throws IOException {
        write("<input name=\"");
        write(fieldName);
        write("\" type=\"");
        write(type.toString());
        write("\" class=\"");
        write(formControlClass);
        write("\" ");
        writeAttributes(attributes);
        write("/>");
    }

    private void input(String fieldName, Type type) throws IOException {
        input(fieldName, type, OptionalAttributes.attr());
    }

    private void beginLabel(OptionalAttributes attributes) throws IOException {
        write("<label");
        writeAttributes(attributes);
        endTag();
    }

    private void endLabel() throws IOException {
        write("</label>");
    }


    private void beginForm(OptionalAttributes attrs) throws IOException {
        write("<form class=\"well\" ");
        writeAttributes(attrs);
        write(">");
    }

    private void writeAttributes(OptionalAttributes attrs) throws IOException {
        Map<String, String> attributes = attrs.build();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            write(" ");
            write(entry.getKey());
            write("=");
            quote();
            write(entry.getValue());
            quote();
        }
    }

    private void quote() throws IOException {
        write("\"");
    }

    private void endForm() throws IOException {
        write("</form>");
    }

    public void beginAnchor(OptionalAttributes attrs) throws IOException {
        write("<a ");
        writeAttributes(attrs);
        endTag();
    }

    public void endAnchor() throws IOException {
        write("</a>");
    }

    private void writeAnchor(OptionalAttributes attrs, String value) throws IOException {
        beginAnchor(attrs);
        write(value);
        endAnchor();
    }


    public static String capitalize(String name) {
        if (name != null && name.length() != 0) {
            char[] chars = name.toCharArray();
            chars[0] = Character.toUpperCase(chars[0]);
            return new String(chars);
        } else {
            return name;
        }
    }

    private void writeHiddenHttpMethodField(RequestMethod httpMethod) throws IOException {
        switch (httpMethod) {
            case GET:
            case POST:
                break;
            default:
                input(methodParam, Type.HIDDEN, OptionalAttributes.attr("value", httpMethod.name()));
        }
    }

    private String getHtmlConformingHttpMethod(RequestMethod requestMethod) {
        String ret;
        switch (requestMethod) {
            case GET:
            case POST:
                ret = requestMethod.name();
                break;
            default:
                ret = RequestMethod.POST.name();
        }
        return ret;
    }

    private Constructor findDefaultCtor(Constructor[] constructors) {
        // TODO duplicate on HtmlResourceMessageConverter
        Constructor constructor = null;
        for (Constructor ctor : constructors) {
            if (ctor.getParameterTypes().length == 0) {
                constructor = ctor;
            }
        }
        return constructor;
    }

    private Constructor findJsonCreator(Constructor[] constructors) {
        // TODO duplicate on HtmlResourceMessageConverter
        Constructor constructor = null;
        for (Constructor ctor : constructors) {
            if (AnnotationUtils.getAnnotation(ctor, JsonCreator.class) != null) {
                constructor = ctor;
                break;
            }
        }
        return constructor;
    }

    /**
     * Renders input fields for bean properties of bean to add or update or patch.
     *
     * @param beanType
     *         to render
     * @param actionDescriptor
     *         which describes the method
     * @param rootInputParameter
     *         which expects the bean as request body
     * @param currentCallValue
     *         sample call value
     * @throws IOException
     */
    private void recurseBeanProperties(Deque<String> propertyPath, Class<?> beanType, ActionDescriptor
            actionDescriptor, AnnotatedParameter
            rootInputParameter, Object currentCallValue) throws IOException {
        // TODO support Option provider by other method args?
        final BeanInfo beanInfo = getBeanInfo(beanType);
        final PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        // TODO collection and map

        // TODO: do not add two inputs for setter and ctor


        // TODO almost duplicate of HtmlResourceMessageConverter.recursivelyCreateObject

        // Convention:
        // - render only writable properties on PUT request body bean
        // - render @JsonCreator constructor parameters of POST request body bean annotated as @JsonProperty
        // - for more fine-grained control use @Input readOnly, hidden, include, exclude attributes on handler methods
        // TODO: consider if the convention for POST and PUT still makes sense now that we have full control via @Input
        if (RequestMethod.POST == RequestMethod.valueOf(actionDescriptor.getHttpMethod())) {
            try {
                // TODO also use writable properties during POST
                Constructor[] constructors = beanType.getConstructors();
                // find default ctor
                Constructor constructor = findDefaultCtor(constructors);
                // find ctor with JsonCreator ann
                if (constructor == null) {
                    constructor = findJsonCreator(constructors);
                }
                Assert.notNull(constructor, "no default constructor or JsonCreator found for type " + beanType
                        .getName());
                int parameterCount = constructor.getParameterTypes().length;
                if (parameterCount > 0) {
                    Annotation[][] annotationsOnParameters = constructor.getParameterAnnotations();

                    Class[] parameters = constructor.getParameterTypes();
                    int paramIndex = 0;
                    for (Annotation[] annotationsOnParameter : annotationsOnParameters) {
                        for (Annotation annotation : annotationsOnParameter) {
                            if (JsonProperty.class == annotation.annotationType()) {
                                JsonProperty jsonProperty = (JsonProperty) annotation;
                                // TODO use required attribute of JsonProperty?
                                String paramName = jsonProperty.value();

                                propertyPath.add(paramName);

                                Class parameterType = parameters[paramIndex];

                                // TODO duplicate below for PropertyDescriptors caused by disjunct ctor and method
                                // inheritance before Java 8
                                if (DataType.isSingleValueType(parameterType)) {

                                    Object propertyValue = getPropertyOrFieldValue(currentCallValue, paramName);

                                    ActionInputParameter constructorParamInputParameter = new ActionInputParameter
                                            (new MethodParameter(constructor, paramIndex), propertyValue);

                                    final Object[] possibleValues =
                                            rootInputParameter.getPossibleValues(constructor, paramIndex,
                                                    actionDescriptor);
                                    if (rootInputParameter.isIncluded(paramName)
                                            && !rootInputParameter.isExcluded(paramName)) {
                                        if (possibleValues.length > 0 && !rootInputParameter.isHidden(paramName)) {
                                            if (rootInputParameter.isArrayOrCollection()) {
                                                // TODO multiple formatted callvalues
                                                collectionToDelimitedString(propertyPath, ".");
                                                appendSelectMulti(collectionToDelimitedString
                                                                (propertyPath, "."), paramName, possibleValues,
                                                        rootInputParameter,
                                                        constructorParamInputParameter);
                                            } else {
                                                appendSelectOne(collectionToDelimitedString(propertyPath,
                                                                "."), paramName, possibleValues, rootInputParameter,
                                                        constructorParamInputParameter);
                                            }
                                        } else {
                                            appendInput(collectionToDelimitedString(propertyPath, "."),
                                                    paramName, rootInputParameter, constructorParamInputParameter,
                                                    constructorParamInputParameter.getCallValue());
                                        }
                                    }
                                } else if (DataType.isArrayOrCollection(parameterType)) {
                                    Object[] callValues = rootInputParameter.getCallValues();
                                    int items = callValues.length;
                                    for (int i = 0; i < items; i++) {
                                        Object value;
                                        if (i < callValues.length) {
                                            value = callValues[i];
                                        } else {
                                            value = null;
                                        }
                                        recurseBeanProperties(propertyPath, rootInputParameter.getParameterType(),
                                                actionDescriptor, rootInputParameter, value);
                                    }
                                } else {
                                    beginDiv();
                                    write(paramName + ":");
                                    Object propertyValue = getPropertyOrFieldValue(currentCallValue, paramName);
                                    recurseBeanProperties(propertyPath, parameterType, actionDescriptor,
                                            rootInputParameter,
                                            propertyValue);
                                    endDiv();
                                }
                                propertyPath.removeLast();
                                paramIndex++; // increase for each @JsonProperty
                            }
                        }
                    }
                    Assert.isTrue(parameters.length == paramIndex,
                            "not all constructor arguments of @JsonCreator " + constructor.getName() +
                                    " are annotated with @JsonProperty");
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to write input fields for constructor", e);
            }
        } else { // non-POST

            // TODO non-writable properties and public fields: make sure the inputs are part of a form
            // write input field for every setter
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                final Method writeMethod = propertyDescriptor.getWriteMethod();
                if (writeMethod == null) {
                    continue;
                }
                final Class<?> propertyType = propertyDescriptor.getPropertyType();

                String propertyName = propertyDescriptor.getName();

                propertyPath.add(propertyName);

                if (DataType.isSingleValueType(propertyType)) {

                    Object propertyValue = getPropertyOrFieldValue(currentCallValue, propertyName);
                    MethodParameter methodParameter = new MethodParameter(propertyDescriptor.getWriteMethod(), 0);
                    ActionInputParameter propertySetterInputParameter = new ActionInputParameter(methodParameter,
                            propertyValue);
                    final Object[] possibleValues = rootInputParameter.getPossibleValues(propertyDescriptor
                                    .getWriteMethod(), 0,
                            actionDescriptor);
                    if (rootInputParameter.isIncluded(propertyName)
                            && !rootInputParameter.isExcluded(propertyName)) {
                        if (possibleValues.length > 0 && !rootInputParameter.isHidden(propertyName)) {
                            if (rootInputParameter.isArrayOrCollection()) {
                                // TODO multiple formatted callvalues
                                appendSelectMulti(collectionToDelimitedString(propertyPath, "."),
                                        propertyName, possibleValues, rootInputParameter,
                                        propertySetterInputParameter);
                            } else {
                                appendSelectOne(collectionToDelimitedString(propertyPath, "."),
                                        propertyName, possibleValues, rootInputParameter,
                                        propertySetterInputParameter);
                            }
                        } else {
                            //String callValueFormatted = rootInputParameter.getCallValueFormatted();
                            appendInput(collectionToDelimitedString(propertyPath, "."), propertyName,
                                    rootInputParameter, propertySetterInputParameter,
                                    propertySetterInputParameter
                                            .getCallValue());
                        }
                    }
                } else if (rootInputParameter.isArrayOrCollection()) {
                    Object[] callValues = rootInputParameter.getCallValues();
                    int items = callValues.length;
                    for (int i = 0; i < items; i++) {
                        Object value;
                        if (i < callValues.length) {
                            value = callValues[i];
                        } else {
                            value = null;
                        }
                        recurseBeanProperties(propertyPath, rootInputParameter.getParameterType(), actionDescriptor,
                                rootInputParameter, value);
                    }
                } else {
                    beginDiv();
                    write(propertyName + ":"); // caption for nested bean
                    Object propertyValue = PropertyUtils.getPropertyValue(currentCallValue, propertyDescriptor);
                    recurseBeanProperties(propertyPath, propertyType, actionDescriptor, rootInputParameter,
                            propertyValue);
                    endDiv();
                }
                propertyPath.removeLast();
            }
        }
    }

    // TODO move to PropertyUtil and remove current method for propertyDescriptors, cache search results
    private Object getPropertyOrFieldValue(Object currentCallValue, String propertyOrFieldName) {
        if (currentCallValue == null) {
            return null;
        }
        Object propertyValue = getBeanPropertyValue(currentCallValue, propertyOrFieldName);
        if (propertyValue == null) {
            propertyValue = getFieldValue(currentCallValue, propertyOrFieldName);
        }
        return propertyValue;
    }

    private Object getFieldValue(Object currentCallValue, String fieldName) {
        try {
            Class<?> beanType = currentCallValue.getClass();
            Object propertyValue = null;
            Field[] fields = beanType.getFields();
            for (Field field : fields) {
                if (fieldName.equals(field.getName())) {
                    propertyValue = field.get(currentCallValue);
                    break;
                }
            }
            return propertyValue;
        } catch (Exception e) {
            throw new RuntimeException("Failed to read field " + fieldName + " from " + currentCallValue.toString(), e);
        }
    }


    // TODO move to PropertyUtil and remove current method for propertyDescriptors
    private Object getBeanPropertyValue(Object currentCallValue, String paramName) {
        if (currentCallValue == null) {
            return null;
        }
        try {
            Object propertyValue = null;
            BeanInfo info = Introspector.getBeanInfo(currentCallValue.getClass());
            PropertyDescriptor[] pds = info.getPropertyDescriptors();
            for (PropertyDescriptor pd : pds) {
                if (paramName.equals(pd.getName())) {
                    Method readMethod = pd.getReadMethod();
                    if (readMethod != null) {
                        propertyValue = readMethod.invoke(currentCallValue);
                    }
                    break;
                }
            }
            return propertyValue;
        } catch (Exception e) {
            throw new RuntimeException("Failed to read property " + paramName + " from " + currentCallValue.toString
                    (), e);
        }
    }

    private BeanInfo getBeanInfo(Class<?> beanType) {
        try {
            return Introspector.getBeanInfo(beanType);
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }

    private void appendInput(String propertyPath, String requestParamName, AnnotatedParameter rootInputParameter,
                             AnnotatedParameter
            actionInputParameter, Object value) throws
            IOException {
        if (actionInputParameter.isRequestBody()) { // recurseBeanProperties does that
            throw new IllegalArgumentException("cannot append input field for requestBody");
        }
        Type htmlInputFieldType = actionInputParameter.getHtmlInputFieldType();
        Assert.notNull(htmlInputFieldType);

        OptionalAttributes attrs = OptionalAttributes.attr("value", value); // handles null value
        if (Type.HIDDEN.equals(htmlInputFieldType) || rootInputParameter.isHidden(propertyPath)) {
            input(propertyPath, Type.HIDDEN, attrs);
        } else {
            beginDiv(OptionalAttributes.attr("class", formGroupClass));
            String documentationUrl = documentationProvider.getDocumentationUrl(actionInputParameter, value);

            if (actionInputParameter.hasInputConstraints()) {
                writeLabelWithDoc(requestParamName, propertyPath, documentationUrl);
                for (Map.Entry<String, Object> entry : actionInputParameter.getInputConstraints()
                        .entrySet()) {
                    attrs.and(entry.getKey(), entry.getValue()
                            .toString());
                }
                if (rootInputParameter.isReadOnly(propertyPath)) {
                    attrs.and("readonly", "readonly");
                }
                input(propertyPath, htmlInputFieldType, attrs);
            } else {
                writeLabelWithDoc(requestParamName, propertyPath, documentationUrl);
                input(propertyPath, htmlInputFieldType, attrs);
            }
            endDiv();
        }

    }

    private void writeLabelWithDoc(String label, String fieldName, String documentationUrl) throws IOException {
        beginLabel(OptionalAttributes.attr("for", fieldName)
                .and("class", controlLabelClass));
        if (documentationUrl == null) {
            write(label);
        } else {
            beginAnchor(XhtmlWriter.OptionalAttributes.attr("href", documentationUrl)
                    .and("title", documentationUrl));
            write(label);
            endAnchor();
        }
        endLabel();
    }


    private void appendSelectOne(String propertyPath, String requestParamName, Object[] possibleValues,
                                 AnnotatedParameter
            rootInputParameter, AnnotatedParameter actionInputParameter)
            throws IOException {
        beginDiv(OptionalAttributes.attr("class", formGroupClass));
        Object callValue = actionInputParameter.getCallValue();
        String documentationUrl = documentationProvider.getDocumentationUrl(actionInputParameter, callValue);
        writeLabelWithDoc(requestParamName, propertyPath, documentationUrl);

        OptionalAttributes attrs = OptionalAttributes.attr("class", formControlClass);
        if (rootInputParameter.isReadOnly(propertyPath)) {
            attrs.and("readonly", "readonly");
        }

        beginSelect(propertyPath, propertyPath, possibleValues.length, attrs);
        for (Object possibleValue : possibleValues) {
            if (possibleValue.equals(callValue)) {
                option(possibleValue.toString(), attr("selected", "selected"));
            } else {
                option(possibleValue.toString());
            }
        }
        endSelect();

        endDiv();
    }


    private void appendSelectMulti(String propertyPath, String requestParamName, Object[] possibleValues,
                                   AnnotatedParameter
            rootInputParameter, AnnotatedParameter actionInputParameter) throws IOException {
        beginDiv(OptionalAttributes.attr("class", formGroupClass));
        Object[] actualValues = actionInputParameter.getCallValues();
        final Object aCallValue;
        if (actualValues.length > 0) {
            aCallValue = actualValues[0];
        } else {
            aCallValue = null;
        }
        String documentationUrl = documentationProvider.getDocumentationUrl(actionInputParameter, aCallValue);
        writeLabelWithDoc(requestParamName, propertyPath, documentationUrl);
        OptionalAttributes attrs = OptionalAttributes.attr("multiple", "multiple")
                .and("class", formControlClass);
        if (rootInputParameter.isReadOnly(propertyPath)) {
            attrs.and("readonly", "readonly");
        }
        beginSelect(propertyPath, propertyPath, possibleValues.length,
                attrs);
        for (Object possibleValue : possibleValues) {
            if (arrayContains(actualValues, possibleValue)) {
                option(possibleValue.toString(), attr("selected", "selected"));
            } else {
                option(possibleValue.toString());
            }
        }
        endForm();
        endDiv();
    }

    private void option(String option) throws IOException {
        option(option, attr());
    }

    private void option(String option, OptionalAttributes attr) throws IOException {
        // <option selected='selected'>%s</option>
        beginTag("option");
        writeAttributes(attr);
        endTag();
        write(option);
        write("</option>");
    }

    private void beginTag(String tag) throws IOException {
        write("<");
        write(tag);

    }

    private void endTag() throws IOException {
        write(">");
    }

    private void beginSelect(String name, String id, int size) throws IOException {
        beginSelect(name, id, size, attr());
    }

    private void beginSelect(String name, String id, int size, OptionalAttributes attrs) throws IOException {
        beginTag("select");
        write(" name=");
        quote(name);
        write(" id=");
        quote(id);
        //write(" size=");
        //quote(Integer.toString(size));
        writeAttributes(attrs);
        endTag();
    }

    private void endSelect() throws IOException {
        write("</select>");
    }

    private void quote(String s) throws IOException {
        quote();
        write(s);
        quote();
    }


    private boolean arrayContains(Object[] values, Object value) {
        for (int i = 0; i < values.length; i++) {
            Object item = values[i];
            if (item.equals(value)) {
                return true;
            }
        }
        return false;
    }
}
