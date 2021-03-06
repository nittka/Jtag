/*******************************************************************************
 * Copyright (C) 2017-2020 Alexander Nittka (alex@nittka.de)
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package de.nittka.tooling.jtag.validation

import de.nittka.tooling.jtag.datesearch.IntervalSearch
import de.nittka.tooling.jtag.datesearch.SearchDate
import de.nittka.tooling.jtag.jtag.Category
import de.nittka.tooling.jtag.jtag.CategoryType
import de.nittka.tooling.jtag.jtag.DateSearch
import de.nittka.tooling.jtag.jtag.File
import de.nittka.tooling.jtag.jtag.JtagConfig
import de.nittka.tooling.jtag.jtag.JtagPackage
import de.nittka.tooling.jtag.jtag.JtagSearches
import de.nittka.tooling.jtag.jtag.Search
import java.time.LocalDate
import java.util.List
import java.util.Map
import java.util.Optional
import java.util.Set
import java.util.regex.Pattern
import javax.inject.Inject
import org.eclipse.emf.ecore.EStructuralFeature
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.resource.IEObjectDescription
import org.eclipse.xtext.resource.IResourceServiceProvider
import org.eclipse.xtext.resource.impl.ResourceDescriptionsProvider
import org.eclipse.xtext.validation.Check
import org.eclipse.xtext.validation.CheckType
import de.nittka.tooling.jtag.jtag.CategoryRef

//import org.eclipse.xtext.validation.Check

/**
 * Custom validation rules. 
 *
 * see http://www.eclipse.org/Xtext/documentation.html#validation
 */
class JtagValidator extends AbstractJtagValidator {

	val public static FILE_NAME="filename"

	@Inject
	private ResourceDescriptionsProvider indexProvider
	@Inject
	private IResourceServiceProvider serviceProvider

	@Check
	def checkDuplicateCategoryType(JtagConfig doc) {
		val Set<String> types=newHashSet()
		doc.types.forEach[t|
			if (types.contains(t.name)){
				error("duplicate category type", t, JtagPackage.Literals.CATEGORY_TYPE__NAME)
			}else{
				types.add(t.name)
			}
		]
	}

	@Check
	def checkDuplicateCategoryType(File doc) {
		val Set<CategoryType> types=newHashSet()
		doc.categories.forEach[
			val t=it.type
			if (types.contains(t)){
				error("duplicate category type", it, JtagPackage.Literals.CATEGORY_REF__TYPE)
			}else{
				types.add(t)
			}
		]
	}

	@Check
	def checkDuplicateCategoryInType(CategoryType type) {
		val Set<String> names=newHashSet()
		type.category.map[allCategories].flatten.toSet.forEach[
			if (names.contains(name)){
				error("duplicate category within "+type.name, it, JtagPackage.Literals.CATEGORY__NAME)
			}else{
				names.add(name)
			}
		]
	}

	@Check
	def checkDuplicateCategoryInRef(CategoryRef ref) {
		val Set<String> names=newHashSet()
		ref.categories.forEach[it, index|
			if (names.contains(name)){
				error("duplicate category", ref, JtagPackage.Literals.CATEGORY_REF__CATEGORIES, index)
			}else{
				names.add(name)
			}
		]
	}

	@Check
	def checkDuplicateCategoryBetweenTypes(JtagConfig config) {
		val Map<String, Set<String>> allNames=newHashMap
		config.types.forEach[
			allNames.put(it.name, category.map[allCategories].flatten.map[name].toSet)
		]
		config.types.forEach[
			val otherTypesNames=allNames.filter[k,v|k!=name].values.flatten.toSet
			category.map[allCategories].flatten.toSet.forEach[
				if(otherTypesNames.contains(name)){
					warning("same category name in other category type", it, JtagPackage.Literals.CATEGORY__NAME)
				}
			]
		]
	}

	val static Pattern datePattern=Pattern.compile("\\d{4}(-\\d{2}){2,2}")

	@Check(CheckType.FAST)
	def checkDateFormant(File doc) {
		if(doc.date!==null){
			if(!datePattern.matcher(doc.date).matches){
				error("illegal date format (yyyy-mm-dd)", JtagPackage.Literals.FILE__DATE)
			} else {
				try{
					LocalDate.parse(doc.date)
				}catch(Exception e){
					error("illegal date", JtagPackage.Literals.FILE__DATE)
				}
			}
		}
	}

	/**
	 * returns all categories directly related to the given one
	 * (ancestors and descendants)
	 */
	def private List<Category> getAllCategories(Category cat){
		val List<Category> result=newArrayList
		result.add(cat)
		result.addAll(EcoreUtil2.eAllOfType(cat, Category))
		var parent=cat.eContainer
		while(parent instanceof Category){
			result.add(parent as Category)
			parent=parent.eContainer
		}
		result
	}

	@Check(CheckType.NORMAL)
	def checkAtMostOneConfig(JtagConfig config) {
		val List<IEObjectDescription>configs=newArrayList
		val index=indexProvider.getResourceDescriptions(config.eResource)
		val containerManager=serviceProvider.containerManager
		val visibleContainer=containerManager.getVisibleContainers(serviceProvider.resourceDescriptionManager.getResourceDescription(config.eResource), index)
		visibleContainer.forEach[
			configs.addAll(it.getExportedObjectsByType(JtagPackage.Literals.JTAG_CONFIG))
		]
		if(configs.size>1){
			error("there is more than one archive configuration file:"+configs.map[name.toString].join(", "), JtagPackage.Literals.JTAG_CONFIG__TYPES)
		}
	}

	@Check(CheckType.NORMAL)
	def checkSearchNamesLocal(JtagSearches searches) {
		val Set<String> existingNames=newHashSet()
		val Set<String> duplicateNames=newHashSet()
		searches.searches.filter[name!==null].forEach[
			val searchName=name
			if(existingNames.contains(searchName)){
				duplicateNames.add(searchName)
			} else {
				existingNames.add(searchName)
			}
		]
		if(!duplicateNames.empty){
			searches.searches.forEach[
				if(duplicateNames.contains(name)){
					error("duplicate name", it, JtagPackage.Literals.SEARCH__NAME)
				}
			]
		}
	}

	@Check(CheckType.NORMAL)
	def checkSearchNamesGlobal(JtagSearches searches) {
		val List<String>otherSearchNames=newArrayList
		val index=indexProvider.getResourceDescriptions(searches.eResource)
		val containerManager=serviceProvider.containerManager
		val visibleContainer=containerManager.getVisibleContainers(serviceProvider.resourceDescriptionManager.getResourceDescription(searches.eResource), index)
		val myUri=searches.eResource.URI
		visibleContainer.forEach[
			resourceDescriptions.forEach[
				if(myUri!=URI){
					val exportedNames=it.getExportedObjectsByType(JtagPackage.Literals.SEARCH).map[name.toString];
					otherSearchNames.addAll(exportedNames)
				}
			]
		]
		searches.searches.forEach[
			if(otherSearchNames.contains(name)){
				error("duplicate name (in other search file)", it, JtagPackage.Literals.SEARCH__NAME)
			}
		]
	}

	@Check(CheckType.NORMAL)
	def checkNamedSearchWithIgnore(Search search) {
		if(search.name!==null && !search.ignore.empty){
			warning("ignore patters will not be considered when reusing the search, they will only be applied when executing this named search directly", JtagPackage.Literals.SEARCH__NAME)
		}
	}

	@Check(CheckType.FAST)
	def checkSearchDate(DateSearch search) {
		checkSearchDate(search.exact, search, JtagPackage.Literals.DATE_SEARCH__EXACT);
		val fromDate=checkSearchDate(search.from, search, JtagPackage.Literals.DATE_SEARCH__FROM);
		val toDate=checkSearchDate(search.to, search, JtagPackage.Literals.DATE_SEARCH__TO);
		if(fromDate.present && toDate.present){
			val intervalSearch=new IntervalSearch(fromDate.get, toDate.get)
			val error=intervalSearch.searchIntervalError
			if(error.present){
				error(error.get, search, JtagPackage.Literals.DATE_SEARCH__TO)
			}
		}
	}

	def private Optional<SearchDate> checkSearchDate(String dateString, DateSearch context, EStructuralFeature feature){
		if(dateString!=null){
			val searchDate=new SearchDate(dateString)
			if(searchDate.syntaxValid){
				if(!searchDate.supportedDateFormat){
					error("unsupported search date", context,feature);
				} else if(searchDate.isValidDate()){
					return Optional.of(searchDate);
				}else{
					error("illegal date", context,feature);
				}
			}else{
				error("illegal date format", context,feature);
			}
		}
		return Optional.empty
	}
}
