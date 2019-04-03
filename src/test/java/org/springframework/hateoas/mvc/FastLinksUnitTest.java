package org.springframework.hateoas.mvc;

import static org.hamcrest.Matchers.endsWith;
import static org.joda.time.format.ISODateTimeFormat.date;
import static org.joda.time.format.ISODateTimeFormat.dateTime;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.TestUtils;
import org.springframework.hateoas.mvc.ControllerLinkBuilderUnitTest.ControllerWithMethods;
import org.springframework.http.HttpEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;

public class FastLinksUnitTest extends TestUtils {

	@Test
	public void encodedParams() throws UnsupportedEncodingException {
		String encodedRequestParam = UriUtils.encodeQueryParam("/ =", "UTF-8");
		String encodedPathParam = UriUtils.encodePathSegment("/ =", "UTF-8");
		String link = FastLinks.linkTo(methodOn(SampleController.class).encodedParams(encodedPathParam, encodedRequestParam));
		assertThat(link, endsWith("/sample/" + encodedPathParam + "?value2="+encodedRequestParam));
	}

	@Test
	public void dateMidnightParams() {
		DateMidnight now = new DateMidnight();
		String link = FastLinks.linkTo(methodOn(SampleController.class).sampleMethodDate(now));
		assertThat(link, endsWith("/sample/" + date().print(now)));
	}

	@Test
	public void dateDateTimeParams() {
		DateTime now = new DateTime();
		String link = FastLinks.linkTo(methodOn(SampleController.class).sampleMethodDate(now));
		System.out.println(link);
		assertThat(link, endsWith("/sample/" + date().print(now)));
	}

	@Test
	public void dateDateParams() {
		Date now = new Date();
		String link = FastLinks.linkTo(methodOn(SampleController.class).sampleMethodDate(now));
		assertThat(link, endsWith("/sample/" + date().print(new DateTime(now))));
	}

	@Test
	public void dateDateLocalParams() {
		LocalDate now = new LocalDate();
		String link = FastLinks.linkTo(methodOn(SampleController.class).sampleMethodDate(now));
		assertThat(link, endsWith("/sample/" + date().print(now)));
	}

	@Test
	public void timeDateTimeParams() {
		DateTime now = new DateTime();
		String link = FastLinks.linkTo(methodOn(SampleController.class).sampleMethodTime(now));
		assertThat(link, endsWith("/sample/" + dateTime().print(now)));
	}

	@Test
	public void timeDateParams() {
		Date now = new Date();
		String link = FastLinks.linkTo(methodOn(SampleController.class).sampleMethodTime(now));
		assertThat(link, endsWith("/sample/" + dateTime().print(new DateTime(now))));
	}


	@Test
	public void dateMidnightQueryParams() {
		DateMidnight now = new DateMidnight();
		String link = FastLinks.linkTo(methodOn(SampleController.class).dateQueryParam(now));
		assertThat(link, endsWith("/sample/date?date=" + encodePlus(date().print(now))));
	}

	@Test
	public void dateDateTimeQueryParams() {
		DateTime now = new DateTime();
		String link = FastLinks.linkTo(methodOn(SampleController.class).dateQueryParam(now));
		System.out.println(link);
		assertThat(link, endsWith("/sample/date?date=" + encodePlus(date().print(now))));
	}

	@Test
	public void dateDateQueryParams() {
		Date now = new Date();
		String link = FastLinks.linkTo(methodOn(SampleController.class).dateQueryParam(now));
		assertThat(link, endsWith("/sample/date?date=" + encodePlus(date().print(new DateTime(now)))));
	}

	@Test
	public void dateDateLocalQueryParams() {
		LocalDate now = new LocalDate();
		String link = FastLinks.linkTo(methodOn(SampleController.class).dateQueryParam(now));
		assertThat(link, endsWith("/sample/date?date=" + encodePlus(date().print(now))));
	}

	@Test
	public void timeDateTimeQueryParams() {
		DateTime now = new DateTime();
		String link = FastLinks.linkTo(methodOn(SampleController.class).timeQueryParam(now));
		assertThat(link, endsWith("/sample/time?time=" + encodePlus(dateTime().print(now))));
	}

	@Test
	public void timeDateQueryParams() {
		Date now = new Date();
		String link = FastLinks.linkTo(methodOn(SampleController.class).timeQueryParam(now));
		assertThat(link, endsWith("/sample/time?time=" + encodePlus(dateTime().print(new DateTime(now)))));
	}

	private String encodePlus(String input) {
		return input.replace("+", "%2B");
	}

	@Test
	public void enumParams() {
		String link = FastLinks.linkTo(methodOn(SampleController.class).sampleMethodEnum(TestEnum.VALUE1, TestEnum.VALUE2));
		assertThat(link, endsWith("/sample/VALUE1?value2=VALUE2"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void spaceIsInvalidPathSegment() {
		FastLinks.linkTo(methodOn(ControllerWithMethods.class).methodWithPathVariable("with blank"));
	}

	@Test
	@Ignore
	public void noParamNames() {
		String link = FastLinks.linkTo(methodOn(SampleController.class).noParamNames(1L, 2L, 3L));
		assertThat(link, endsWith("/sample/2/1?id3=3"));
	}

	@Test
	public void optionalParameters() {
		String link = FastLinks.linkTo(methodOn(SampleController.class).sampleMethod(1L, 2L, null));

		assertThat(link, endsWith("/sample/1?id1=2"));
	}

	@Test
	public void listParameters() {
		List<Long> idList = Arrays.asList(2L, 3L, 4L);
		String link = FastLinks.linkTo(methodOn(SampleController.class).listParam(1L, idList));

		assertThat(link, endsWith("/sample/list?id=1&ids=2&ids=3&ids=4"));
	}

	@Test
	public void emptyListParameters() {
		String link = FastLinks.linkTo(methodOn(SampleController.class).listParam(1L, new ArrayList<Long>()));

		assertThat(link, endsWith("/sample/list?id=1"));
	}

	@Test
	public void arrayParameters() {
		Long[] idList = new Long[] {2L, 3L, 4L};
		String link = FastLinks.linkTo(methodOn(SampleController.class).arrayParam(1L, idList));

		assertThat(link, endsWith("/sample/array?id=1&ids=2&ids=3&ids=4"));
	}

	@Test
	public void emptyArrayParameters() {
		String link = FastLinks.linkTo(methodOn(SampleController.class).arrayParam(1L, new Long[0]));

		assertThat(link, endsWith("/sample/array?id=1"));
	}

	@Test
	public void arrayEnumParameters() {
		TestEnum[] enumList = new TestEnum[] {TestEnum.VALUE1, TestEnum.VALUE2};
		String link = FastLinks.linkTo(methodOn(SampleController.class).arrayParam(1L, enumList));

		assertThat(link, endsWith("/sample/1?values=VALUE1&values=VALUE2"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void mapsInLinksAreNotSupported() {
		Map<String, String> queryParams = new LinkedHashMap<String, String>();
		queryParams.put("firstKey", "firstValue");
		queryParams.put("secondKey", "secondValue");

		FastLinks.linkTo(methodOn(SampleController.class).sampleMethodWithMap(queryParams));
	}

	@Test
	public void mapsAreOkWhenTheyAreNull() {
		String link = FastLinks.linkTo(methodOn(SampleController.class).sampleMethodWithMap(null));
		assertThat(link, endsWith("/sample/mapsupport"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void multimapsInListAreNotSupported() {
		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
		queryParams.put("key1", Arrays.asList("value1a", "value1b"));
		queryParams.put("key2", Arrays.asList("value2a", "value2b"));

		FastLinks.linkTo(methodOn(SampleController.class).sampleMethodWithMultimap(queryParams));
	}

	@Test
	public void multimapsAreOkWhenTheyAreNull() {
		String link = FastLinks.linkTo(methodOn(SampleController.class).sampleMethodWithMultimap(null));
		assertThat(link, endsWith("/sample/multivaluemapsupport"));
	}

	@Test
	public void subclassing() {
		String link1 = FastLinks.linkTo(methodOn(SampleController.class).sampleMethodWithMultimap(null));
		String link2 = FastLinks.linkTo(methodOn(SampleSubController.class).sampleMethodWithMultimap(null));
		assertThat(link1, endsWith("/sample/multivaluemapsupport"));
		assertThat(link2, endsWith("/sub/sample/multivaluemapsupport"));
	}

	@Test
	public void linkContainsHostAndObjectParams() {
		String link = FastLinks.linkTo(methodOn(ObjectParamController.class, "objectParam").sampleMethodTime(1L));

		assertEquals("http://localhost/objectParam/sample/1", link);
	}

	interface SampleController {

		@RequestMapping("/sample/list")
		HttpEntity<?> listParam(@RequestParam("id") Long id, @RequestParam("ids") List<Long> ids);

		@RequestMapping("/sample/array")
		HttpEntity<?> arrayParam(@RequestParam("id") Long id, @RequestParam("ids") Long[] ids);

		@RequestMapping("/sample/{id}")
		HttpEntity<?> arrayParam(@PathVariable("id") Long id, @RequestParam("values") TestEnum[] values);

		@RequestMapping("/sample/{id}")
		HttpEntity<?> sampleMethod(@PathVariable("id") Long id, @RequestParam("id1") Long id1, @RequestParam("id2") Long id2);

		@RequestMapping("/sample/{date}")
		HttpEntity<?> sampleMethodDate(@PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) DateMidnight time);

		@RequestMapping("/sample/{date}")
		HttpEntity<?> sampleMethodDate(@PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) DateTime time);

		@RequestMapping("/sample/{date}")
		HttpEntity<?> sampleMethodDate(@PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate time);

		@RequestMapping("/sample/{date}")
		HttpEntity<?> sampleMethodDate(@PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date time);

		@RequestMapping("/sample/{time}")
		HttpEntity<?> sampleMethodTime(@PathVariable("time") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date time);

		@RequestMapping("/sample/{time}")
		HttpEntity<?> sampleMethodTime(@PathVariable("time") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) DateTime time);


		@RequestMapping("/sample/date")
		HttpEntity<?> dateQueryParam(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) DateMidnight time);

		@RequestMapping("/sample/date")
		HttpEntity<?> dateQueryParam(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) DateTime time);

		@RequestMapping("/sample/date")
		HttpEntity<?> dateQueryParam(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate time);

		@RequestMapping("/sample/date")
		HttpEntity<?> dateQueryParam(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date time);

		@RequestMapping("/sample/time")
		HttpEntity<?> timeQueryParam(@RequestParam("time") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date time);

		@RequestMapping("/sample/time")
		HttpEntity<?> timeQueryParam(@RequestParam("time") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) DateTime time);


		@RequestMapping("/sample/{value}")
		HttpEntity<?> sampleMethodEnum(@PathVariable("value") TestEnum value, @RequestParam("value2") TestEnum value2);

		@RequestMapping("/sample/{id2}/{id1}")
		HttpEntity<?> noParamNames(@PathVariable Long id1, @PathVariable Long id2, @RequestParam Long id3);

		@RequestMapping("/sample/{value1}")
		HttpEntity<?> encodedParams(@PathVariable("value1") String value1, @RequestParam("value2") String value2);

		@RequestMapping("/sample/mapsupport")
		HttpEntity<?> sampleMethodWithMap(@RequestParam Map<String, String> queryParams);

		@RequestMapping("/sample/multivaluemapsupport")
		HttpEntity<?> sampleMethodWithMultimap(@RequestParam MultiValueMap<String, String> queryParams);
	}

	@RequestMapping("/sub")
	interface SampleSubController extends SampleController {
	}

	@RequestMapping("/{context}")
	interface ObjectParamController  {
		@RequestMapping("/sample/{id}")
		HttpEntity<?> sampleMethodTime(@PathVariable("id") Long id);

	}

	enum TestEnum {
		VALUE1 {
			@Override
			public String toString() {
				return "value one";
			}
		},
		VALUE2
	}

}
