/*
 * generated by Xtext
 */
package de.nittka.tooling.jtag.validation

import de.nittka.tooling.jtag.datesearch.SearchDate
import de.nittka.tooling.jtag.jtag.Category
import de.nittka.tooling.jtag.jtag.CategoryType
import de.nittka.tooling.jtag.jtag.DateSearch
import de.nittka.tooling.jtag.jtag.File
import de.nittka.tooling.jtag.jtag.JtagConfig
import de.nittka.tooling.jtag.jtag.JtagPackage
import de.nittka.tooling.jtag.jtag.Search
import java.util.List
import java.util.Map
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
import java.util.Optional
import de.nittka.tooling.jtag.datesearch.IntervalSearch
import java.time.LocalDate

//import org.eclipse.xtext.validation.Check

/**
 * Custom validation rules. 
 *
 * see http://www.eclipse.org/Xtext/documentation.html#validation
 */
class JtagValidator extends AbstractJtagValidator {

	val public static FILE_NAME="filename"
	val public static MISSING_CATEGORY="missingCategory"

	@Inject
	private ResourceDescriptionsProvider indexProvider
	@Inject
	private IResourceServiceProvider serviceProvider

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
