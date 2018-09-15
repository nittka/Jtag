package de.nittka.tooling.jtag.datesearch;

import java.time.LocalDate;
import java.util.Optional;
import java.util.regex.Pattern;

public class SearchDate {

	private Pattern searchDatePattern=Pattern.compile("((\\?)|(\\d{4}))-((\\?)|(\\d{2}))-((\\?)|(\\d{2}))");
	private String dateString;
	private Optional<Integer> year;
	private Optional<Integer> month;
	private Optional<Integer> day;

	public SearchDate(String date) {
		this.dateString=date;
	}

	public boolean isSyntaxValid(){
		return searchDatePattern.matcher(dateString).matches();
	}

	public boolean isSupportedDateFormat(){
		String[] splitted = dateString.split("-");
		if(splitted.length==3){
			year=toInteger(splitted[0]);
			month=toInteger(splitted[1]);
			day=toInteger(splitted[2]);

			if(year.isPresent()){
				if(!month.isPresent() && day.isPresent()){
					return false;
				}
			} else if(!month.isPresent()){
				return false;
			}
			return true;
		}
		throw new IllegalStateException("invalid syntax "+dateString);
	}

	public boolean isValidDate(){
		if(year==null ||month==null||day==null){
			throw new IllegalStateException("date format was not checked");
		}
		try{
			LocalDate.of(year.orElse(2004), month.orElse(1), day.orElse(1));
			return true;
		}catch(Exception e){
			return false;
		}
	}

	public boolean isYearDefined(){
		return year.isPresent();
	}

	private Optional<Integer> toInteger(String segment){
		if(isWildcard(segment)){
			return Optional.empty();
		}else{
			return Optional.of(Integer.parseInt(segment));
		}
	}

	private boolean isWildcard(String segment){
		return segment.length()==1 && segment.charAt(0)=='?';
	}

	boolean isBeforeOrEqual(SearchDate other){
		if(isYearDefined() && other.isYearDefined()){
			int yearCompare=compare(year, other.year);
			if(yearCompare==0){
				int monthCompare=compare(month, other.month);
				if(monthCompare==0){
					return compare(day, other.day)<=0;
				}else{
					return monthCompare<0;
				}
			}else{
				return yearCompare<0;
			}
		}
		return true;
	}

	private int compare(Optional<Integer> i1, Optional<Integer> i2){
		if(i1.isPresent() && i2.isPresent()){
			return i1.get().compareTo(i2.get());
		}
		return -1;
	}
}
