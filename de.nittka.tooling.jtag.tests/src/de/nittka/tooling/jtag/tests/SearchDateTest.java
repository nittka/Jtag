package de.nittka.tooling.jtag.tests;

import org.junit.Assert;
import org.junit.Test;

import de.nittka.tooling.jtag.datesearch.SearchDate;

public class SearchDateTest {

	@Test
	public void checkSyntax(){
		syntaxOK("?-?-?");
		syntaxOK("?-?-01");
		syntaxOK("?-01-?");
		syntaxOK("0001-?-?");
		syntaxOK("?-01-01");
		syntaxOK("0001-01-?");
		syntaxOK("0001-01-01");
		syntaxOK("?-?-99");
		syntaxOK("?-99-?");
		syntaxOK("9999-?-?");
		syntaxOK("?-99-99");
		syntaxOK("9999-99-?");
		syntaxOK("9999-99-99");

		syntaxNotOK("00a1-01-01");
		syntaxNotOK("0001-a1-01");
		syntaxNotOK("0001-01-a1");
		syntaxNotOK("? -?-?");
		syntaxNotOK("?-?-1");
		syntaxNotOK("?-?-111");
		syntaxNotOK("?-1-?");
		syntaxNotOK("?-111-?");
		syntaxNotOK("?-1-01");
		syntaxNotOK("?-111-01");
		syntaxNotOK("111-?-?");
		syntaxNotOK("11111-?-?");
		syntaxNotOK("111-01-?");
		syntaxNotOK("11111-01-?");
		syntaxNotOK("111-?-01");
		syntaxNotOK("11111-?-01");
		syntaxNotOK("111-01-01");
		syntaxNotOK("11111-01-01");
	}

	private void syntaxOK(String date){
		Assert.assertTrue(new SearchDate(date).isSyntaxValid());
	}

	private void syntaxNotOK(String date){
		Assert.assertFalse(new SearchDate(date).isSyntaxValid());
	}

	@Test
	public void checkSearchDateFormat(){
		//YMD, YM, Y, MD, M supported
		dateFormatOK("2015-?-?");
		dateFormatOK("2015-01-?");
		dateFormatOK("2015-01-01");
		dateFormatOK("?-01-?");
		dateFormatOK("?-01-01");
		dateFormatOK("9999-?-?");
		dateFormatOK("9999-99-?");
		dateFormatOK("9999-99-99");
		dateFormatOK("?-99-?");
		dateFormatOK("?-99-99");

		//YD, D, "nothing" do not make sense
		dateFormatNotOK("2001-?-01");
		dateFormatNotOK("?-?-01");
		dateFormatNotOK("?-?-?");
	}

	private void dateFormatOK(String date){
		Assert.assertTrue(new SearchDate(date).isSupportedDateFormat());
	}

	private void dateFormatNotOK(String date){
		Assert.assertFalse(new SearchDate(date).isSupportedDateFormat());
	}

	@Test
	public void checkSearchDateValid(){
		dateValid("0001-?-?");
		dateValid("2015-?-?");
		dateValid("9999-?-?");
		dateValid("0000-01-?");
		dateValid("2015-01-?");
		dateValid("9999-01-?");
		dateValid("0000-12-?");
		dateValid("2015-12-?");
		dateValid("9999-12-?");
		dateValid("0000-01-01");
		dateValid("2015-01-01");
		dateValid("9999-01-01");
		dateValid("0000-12-01");
		dateValid("2015-12-01");
		dateValid("9999-12-01");
		dateValid("0000-01-31");
		dateValid("2015-01-31");
		dateValid("9999-01-31");
		dateValid("0000-12-31");
		dateValid("2015-12-31");
		dateValid("9999-12-31");
		dateValid("?-01-?");
		dateValid("?-12-?");
		dateValid("?-01-01");
		dateValid("?-12-01");
		dateValid("?-01-31");
		dateValid("?-12-31");
		dateValid("2008-02-28");
		dateValid("2008-02-29");
		dateValid("2015-03-01");
		dateValid("2015-03-31");

		dateInvalid("2015-00-01");
		dateInvalid("2015-13-01");
		dateInvalid("2015-01-00");
		dateInvalid("2015-01-32");
		dateInvalid("2015-12-00");
		dateInvalid("2015-12-32");
		dateInvalid("2015-02-29");
		dateInvalid("2015-04-31");
	}

	private void dateValid(String date){
		Assert.assertTrue(getSearchDate(date).isValidDate());
	}

	private void dateInvalid(String date){
		Assert.assertFalse(getSearchDate(date).isValidDate());
	}

	@Test
	public void testExactMatch(){
		String imageDate="2015-03-17";
		assertExactMatch("2015-03-17", imageDate);
		assertExactMatch("2015-03-?", imageDate);
		assertExactMatch("2015-?-?", imageDate);
		assertExactMatch("?-03-17", imageDate);
		assertExactMatch("?-03-?", imageDate);

		rejectExactMatch("2014-03-17", imageDate);
		rejectExactMatch("2016-03-17", imageDate);
		rejectExactMatch("2015-02-17", imageDate);
		rejectExactMatch("2015-04-17", imageDate);
		rejectExactMatch("2015-03-18", imageDate);
		rejectExactMatch("2015-03-16", imageDate);
		rejectExactMatch("2015-02-?", imageDate);
		rejectExactMatch("2015-04-?", imageDate);
		rejectExactMatch("2014-?-?", imageDate);
		rejectExactMatch("2016-?-?", imageDate);
		rejectExactMatch("?-02-17", imageDate);
		rejectExactMatch("?-04-17", imageDate);
		rejectExactMatch("?-02-?", imageDate);
		rejectExactMatch("?-04-?", imageDate);
	}

	private void assertExactMatch(String searchDate, String imageDate){
		SearchDate sd = getSearchDate(searchDate);
		Assert.assertTrue(sd.isExactMatch(imageDate));
	}

	private void rejectExactMatch(String searchDate, String imageDate){
		SearchDate sd = getSearchDate(searchDate);
		Assert.assertFalse(sd.isExactMatch(imageDate));
		
	}

	private SearchDate getSearchDate(String date){
		SearchDate searchDate = new SearchDate(date);
		Assert.assertTrue(searchDate.isSupportedDateFormat());
		return searchDate;
	}
}
