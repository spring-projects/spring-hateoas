package org.springframework.hateoas.util;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

public class LinkTemplateUtilsUnitTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testGetClassLevelMappingSpring() throws Exception {
		String classLevelMapping = LinkTemplateUtils.getClassLevelMapping(FooController.class, RequestMapping.class);
		assertEquals("/bar", classLevelMapping);
	}

	@Test
	public void testGetClassLevelMappingJaxRs() throws Exception {
		String classLevelMapping = LinkTemplateUtils.getClassLevelMapping(FooService.class, Path.class);
		assertEquals("/bar", classLevelMapping);
	}

	@Test
	public void testCreateLinkTemplateSpring() throws SecurityException, NoSuchMethodException {
		Method method = this.getClass().getDeclaredMethod("showBarSpring", String.class, Long.class);
		LinkTemplate<PathVariable, RequestParam> linkTemplate = LinkTemplateUtils.createLinkTemplate("/foo", method,
				RequestMapping.class, PathVariable.class, RequestParam.class);
		assertEquals("/foo/bar/{baz}", linkTemplate.getLinkTemplate());
		List<AnnotatedParam<PathVariable>> pathVariables = linkTemplate.getPathVariables();
		assertEquals(1, pathVariables.size());
		assertEquals(String.class, pathVariables.get(0).paramType);
		List<AnnotatedParam<RequestParam>> requestParams = linkTemplate.getRequestParams();
		assertEquals(Long.class, requestParams.get(0).paramType);
	}

	@Test
	public void testCreateLinkTemplateJaxRs() throws SecurityException, NoSuchMethodException {
		Method method = this.getClass().getDeclaredMethod("showBarJaxRs", String.class, Integer.class);
		LinkTemplate<PathParam, QueryParam> linkTemplate = LinkTemplateUtils.createLinkTemplate("/foo", method, Path.class,
				PathParam.class, QueryParam.class);
		assertEquals("/foo/bar/{baz}", linkTemplate.getLinkTemplate());
		List<AnnotatedParam<PathParam>> pathVariables = linkTemplate.getPathVariables();
		assertEquals(1, pathVariables.size());
		assertEquals(String.class, pathVariables.get(0).paramType);
		List<AnnotatedParam<QueryParam>> requestParams = linkTemplate.getRequestParams();
		assertEquals(Integer.class, requestParams.get(0).paramType);
	}

	@RequestMapping("/bar")
	class FooController {

	}

	@Path("bar")
	class FooService {

	}

	/** Test method with mappings */
	@RequestMapping(value = "/bar/{baz}")
	HttpEntity<String> showBarSpring(@PathVariable("baz") String baz, @RequestParam("id") Long id) {
		return null;

	}

	/** Test method with mappings */
	@Path("bar/{baz}")
	HttpEntity<String> showBarJaxRs(@PathParam("baz") String baz, @QueryParam("id") Integer id) {
		return null;

	}

}
