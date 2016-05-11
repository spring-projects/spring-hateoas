package de.escalon.hypermedia.affordance;

import static org.junit.Assert.*;

import java.util.List;

import de.escalon.hypermedia.spring.sample.test.Person;
import de.escalon.hypermedia.spring.sample.test.Review;
import org.junit.Test;

/**
 * Created by Dietrich on 06.12.2015.
 */
public class BeanUtilTest {

	@Test
	public void testGetPropertyPaths() throws Exception {
		List<String> propertyPaths = BeanUtil.getPropertyPaths(Person.class);
		assertEquals("name", propertyPaths.get(0));
	}

	@Test
	public void testGetNestedPropertyPaths() throws Exception {
		List<String> propertyPaths = BeanUtil.getPropertyPaths(Review.class);
		assertEquals("reviewBody", propertyPaths.get(0));
		assertEquals("reviewRating.ratingValue", propertyPaths.get(1));
	}
}