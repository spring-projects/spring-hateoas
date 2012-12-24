package org.springframework.hateoas.action;

import java.io.IOException;
import java.util.Map.Entry;

import org.springframework.hateoas.mvc.MethodParameterValue;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.FileCopyUtils;

/**
 * Message converter which converts one FormDescriptor or an array of FormDescriptor items to an HTML page containing
 * one form per FormDescriptor.
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
 *   &lt;bean id="htmlFormMessageConverter" class="org.springframework.hateoas.HtmlFormMessageConverter"&gt;
 *     &lt;property name="supportedMediaTypes" value="text/html" /&gt;
 *   &lt;/bean&gt;
 * </pre>
 * 
 * @author Dietrich Schulten
 * 
 */
public class HtmlFormMessageConverter extends AbstractHttpMessageConverter<Object> {

	String searchPerson = "searchPerson";
	String backRel = "back";

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
		String action = actionDescriptor.getActionLink();
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
		// TODO HTML5 input types: number, date, text... depending on requestParamArg
		final String ret;
		if (methodParameterValue.hasParameterAnnotation(Hidden.class)) {
			// TODO support hidden input fields
			ret = "hidden";
		} else {
			// methodParameterValue.getParameterType()
			ret = "text";
		}
		return ret;
	}

}
