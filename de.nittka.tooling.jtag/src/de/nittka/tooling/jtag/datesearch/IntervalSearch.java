/*******************************************************************************
 * Copyright (C) 2017-2020 Alexander Nittka (alex@nittka.de)
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package de.nittka.tooling.jtag.datesearch;

import java.util.Objects;
import java.util.Optional;

public class IntervalSearch {

	private SearchDate from;
	private SearchDate to;
	private Optional<String> intervalError;
	boolean isInverseInteval;

	public IntervalSearch(SearchDate from, SearchDate to) {
		this.from=Objects.requireNonNull(from);
		this.to=Objects.requireNonNull(to);
		intervalError=calculateInvervalError();
		isInverseInteval= to.isBeforeOrEqual(from) && !from.isBeforeOrEqual(to);
	}

	public Optional<String> getSearchIntervalError(){
		return intervalError;
	}

	private Optional<String> calculateInvervalError(){
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

	public boolean isMatch(String date){
		if(date!=null && !intervalError.isPresent()){
			SearchDate imageDate = new SearchDate(date);
			if(imageDate.isSupportedDateFormat() && imageDate.isValidDate()){
				if(isInverseInteval){
					return from.isBeforeOrEqual(imageDate) || imageDate.isBeforeOrEqual(to);
				} else {
					return from.isBeforeOrEqual(imageDate) && imageDate.isBeforeOrEqual(to);
				}
			}
		}
		return false;
	}
}
