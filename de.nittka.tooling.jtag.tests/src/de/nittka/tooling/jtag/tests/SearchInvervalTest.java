package de.nittka.tooling.jtag.tests;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import de.nittka.tooling.jtag.datesearch.IntervalSearch;
import de.nittka.tooling.jtag.datesearch.SearchDate;

public class SearchInvervalTest {

	//year must be present or missing in both years
	@Test
	public void yearStatusSame(){
		intervalNotOK("2015-03-01", "?-03-31");
		intervalNotOK("2015-03-?", "?-03-31");
		intervalNotOK("2015-?-?", "?-03-31");
		intervalNotOK("2015-03-01", "?-03-?");
		intervalNotOK("2015-03-?", "?-03-?");
		intervalNotOK("2015-?-?", "?-03-?");
		intervalNotOK("?-03-01", "2015-03-31");
		intervalNotOK("?-03-?", "2015-03-31");
		intervalNotOK("?-03-01", "2015-03-?");
		intervalNotOK("?-03-?", "2015-03-?");
		intervalNotOK("?-03-01", "2015-?-?");
		intervalNotOK("?-03-?", "2015-?-?");
	}

	@Test
	public void checkSearchIntervalOK(){
		intervalOK("2015-03-01", "2015-03-31");
		intervalOK("2015-03-01", "2015-03-?");
		intervalOK("2015-03-01", "2015-04-01");
		intervalOK("2015-03-01", "2015-04-?");
		intervalOK("2015-03-01", "2015-?-?");
		intervalOK("2015-03-01", "2016-02-1");
		intervalOK("2015-03-01", "2016-02-?");
		intervalOK("2015-03-01", "2016-?-?");

		//without year any combination is OK
		intervalOK("?-03-01", "?-03-31");
		intervalOK("?-05-01", "?-03-31");
		intervalOK("?-03-?", "?-03-31");
		intervalOK("?-05-?", "?-03-31");
		intervalOK("?-03-01", "?-03-?");
		intervalOK("?-05-01", "?-03-?");
		intervalOK("?-03-?", "?-03-?");
		intervalOK("?-05-?", "?-03-?");
	}

	@Test
	public void negativeInterval(){
		intervalNotOK("2015-03-31", "2015-03-30");
		intervalNotOK("2015-03-01", "2015-02-28");
		intervalNotOK("2015-03-01", "2015-02-?");
		intervalNotOK("2015-03-01", "2014-?-?");
		intervalNotOK("2015-02-?", "2015-01-31");
		intervalNotOK("2015-02-?", "2015-01-?");
		intervalNotOK("2015-02-?", "2014-?-?");
		intervalNotOK("2015-?-?", "2014-12-31");
		intervalNotOK("2015-?-?", "2014-12-?");
		intervalNotOK("2015-?-?", "2014-?-?");
	}


	private void intervalOK(String from, String to){
		IntervalSearch interval = getInterval(from, to);
		Optional<String> error = interval.getSearchIntervalError();
		if(error.isPresent()){
			Assert.fail(error.get());
		}
	}

	private void intervalNotOK(String from, String to){
		IntervalSearch interval = getInterval(from, to);
		Assert.assertTrue(interval.getSearchIntervalError().isPresent());
	}

	private IntervalSearch getInterval(String from, String to) {
		IntervalSearch interval = new IntervalSearch(getDate(from), getDate(to));
		return interval;
	}

	private SearchDate getDate(String date){
		SearchDate result = new SearchDate(date);
		Assert.assertTrue(result.isSupportedDateFormat());
		return result;
	}

	@Test
	public void intervalSearchMatchWithYear(){
		String from="2015-03-01";
		String to="2015-03-31";
		intervalSearchMatch(from, to, from);
		intervalSearchMatch(from, to, to);
		intervalSearchMatch(from, to, "2015-03-15");
		intervalSearchMisMatch(from, to, "2015-02-28");
		intervalSearchMisMatch(from, to, "2015-04-01");
		intervalSearchMisMatch(from, to, "2014-03-01");
		intervalSearchMisMatch(from, to, "2016-03-01");

		from="2015-03-01";
		to="2017-07-31";
		intervalSearchMatch(from, to, from);
		intervalSearchMatch(from, to, to);
		intervalSearchMatch(from, to, "2016-06-13");
		intervalSearchMisMatch(from, to, "2015-02-28");
		intervalSearchMisMatch(from, to, "2017-08-01");

		from="2015-03-12";
		to="2017-05-?";
		intervalSearchMatch(from, to, "2015-03-12");
		intervalSearchMatch(from, to, "2016-01-01");
		intervalSearchMatch(from, to, "2016-12-31");
		intervalSearchMatch(from, to, "2017-05-31");
		intervalSearchMisMatch(from, to, "2015-03-11");
		intervalSearchMisMatch(from, to, "2015-03-01");
		intervalSearchMisMatch(from, to, "2015-02-28");
		intervalSearchMisMatch(from, to, "2014-12-03");
		intervalSearchMisMatch(from, to, "2017-06-01");
		intervalSearchMisMatch(from, to, "2018-04-01");

		from="2015-03-?";
		to="2017-05-17";
		intervalSearchMatch(from, to, "2015-03-01");
		intervalSearchMatch(from, to, "2016-01-01");
		intervalSearchMatch(from, to, "2016-12-31");
		intervalSearchMatch(from, to, "2017-05-17");
		intervalSearchMisMatch(from, to, "2017-05-18");
		intervalSearchMisMatch(from, to, "2015-02-28");
		intervalSearchMisMatch(from, to, "2014-12-03");
		intervalSearchMisMatch(from, to, "2017-06-01");
		intervalSearchMisMatch(from, to, "2018-04-01");

		from="2015-03-?";
		to="2017-05-?";
		intervalSearchMatch(from, to, "2015-03-01");
		intervalSearchMatch(from, to, "2016-01-01");
		intervalSearchMatch(from, to, "2016-12-31");
		intervalSearchMatch(from, to, "2017-05-31");
		intervalSearchMisMatch(from, to, "2015-02-28");
		intervalSearchMisMatch(from, to, "2014-12-03");
		intervalSearchMisMatch(from, to, "2017-06-01");
		intervalSearchMisMatch(from, to, "2018-04-01");

		from="2015-?-?";
		to="2017-05-?";
		intervalSearchMatch(from, to, "2015-01-01");
		intervalSearchMatch(from, to, "2017-05-31");
		intervalSearchMatch(from, to, "2016-08-11");
		intervalSearchMisMatch(from, to, "2014-12-31");
		intervalSearchMisMatch(from, to, "2014-04-12");
		intervalSearchMisMatch(from, to, "2017-06-01");
		intervalSearchMisMatch(from, to, "2018-03-11");

		from="2015-03-?";
		to="2017-?-?";
		intervalSearchMatch(from, to, "2015-03-01");
		intervalSearchMatch(from, to, "2017-12-31");
		intervalSearchMatch(from, to, "2016-08-11");
		intervalSearchMisMatch(from, to, "2015-02-28");
		intervalSearchMisMatch(from, to, "2014-04-12");
		intervalSearchMisMatch(from, to, "2018-01-01");

		from="2015-?-?";
		to="2017-?-?";
		intervalSearchMatch(from, to, "2015-01-01");
		intervalSearchMatch(from, to, "2017-12-31");
		intervalSearchMatch(from, to, "2016-08-11");
		intervalSearchMisMatch(from, to, "2014-12-31");
		intervalSearchMisMatch(from, to, "2013-02-28");
		intervalSearchMisMatch(from, to, "2018-01-01");
	}

	@Test
	public void intervalSearchMatchWithoutYear(){
		String from="?-03-01";
		String to="?-03-31";
		intervalSearchMatch(from, to, "2013-03-01");
		intervalSearchMatch(from, to, "2014-03-01");
		intervalSearchMatch(from, to, "2017-03-01");
		intervalSearchMatch(from, to, "2013-03-10");
		intervalSearchMatch(from, to, "2014-03-20");
		intervalSearchMatch(from, to, "2017-03-30");
		intervalSearchMatch(from, to, "2013-03-31");
		intervalSearchMatch(from, to, "2014-03-31");
		intervalSearchMatch(from, to, "2017-03-31");

		intervalSearchMisMatch(from, to, "2013-02-28");
		intervalSearchMisMatch(from, to, "2017-04-01");
		intervalSearchMisMatch(from, to, "2018-12-03");

		from="?-03-07";
		to="?-07-19";
		intervalSearchMatch(from, to, "2013-03-07");
		intervalSearchMatch(from, to, "2014-03-07");
		intervalSearchMatch(from, to, "2017-03-07");
		intervalSearchMatch(from, to, "2013-04-20");
		intervalSearchMatch(from, to, "2014-05-23");
		intervalSearchMatch(from, to, "2017-06-03");
		intervalSearchMatch(from, to, "2013-07-19");
		intervalSearchMatch(from, to, "2014-07-19");
		intervalSearchMatch(from, to, "2017-07-19");

		intervalSearchMisMatch(from, to, "2011-03-06");
		intervalSearchMisMatch(from, to, "2023-03-06");
		intervalSearchMisMatch(from, to, "2014-07-20");
		intervalSearchMisMatch(from, to, "2018-07-20");
		intervalSearchMisMatch(from, to, "2002-01-30");
		intervalSearchMisMatch(from, to, "2015-11-01");

		from="?-05-?";
		to="?-09-14";
		intervalSearchMatch(from, to, "2013-05-01");
		intervalSearchMatch(from, to, "2014-05-01");
		intervalSearchMatch(from, to, "2017-05-01");
		intervalSearchMatch(from, to, "2013-07-20");
		intervalSearchMatch(from, to, "2014-08-31");
		intervalSearchMatch(from, to, "2017-09-14");
		intervalSearchMatch(from, to, "2013-09-14");

		intervalSearchMisMatch(from, to, "2011-04-30");
		intervalSearchMisMatch(from, to, "2023-04-30");
		intervalSearchMisMatch(from, to, "2014-09-15");
		intervalSearchMisMatch(from, to, "2018-09-15");
		intervalSearchMisMatch(from, to, "2002-01-30");
		intervalSearchMisMatch(from, to, "2015-11-01");

		from="?-07-?";
		to="?-10-?";
		intervalSearchMatch(from, to, "2013-07-01");
		intervalSearchMatch(from, to, "2014-07-01");
		intervalSearchMatch(from, to, "2017-07-01");
		intervalSearchMatch(from, to, "2013-08-20");
		intervalSearchMatch(from, to, "2014-09-30");
		intervalSearchMatch(from, to, "2017-10-31");
		intervalSearchMatch(from, to, "2013-10-31");

		intervalSearchMisMatch(from, to, "2011-06-30");
		intervalSearchMisMatch(from, to, "2023-06-30");
		intervalSearchMisMatch(from, to, "2014-11-01");
		intervalSearchMisMatch(from, to, "2018-11-01");
		intervalSearchMisMatch(from, to, "2002-03-05");
		intervalSearchMisMatch(from, to, "2015-12-17");
	}

	@Test
	public void intervalSearchMatchWithoutYearInverseInterval(){
		String from="?-10-01";
		String to="?-03-31";
		intervalSearchMatch(from, to, "2013-10-01");
		intervalSearchMatch(from, to, "2014-10-02");
		intervalSearchMatch(from, to, "2017-11-12");
		intervalSearchMatch(from, to, "2013-12-20");
		intervalSearchMatch(from, to, "2014-01-20");
		intervalSearchMatch(from, to, "2017-02-28");
		intervalSearchMatch(from, to, "2013-03-01");
		intervalSearchMatch(from, to, "2014-03-15");
		intervalSearchMatch(from, to, "2017-03-31");

		intervalSearchMisMatch(from, to, "2013-09-30");
		intervalSearchMisMatch(from, to, "2017-04-01");
		intervalSearchMisMatch(from, to, "2018-06-07");

		from="?-07-14";
		to="?-02-19";
		intervalSearchMatch(from, to, "2013-07-14");
		intervalSearchMatch(from, to, "2014-07-15");
		intervalSearchMatch(from, to, "2017-08-01");
		intervalSearchMatch(from, to, "2013-09-06");
		intervalSearchMatch(from, to, "2014-10-11");
		intervalSearchMatch(from, to, "2017-11-19");
		intervalSearchMatch(from, to, "2013-12-31");
		intervalSearchMatch(from, to, "2014-01-01");
		intervalSearchMatch(from, to, "2017-01-14");
		intervalSearchMatch(from, to, "2008-01-31");
		intervalSearchMatch(from, to, "2000-02-01");
		intervalSearchMatch(from, to, "2008-02-18");
		intervalSearchMatch(from, to, "2000-02-19");

		intervalSearchMisMatch(from, to, "2011-07-01");
		intervalSearchMisMatch(from, to, "2023-07-13");
		intervalSearchMisMatch(from, to, "2014-02-20");
		intervalSearchMisMatch(from, to, "2018-02-21");
		intervalSearchMisMatch(from, to, "2002-03-30");
		intervalSearchMisMatch(from, to, "2015-06-31");

		from="?-12-?";
		to="?-02-14";
		intervalSearchMatch(from, to, "2013-12-01");
		intervalSearchMatch(from, to, "2014-12-03");
		intervalSearchMatch(from, to, "2017-12-31");
		intervalSearchMatch(from, to, "2013-01-31");
		intervalSearchMatch(from, to, "2014-02-13");
		intervalSearchMatch(from, to, "2017-02-14");

		intervalSearchMisMatch(from, to, "2011-11-30");
		intervalSearchMisMatch(from, to, "2023-02-15");
		intervalSearchMisMatch(from, to, "2014-03-15");
		intervalSearchMisMatch(from, to, "2018-10-01");

		from="?-10-?";
		to="?-03-?";
		intervalSearchMatch(from, to, "2013-10-01");
		intervalSearchMatch(from, to, "2014-10-01");
		intervalSearchMatch(from, to, "2017-03-31");
		intervalSearchMatch(from, to, "2013-03-31");
		intervalSearchMatch(from, to, "2014-11-13");
		intervalSearchMatch(from, to, "2017-12-28");
		intervalSearchMatch(from, to, "2013-01-17");
		intervalSearchMatch(from, to, "2013-02-05");

		intervalSearchMisMatch(from, to, "2011-09-30");
		intervalSearchMisMatch(from, to, "2023-04-01");
		intervalSearchMisMatch(from, to, "2014-05-13");
		intervalSearchMisMatch(from, to, "2018-06-17");
		intervalSearchMisMatch(from, to, "2002-08-29");
	}

	private void intervalSearchMatch(String from, String to, String imageDate){
		IntervalSearch search=new IntervalSearch(getDate(from), getDate(to));
		Assert.assertTrue(search.isMatch(imageDate));
	}

	private void intervalSearchMisMatch(String from, String to, String imageDate){
		IntervalSearch search=new IntervalSearch(getDate(from), getDate(to));
		Assert.assertFalse(search.isMatch(imageDate));
	}

}