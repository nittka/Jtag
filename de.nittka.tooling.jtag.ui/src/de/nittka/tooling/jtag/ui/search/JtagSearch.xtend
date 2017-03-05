package de.nittka.tooling.jtag.ui.search

import com.google.common.base.Optional
import de.nittka.tooling.jtag.jtag.AndSearchExpression
import de.nittka.tooling.jtag.jtag.Category
import de.nittka.tooling.jtag.jtag.CategorySearch
import de.nittka.tooling.jtag.jtag.DescriptionSearch
import de.nittka.tooling.jtag.jtag.JtagPackage
import de.nittka.tooling.jtag.jtag.NegationSearchExpression
import de.nittka.tooling.jtag.jtag.OrSearchExpression
import de.nittka.tooling.jtag.jtag.Search
import de.nittka.tooling.jtag.jtag.SearchExpression
import de.nittka.tooling.jtag.jtag.SearchReference
import de.nittka.tooling.jtag.jtag.TagSearch
import de.nittka.tooling.jtag.jtag.TitleSearch
import java.util.ArrayList
import java.util.Collections
import java.util.List
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.resource.IEObjectDescription
import org.eclipse.xtext.resource.IReferenceDescription

//TODO prevent SearchReference loops
//TODO refine tag and description search
//TODO introduce date and reference search?
class JtagSearch {

	Search search;
	IEObjectDescription desc
	List<IReferenceDescription> refs
	IProgressMonitor monitor

	new (Search search) {
		this.search=search;
	}

	def Resource getResource(){
		return search.eResource();
	}

	def boolean matches(IEObjectDescription description, List<IReferenceDescription> references, IProgressMonitor monitor){
		this.desc=description
		this.monitor=monitor
		this.refs=references;
		return apply(search.search)
	}

	def private boolean apply(SearchExpression expression){
		return !monitor.canceled && internalApply(expression)
	}

	def private dispatch boolean internalApply(SearchExpression exp){
		throw new UnsupportedOperationException("missing search implementation for "+exp.class)
	}

	def private dispatch boolean internalApply(NegationSearchExpression exp){
		return !apply(exp.negated)
	}

	def private dispatch boolean internalApply(SearchReference exp){
		return apply(exp.search.search)
	}

	def private dispatch boolean internalApply(OrSearchExpression exp){
		return apply(exp.left) || apply(exp.right)
	}

	def private dispatch boolean internalApply(AndSearchExpression exp){
		return apply(exp.left) && apply(exp.right)
	}

	def private String userData(String key){
		Optional.of(desc.getUserData(key)).or("");
	}

	def private dispatch boolean internalApply(TagSearch exp){
		val tags=userData("tags").split(";")?.toList
		return tags.contains(exp.tag)
	}

	def private dispatch boolean internalApply(DescriptionSearch exp){
		return userData("desc").contains(exp.description)
	}

	def private dispatch boolean internalApply(TitleSearch exp){
		return userData("title").contains(exp.title)
	}

	def private dispatch boolean internalApply(CategorySearch exp){
		//TODO handle shortcut categories properly - after considering what the search semantics should be
		val referencedCategories=refs
		.filter[refFromCurrentDesc]
		.map[targetEObjectUri].toList
		return !Collections.disjoint(referencedCategories, getUrisToLookFor(exp))
	}

	def private boolean refFromCurrentDesc(IReferenceDescription ref){
		val containerURI=ref.containerEObjectURI
		val boolean refForAllDescriptions=containerURI===null
		return (ref.EReference===JtagPackage.Literals.CATEGORY_REF__CATEGORIES && 
			(refForAllDescriptions ||ref.containerEObjectURI.equals(desc.EObjectURI))
		)
	}

	def private List<URI> getUrisToLookFor(CategorySearch exp){
		val categoriesToLookFor=new ArrayList<URI>
		val category = exp.category.category
		val baseURI = getResource.getURI();

		categoriesToLookFor.add(baseURI.appendFragment(getResource.getURIFragment(category)))
		if(category.shortCuts.empty){
			if (exp.orBelow){
				EcoreUtil2.getAllContentsOfType(category, Category).forEach[
					categoriesToLookFor.add(baseURI.appendFragment(getResource.getURIFragment(it)))
				]
			}
			if (exp.orAbove){
				var EObject parent=category.eContainer
				while(parent instanceof Category){
					categoriesToLookFor.add(baseURI.appendFragment(getResource.getURIFragment(parent)))
					parent=parent.eContainer
				}
			}
		}
		return categoriesToLookFor
	}
}