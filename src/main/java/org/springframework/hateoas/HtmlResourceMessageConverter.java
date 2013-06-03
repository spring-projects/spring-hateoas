package org.springframework.hateoas;

import java.io.IOException;
import java.util.Map.Entry;

import org.springframework.hateoas.action.ActionDescriptor;
import org.springframework.hateoas.action.Input;
import org.springframework.hateoas.action.Type;
import org.springframework.hateoas.mvc.MethodParameterValue;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.FileCopyUtils;

/**
 * Message converter which converts one ActionDescriptor or an array of ActionDescriptor items to an HTML page
 * containing one form per ActionDescriptor.
 *
 * Add the following to your spring configuration to enable this converter:
 *
 * <pre>
 *   &lt;bean
 *     class="org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter"&gt;
 *     &lt;property name="messageConverters"&gt;
 *       &lt;list&gt;
 *         &lt;ref bean="jsonConverter" /&gt;
 *         &lt;ref bean="htmlFormMessageConverter" /&gt;
 *       &lt;/list&gt;
 *     &lt;/property&gt;
 *   &lt;/bean&gt;
 *
 *   &lt;bean id="htmlFormMessageConverter" class="org.springframework.hateoas.HtmlResourceMessageConverter"&gt;
 *     &lt;property name="supportedMediaTypes" value="text/html" /&gt;
 *   &lt;/bean&gt;
 * </pre>
 *
 * @author Dietrich Schulten
 *
 */
public class HtmlResourceMessageConverter extends AbstractHttpMessageConverter<Object> {

	/** expects title */
	public static final String HTML_START = "" + //
			// "<?xml version='1.0' encoding='UTF-8' ?>" + // formatter
			"<!DOCTYPE html>" + //
			"<html xmlns='http://www.w3.org/1999/xhtml'>" + //
			"  <head>" + //
			"    <title>%s</title>" + //
			"  </head>" + //
			"  <body>";

	/** expects action url, form name, form method, form h1 */
	public static final String FORM_START = "" + //
			"    <form action='%s' name='%s' method='%s'>" + //
			"      <h1>%s</h1>"; //

	/** expects field label, input field type and field name */
	public static final String FORM_INPUT = "" + //
			"      <label>%s<input type='%s' name='%s' value='%s' /></label>";

	/** closes the form */
	public static final String FORM_END = "" + //
			"      <input type='submit' value='Submit' />" + //
			"    </form>";
	public static final String HTML_END = "" + //
			"  </body>" + //
			"</html>";

	@Override
	protected boolean supports(Class<?> clazz) {
		final boolean ret;
		if (ActionDescriptor.class == clazz || ActionDescriptor[].class == clazz) {
			ret = true;
		} else {
			ret = false;
		}
		return ret;
	}

	@Override
	protected Object readInternal(Class<? extends Object> clazz, HttpInputMessage inputMessage) throws IOException,
			HttpMessageNotReadableException {
		return new Object();
	}

	@Override
	protected void writeInternal(Object t, HttpOutputMessage outputMessage) throws IOException,
			HttpMessageNotWritableException {

		StringBuilder sb = new StringBuilder();
		sb.append(String.format(HTML_START, "Input Data"));

		if (t instanceof ActionDescriptor[]) {
			ActionDescriptor[] descriptors = (ActionDescriptor[]) t;
			for (ActionDescriptor actionDescriptor : descriptors) {
				appendForm(sb, actionDescriptor);
			}
		} else {
			ActionDescriptor actionDescriptor = (ActionDescriptor) t;
			appendForm(sb, actionDescriptor);
		}
		sb.append(HTML_END);
		FileCopyUtils.copy(sb.toString().getBytes("UTF-8"), outputMessage.getBody());

	}

	private void appendForm(StringBuilder sb, ActionDescriptor actionDescriptor) {
		String action = actionDescriptor.getRelativeActionLink();
		String formName = actionDescriptor.getResourceName();

		String formH1 = "Form " + formName;
		sb.append(String.format(FORM_START, action, formName, actionDescriptor.getHttpMethod().toString(), formH1));

		// build the form
		for (Entry<String, MethodParameterValue> requestParam : actionDescriptor.getRequestParams().entrySet()) {

			String requestParamName = requestParam.getKey();
			MethodParameterValue methodParameterValue = requestParam.getValue();
			String inputFieldType = getInputFieldType(methodParameterValue);
			String fieldLabel = requestParamName + ": ";
			// TODO support list and matrix parameters?
			// TODO support RequestBody mapped by object marshaler? Look at bean properties in that case instead of
			// RequestParam arguments.
			// TODO support valid value ranges, possible values, value constraints?
			Object invocationValue = methodParameterValue.getCallValue();
			sb.append(String.format(FORM_INPUT, fieldLabel, inputFieldType, requestParamName, invocationValue == null ? ""
					: invocationValue.toString()));
		}
		sb.append(FORM_END);
	}

	private String getInputFieldType(MethodParameterValue methodParameterValue) {
		final String ret;
		Input inputAnnotation = methodParameterValue.getParameterAnnotation(Input.class);
		if (inputAnnotation != null) {
			ret = inputAnnotation.value().toString();
		} else {
			Class<?> parameterType = methodParameterValue.getParameterType();
			if (Number.class.isAssignableFrom(parameterType)) {
				ret = Type.NUMBER.toString();
			} else {
				ret = Type.TEXT.toString();
			}
		}
		return ret;
	}

}
