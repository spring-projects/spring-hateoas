package org.springframework.hateoas;

import java.io.IOException;
import java.util.Map.Entry;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.FileCopyUtils;

/**
 * Message converter which converts one ResourceDescriptor or an array of ResourceDescriptor items to an HTML page
 * containing one form per ResourceDescriptor.
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
			"<?xml version='1.0' encoding='UTF-8' ?>" + // formatter
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
			"      <label>%s<input type='%s' name='%s' /></label>";

	/** closes the form */
	public static final String FORM_END = "" + //
			"      <input type='submit' value='Find' />" + //
			"    </form>";
	public static final String HTML_END = "" + //
			"  </body>" + //
			"</html>";

	@Override
	protected boolean supports(Class<?> clazz) {
		final boolean ret;
		if (FormDescriptor.class == clazz || FormDescriptor[].class == clazz) {
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

		if (t instanceof FormDescriptor[]) {
			FormDescriptor[] descriptors = (FormDescriptor[]) t;
			for (FormDescriptor formDescriptor : descriptors) {
				appendForm(sb, formDescriptor);
			}
		} else {
			FormDescriptor formDescriptor = (FormDescriptor) t;
			appendForm(sb, formDescriptor);
		}
		sb.append(HTML_END);
		FileCopyUtils.copy(sb.toString().getBytes("UTF-8"), outputMessage.getBody());

	}

	private void appendForm(StringBuilder sb, FormDescriptor formDescriptor) {
		String action = formDescriptor.getLinkTemplate();
		String formName = formDescriptor.getResourceName();

		String formH1 = "Form " + formName;
		sb.append(String.format(FORM_START, action, formName, formDescriptor.getHttpMethod().toString(), formH1));

		// build the form
		for (Entry<String, Class<?>> entry : formDescriptor.getRequestParams().entrySet()) {

			String requestParamName = entry.getKey();
			Class<?> requestParamArg = entry.getValue();
			String inputFieldType = getInputFieldType(requestParamArg);
			String fieldLabel = requestParamName + ": ";
			// TODO support list and matrix parameters
			sb.append(String.format(FORM_INPUT, fieldLabel, inputFieldType, requestParamName));
		}
		sb.append(FORM_END);
	}

	private String getInputFieldType(Class<?> requestParamArg) {
		// TODO HTML5 input types: number, date, text... depending on requestParamArg
		return "text";
	}

}
