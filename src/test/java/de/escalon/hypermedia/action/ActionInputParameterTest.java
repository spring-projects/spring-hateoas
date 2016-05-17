package de.escalon.hypermedia.action;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.escalon.hypermedia.spring.SpringActionDescriptor;
import de.escalon.hypermedia.spring.SpringActionInputParameter;

/**
 * Created by Dietrich on 16.05.2015.
 */
public class ActionInputParameterTest {

	@RequestMapping("/reviews")
	public static class DummyController {

		@Action("ReviewAction")
		@RequestMapping(value = "/{rating}", params = "reviewBody", method = RequestMethod.POST)
		public @ResponseBody ResponseEntity<Void> addReview(
				@PathVariable @Select({ "excellent", "mediocre", "abysmal" }) String rating,
				@RequestParam(defaultValue = "excellent") @Input(minLength = 5, pattern = "[ -~]*") String reviewBody) {
			return null;
		}

		@Action("ReviewAction")
		@RequestMapping(params = "searchTerms", method = RequestMethod.GET)
		public @ResponseBody ResponseEntity<Object> queryReviewsRated(
				@RequestParam @Select({ "excellent", "mediocre", "abysmal" }) List<String> searchTerms) {
			return null;
		}
	}

	@Before
	public void setUp() {

	}

	@Test
	public void testAddReviewPathVariableReviewBody() throws NoSuchMethodException {
		Method addReview = DummyController.class.getMethod("addReview", String.class, String.class);
		MethodParameter rating = new MethodParameter(addReview, 0);
		MethodParameter reviewBody = new MethodParameter(addReview, 1);

		SpringActionInputParameter actionInputParameter = new SpringActionInputParameter(reviewBody, "yada, yada");

		assertTrue(actionInputParameter.hasValue());
		assertEquals("yada, yada", actionInputParameter.getValue());

		assertEquals(DummyController.class, actionInputParameter.getDeclaringClass());

		assertTrue(actionInputParameter.hasInputConstraints());
		assertEquals("[ -~]*", actionInputParameter.getInputConstraints().get("pattern"));
		assertEquals(5, actionInputParameter.getInputConstraints().get("minLength"));

		assertEquals("reviewBody", actionInputParameter.getParameterName());
		assertEquals(String.class, actionInputParameter.getParameterType());
		assertEquals(0,
				actionInputParameter.getPossibleValues(new SpringActionDescriptor("post", RequestMethod.POST.name())).length);
		assertEquals(Type.TEXT, actionInputParameter.getHtmlInputFieldType());
		assertNull(actionInputParameter.getRequestHeaderName());

		assertFalse(actionInputParameter.isArrayOrCollection());
		assertFalse(actionInputParameter.isRequestBody());
		assertFalse(actionInputParameter.isRequestHeader());
		assertFalse(actionInputParameter.isPathVariable());
		assertFalse(actionInputParameter.isRequired());

		assertTrue(actionInputParameter.isRequestParam());
	}

	@Test
	public void testAddReviewRequestParamRating() throws NoSuchMethodException {
		Method addReview = DummyController.class.getMethod("addReview", String.class, String.class);
		MethodParameter rating = new MethodParameter(addReview, 0);

		SpringActionInputParameter actionInputParameter = new SpringActionInputParameter(rating, "excellent");

		assertTrue(actionInputParameter.hasValue());
		assertEquals("excellent", actionInputParameter.getValue());
		assertEquals("excellent", actionInputParameter.getValueFormatted());

		assertEquals(DummyController.class, actionInputParameter.getDeclaringClass());

		// @Select set property as required
		// assertFalse(actionInputParameter.hasInputConstraints());

		assertEquals("rating", actionInputParameter.getParameterName());
		assertEquals(String.class, actionInputParameter.getParameterType());
		assertEquals(3,
				actionInputParameter.getPossibleValues(new SpringActionDescriptor("post", RequestMethod.POST.name())).length);
		assertEquals(Type.TEXT, actionInputParameter.getHtmlInputFieldType());

		assertFalse(actionInputParameter.isArrayOrCollection());
		assertFalse(actionInputParameter.isRequestBody());
		assertFalse(actionInputParameter.isRequestHeader());
		assertFalse(actionInputParameter.isRequestParam());

		assertTrue(actionInputParameter.isRequired());
		assertTrue(actionInputParameter.isPathVariable());
	}

	@Test
	public void testAddReviewRequestParamSearchTerms() throws NoSuchMethodException {
		Method addReview = DummyController.class.getMethod("queryReviewsRated", List.class);
		MethodParameter rating = new MethodParameter(addReview, 0);

		List<String> callValues = Arrays.asList("excellent", "mediocre");
		SpringActionInputParameter actionInputParameter = new SpringActionInputParameter(rating, callValues);

		assertTrue(actionInputParameter.hasValue());
		assertEquals(callValues, actionInputParameter.getValue());
		assertThat(callValues, Matchers.contains("excellent", "mediocre"));

		assertEquals(DummyController.class, actionInputParameter.getDeclaringClass());

		// @Select set property as required
		// assertFalse(actionInputParameter.hasInputConstraints());

		assertEquals("searchTerms", actionInputParameter.getParameterName());
		assertEquals(List.class, actionInputParameter.getParameterType());
		assertEquals(new ParameterizedTypeReference<List<String>>() {}.getType(),
				actionInputParameter.getGenericParameterType());
		assertEquals(3,
				actionInputParameter.getPossibleValues(new SpringActionDescriptor("post", RequestMethod.POST.name())).length);
		assertNull(actionInputParameter.getHtmlInputFieldType());

		assertTrue(actionInputParameter.isRequestParam());
		assertTrue(actionInputParameter.isArrayOrCollection());
		assertTrue(actionInputParameter.isRequired());

		assertFalse(actionInputParameter.isRequestBody());
		assertFalse(actionInputParameter.isRequestHeader());
		assertFalse(actionInputParameter.isPathVariable());
	}

	enum ShadeOfBlue {
		DARK_BLUE, BLUE, LIGHT_BLUE, BABY_BLUE
	}

	@Test
	public void testGetPossibleValuesForEnum() throws NoSuchMethodException {

		class BlueController {

			@RequestMapping
			public void setShade(@RequestParam ShadeOfBlue shade) {

			}
		}

		Method setShade = BlueController.class.getMethod("setShade", ShadeOfBlue.class);
		MethodParameter shade = new MethodParameter(setShade, 0);

		SpringActionInputParameter actionInputParameter = new SpringActionInputParameter(shade, ShadeOfBlue.DARK_BLUE);

		assertTrue(actionInputParameter.hasValue());
		assertEquals(ShadeOfBlue.DARK_BLUE, actionInputParameter.getValue());

		assertEquals(BlueController.class, actionInputParameter.getDeclaringClass());

		assertFalse(actionInputParameter.hasInputConstraints());

		assertEquals("shade", actionInputParameter.getParameterName());
		assertEquals(ShadeOfBlue.class, actionInputParameter.getParameterType());
		assertEquals(ShadeOfBlue.class, actionInputParameter.getGenericParameterType());
		assertEquals(4,
				actionInputParameter.getPossibleValues(new SpringActionDescriptor("get", RequestMethod.GET.name())).length);
		assertEquals(Type.TEXT, actionInputParameter.getHtmlInputFieldType());

		assertTrue(actionInputParameter.isRequestParam());
		assertTrue(actionInputParameter.isRequired());

		assertFalse(actionInputParameter.isArrayOrCollection());

		assertFalse(actionInputParameter.isRequestBody());
		assertFalse(actionInputParameter.isRequestHeader());
		assertFalse(actionInputParameter.isPathVariable());
	}

	@Test
	public void testGetPossibleValuesForEnumArray() throws NoSuchMethodException {

		class BlueController {

			@RequestMapping
			public void setShade(@RequestParam ShadeOfBlue[] shade) {

			}
		}

		Method setShade = BlueController.class.getMethod("setShade", ShadeOfBlue[].class);
		MethodParameter shade = new MethodParameter(setShade, 0);

		SpringActionInputParameter actionInputParameter = new SpringActionInputParameter(shade, ShadeOfBlue.DARK_BLUE);

		assertTrue(actionInputParameter.hasValue());
		assertEquals(ShadeOfBlue.DARK_BLUE, actionInputParameter.getValue());

		assertEquals(BlueController.class, actionInputParameter.getDeclaringClass());

		assertFalse(actionInputParameter.hasInputConstraints());

		assertEquals("shade", actionInputParameter.getParameterName());
		assertEquals(ShadeOfBlue[].class, actionInputParameter.getParameterType());
		assertEquals(ShadeOfBlue[].class, actionInputParameter.getGenericParameterType());
		assertEquals(4,
				actionInputParameter.getPossibleValues(new SpringActionDescriptor("get", RequestMethod.GET.name())).length);
		assertNull(actionInputParameter.getHtmlInputFieldType());

		assertTrue(actionInputParameter.isRequestParam());
		assertTrue(actionInputParameter.isRequired());
		assertTrue(actionInputParameter.isArrayOrCollection());

		assertFalse(actionInputParameter.isRequestBody());
		assertFalse(actionInputParameter.isRequestHeader());
		assertFalse(actionInputParameter.isPathVariable());
	}

	@Test
	public void testGetPossibleValuesForListOfEnum() throws NoSuchMethodException {

		class BlueController {

			@RequestMapping
			public void setShade(@RequestParam List<ShadeOfBlue> shade) {

			}
		}

		Method setShade = BlueController.class.getMethod("setShade", List.class);
		MethodParameter shade = new MethodParameter(setShade, 0);

		SpringActionInputParameter actionInputParameter = new SpringActionInputParameter(shade, ShadeOfBlue.DARK_BLUE);

		assertTrue(actionInputParameter.hasValue());
		assertEquals(ShadeOfBlue.DARK_BLUE, actionInputParameter.getValue());

		assertEquals(BlueController.class, actionInputParameter.getDeclaringClass());

		assertFalse(actionInputParameter.hasInputConstraints());

		assertEquals("shade", actionInputParameter.getParameterName());
		assertEquals(List.class, actionInputParameter.getParameterType());

		assertEquals(new ParameterizedTypeReference<List<ShadeOfBlue>>() {}.getType(),
				actionInputParameter.getGenericParameterType());
		assertEquals(4,
				actionInputParameter.getPossibleValues(new SpringActionDescriptor("get", RequestMethod.GET.name())).length);
		assertNull(actionInputParameter.getHtmlInputFieldType());

		assertTrue(actionInputParameter.isRequestParam());
		assertTrue(actionInputParameter.isRequired());
		assertTrue(actionInputParameter.isArrayOrCollection());

		assertFalse(actionInputParameter.isRequestBody());
		assertFalse(actionInputParameter.isRequestHeader());
		assertFalse(actionInputParameter.isPathVariable());
	}
}
