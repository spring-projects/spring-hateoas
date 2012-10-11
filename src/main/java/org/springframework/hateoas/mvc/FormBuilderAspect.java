package org.springframework.hateoas.mvc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Component
@Aspect
public class FormBuilderAspect {

	String searchPerson = "searchPerson";
	String backRel = "back";
	// what comes from the @requestMapping
	// -method, value, inputs with their names, requestParam value, formName?
	// backRel?

	/** expects title */
	public static final String HTML_START = "" + //
			"<?xml version='1.0' encoding='UTF-8' ?>" + // formatter
			"<!DOCTYPE html>" + //
			"<html xmlns='http://www.w3.org/1999/xhtml'>" + //
			"  <head>" + //
			"    <title>%s</title>" + //
			"  </head>" + //
			"  <body>";

	/** expects action url, form name, form h1 */
	public static final String FORM_START = "" + //
			"    <form action='%s' name='%s'>" + //
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

	@Pointcut("execution(@org.springframework.hateoas.mvc.Form * *(..))")
	public void form(Form form) {

	}

	@Around("@annotation(form)")
	public Object buildForm(ProceedingJoinPoint pjp, Form form) throws Throwable {

		Object[] args = pjp.getArgs();

		boolean argsAreNull = true;
		for (Object object : args) {
			if (object != null) {
				argsAreNull = false;
				break;
			}
		}

		if (!argsAreNull) {
			return pjp.proceed(args);
		} else {
			MethodSignature methodSignature = (MethodSignature) pjp.getSignature();

			String methodName = methodSignature.getName();
			Class<?>[] methodParameterTypes = methodSignature.getParameterTypes();

			Class<?> withinType = pjp.getSourceLocation().getWithinType();
			Method method = withinType.getMethod(methodName, methodParameterTypes);

			Annotation[][] paramAnnotations = method.getParameterAnnotations();

			List<RequestParam> requestParamAnnotations = new ArrayList<RequestParam>();
			List<Class<?>> requestParamArgs = new ArrayList<Class<?>>();
			for (int i = 0; i < paramAnnotations.length; i++) {
				Class<?> argType = methodParameterTypes[i].getClass();
				for (int j = 0; j < paramAnnotations[i].length; j++) {
					Annotation paramAnnotation = paramAnnotations[i][j];
					Class<? extends Annotation> annotationType = paramAnnotation.annotationType();
					if (RequestParam.class == annotationType) {
						requestParamAnnotations.add((RequestParam) paramAnnotation);
						requestParamArgs.add(argType);
					}
				}
			}

			RequestMapping classRequestMapping = AnnotationUtils.findAnnotation(withinType, RequestMapping.class);
			RequestMapping methodRequestMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
			String classLevelMapping = classRequestMapping.value()[0];
			String methodLevelMapping = methodRequestMapping.value()[0];

			String action = classLevelMapping + methodLevelMapping;

			// form name
			String formName = form.value();

			StringBuilder sb = new StringBuilder();
			sb.append(String.format(HTML_START, formName));
			for (int i = 0; i < requestParamAnnotations.size(); i++) {
				String formH1 = "Form " + formName;
				sb.append(String.format(FORM_START, action, formName, formH1));

				RequestParam annotation = requestParamAnnotations.get(i);
				String requestParamName = annotation.value();
				Class<?> requestParamArg = requestParamArgs.get(i);
				String inputFieldType = getInputFieldType(requestParamArg);
				String fieldLabel = requestParamName + ": ";
				// TODO support list and matrix parameters
				sb.append(String.format(FORM_INPUT, fieldLabel, inputFieldType, requestParamName));
			}
			sb.append(FORM_END).append(HTML_END);

      HttpHeaders headers = new HttpHeaders();
      headers.set("Content-Type", "text/html");
      HttpEntity<String> httpEntity = new HttpEntity<String>(sb.toString(), headers);
      return httpEntity;

		}
	}

	private String getInputFieldType(Class<?> requestParamArg) {
		// TODO number, date, text... depending on requestParamArg
		return "text";
	}
}
