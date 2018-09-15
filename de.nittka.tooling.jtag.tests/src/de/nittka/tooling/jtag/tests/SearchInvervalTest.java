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
}