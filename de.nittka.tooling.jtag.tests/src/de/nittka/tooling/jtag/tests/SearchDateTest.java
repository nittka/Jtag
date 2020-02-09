/*******************************************************************************
 * Copyright (C) 2017-2020 Alexander Nittka (alex@nittka.de)
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
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

	@Test
	public void testFromMatch(){
		String fromDate="2015-03-17";
		assertFromMatch(fromDate, fromDate);
		assertFromMatch(fromDate, "2015-03-18");
		assertFromMatch(fromDate, "2015-04-03");
		assertFromMatch(fromDate, "2015-12-31");
		assertFromMatch(fromDate, "2016-01-01");
		assertFromMatch(fromDate, "2019-02-28");
		rejectFromMatch(fromDate, "2015-03-16");
		rejectFromMatch(fromDate, "2015-03-01");
		rejectFromMatch(fromDate, "2015-02-28");
		rejectFromMatch(fromDate, "2014-10-16");

		fromDate="2015-03-?";
		assertFromMatch(fromDate, "2015-03-01");
		assertFromMatch(fromDate, "2015-03-31");
		assertFromMatch(fromDate, "2015-04-03");
		assertFromMatch(fromDate, "2015-12-31");
		assertFromMatch(fromDate, "2016-01-01");
		assertFromMatch(fromDate, "2019-02-28");
		rejectFromMatch(fromDate, "2015-02-28");
		rejectFromMatch(fromDate, "2014-10-16");

		fromDate="2015-?-?";
		assertFromMatch(fromDate, "2015-01-01");
		assertFromMatch(fromDate, "2015-03-31");
		assertFromMatch(fromDate, "2015-04-03");
		assertFromMatch(fromDate, "2015-12-31");
		assertFromMatch(fromDate, "2016-01-01");
		assertFromMatch(fromDate, "2019-02-28");
		rejectFromMatch(fromDate, "2014-12-31");
		rejectFromMatch(fromDate, "2012-10-16");

		fromDate="?-03-17";
		assertFromMatch(fromDate, "2015-03-17");
		assertFromMatch(fromDate, "2014-03-18");
		assertFromMatch(fromDate, "2011-04-03");
		assertFromMatch(fromDate, "2000-12-31");
		rejectFromMatch(fromDate, "2015-03-16");
		rejectFromMatch(fromDate, "2021-03-01");
		rejectFromMatch(fromDate, "2015-02-28");
		rejectFromMatch(fromDate, "2016-01-01");
		rejectFromMatch(fromDate, "2019-02-28");

		fromDate="?-07-?";
		assertFromMatch(fromDate, "2015-07-01");
		assertFromMatch(fromDate, "2014-07-18");
		assertFromMatch(fromDate, "2011-08-03");
		assertFromMatch(fromDate, "2000-12-31");
		rejectFromMatch(fromDate, "2015-06-30");
		rejectFromMatch(fromDate, "2021-06-30");
		rejectFromMatch(fromDate, "2015-05-12");
		rejectFromMatch(fromDate, "2016-04-01");
		rejectFromMatch(fromDate, "2019-01-01");
}

	private void assertFromMatch(String searchDate, String imageDate){
		SearchDate sd = getSearchDate(searchDate);
		Assert.assertTrue(sd.isFromMatch(imageDate));
	}

	private void rejectFromMatch(String searchDate, String imageDate){
		SearchDate sd = getSearchDate(searchDate);
		Assert.assertFalse(sd.isFromMatch(imageDate));
		
	}

	@Test
	public void testToMatch(){
		String toDate="2015-03-17";
		assertToMatch(toDate, toDate);
		assertToMatch(toDate, "2015-03-16");
		assertToMatch(toDate, "2015-03-01");
		assertToMatch(toDate, "2015-02-28");
		assertToMatch(toDate, "2014-10-16");
		rejectToMatch(toDate, "2015-03-18");
		rejectToMatch(toDate, "2015-04-03");
		rejectToMatch(toDate, "2015-12-31");
		rejectToMatch(toDate, "2016-01-01");
		rejectToMatch(toDate, "2019-02-28");

		toDate="2015-03-?";
		assertToMatch(toDate, "2015-03-31");
		assertToMatch(toDate, "2015-03-01");
		assertToMatch(toDate, "2015-02-28");
		assertToMatch(toDate, "2014-10-16");
		rejectToMatch(toDate, "2015-04-01");
		rejectToMatch(toDate, "2015-12-31");
		rejectToMatch(toDate, "2016-01-01");
		rejectToMatch(toDate, "2019-02-28");

		toDate="2015-?-?";
		assertToMatch(toDate, "2015-01-01");
		assertToMatch(toDate, "2015-03-31");
		assertToMatch(toDate, "2015-04-03");
		assertToMatch(toDate, "2015-12-31");
		assertToMatch(toDate, "2014-12-31");
		assertToMatch(toDate, "2012-10-16");
		rejectToMatch(toDate, "2016-01-01");
		rejectToMatch(toDate, "2019-02-28");

		toDate="?-03-17";
		assertToMatch(toDate, "2015-03-17");
		assertToMatch(toDate, "2015-03-16");
		assertToMatch(toDate, "2021-03-01");
		assertToMatch(toDate, "2015-02-28");
		assertToMatch(toDate, "2016-01-01");
		assertToMatch(toDate, "2019-02-28");
		rejectToMatch(toDate, "2014-03-18");
		rejectToMatch(toDate, "2011-04-03");
		rejectToMatch(toDate, "2011-05-03");
		rejectToMatch(toDate, "2011-07-02");
		rejectToMatch(toDate, "2000-12-31");

		toDate="?-07-?";
		assertToMatch(toDate, "2015-07-01");
		assertToMatch(toDate, "2014-07-18");
		assertToMatch(toDate, "2015-06-30");
		assertToMatch(toDate, "2021-06-30");
		assertToMatch(toDate, "2015-05-12");
		assertToMatch(toDate, "2016-04-01");
		assertToMatch(toDate, "2019-01-01");

		rejectToMatch(toDate, "2011-08-01");
		rejectToMatch(toDate, "2020-09-07");
		rejectToMatch(toDate, "2005-10-11");
		rejectToMatch(toDate, "2000-12-31");
	}

	private void assertToMatch(String searchDate, String imageDate){
		SearchDate sd = getSearchDate(searchDate);
		Assert.assertTrue(sd.isToMatch(imageDate));
	}

	private void rejectToMatch(String searchDate, String imageDate){
		SearchDate sd = getSearchDate(searchDate);
		Assert.assertFalse(sd.isToMatch(imageDate));
		
	}

	private SearchDate getSearchDate(String date){
		SearchDate searchDate = new SearchDate(date);
		Assert.assertTrue(searchDate.isSupportedDateFormat());
		return searchDate;
	}
}
