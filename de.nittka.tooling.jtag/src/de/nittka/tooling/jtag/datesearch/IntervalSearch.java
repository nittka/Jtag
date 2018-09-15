package de.nittka.tooling.jtag.datesearch;

import java.util.Objects;
import java.util.Optional;

public class IntervalSearch {

	private SearchDate from;
	private SearchDate to;

	public IntervalSearch(SearchDate from, SearchDate to) {
		this.from=Objects.requireNonNull(from);
		this.to=Objects.requireNonNull(to);
	}

	public Optional<String> getSearchIntervalError(){
		if(from.isValidDate() && to.isValidDate()){
			if(from.isYearDefined() != to.isYearDefined()){
				//interval search is possible only if *for both dates* year is present or missing
				return Optional.of("for interval searches the year in both dates must be specified or wildcard");
			} else if(from.isYearDefined() && to.isYearDefined()){
				if(!from.isBeforeOrEqual(to)){
					return Optional.of("invalid search interval - to must not be before from");
				}
			}
		}else{
			throw new IllegalStateException("dates were not validated");
		}
		return Optional.empty();
	}
}
