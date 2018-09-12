package de.nittka.tooling.jtag.ui.search

import com.google.common.base.Optional
import de.nittka.tooling.jtag.jtag.AndSearchExpression
import de.nittka.tooling.jtag.jtag.Category
import de.nittka.tooling.jtag.jtag.CategorySearch
import de.nittka.tooling.jtag.jtag.JtagPackage
import de.nittka.tooling.jtag.jtag.NegationSearchExpression
import de.nittka.tooling.jtag.jtag.OrSearchExpression
import de.nittka.tooling.jtag.jtag.Search
import de.nittka.tooling.jtag.jtag.SearchExpression
import de.nittka.tooling.jtag.jtag.SearchReference
import de.nittka.tooling.jtag.jtag.TagSearch
import de.nittka.tooling.jtag.jtag.TextSearch
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
		if(description.matchesIgnorePattern){
			return false
		}else{
			return apply(search.search)
		}
	}

	def private boolean matchesIgnorePattern(IEObjectDescription desc){
		if(!search.ignore.empty){
			//URI of the image file
			val uri=desc.EObjectURI.trimFragment.trimSegments(1).appendSegment(desc.qualifiedName.toString)
			if(search.ignore.exists[pattern|uri.matchesIgnorePattern(pattern)]){
				return true;
			}
		}
		return false;
	}

	def private boolean matchesIgnorePattern(URI uri, String pattern){
		//first simple approximation of match - no wildcards etc.
		return uri.toString.contains(pattern)
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
		Optional.fromNullable(desc.getUserData(key)).or("");
	}

	def private dispatch boolean internalApply(TagSearch exp){
		val tags=userData("tags").split(";")?.toList
		return tags.contains(exp.tag)
	}

	def private dispatch boolean internalApply(TextSearch exp){
		val searchString=exp.text.toLowerCase
		return userData("desc").toLowerCase.contains(searchString)
			|| userData("title").toLowerCase.contains(searchString)
			|| desc.qualifiedName.toString.toLowerCase.contains(searchString)
	}

	def private dispatch boolean internalApply(CategorySearch exp){
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
		val defResource=category.eResource
		val baseURI = defResource.getURI()

		categoriesToLookFor.add(baseURI.appendFragment(defResource.getURIFragment(category)))
		if (exp.orBelow){
			EcoreUtil2.getAllContentsOfType(category, Category).forEach[
				categoriesToLookFor.add(baseURI.appendFragment(defResource.getURIFragment(it)))
			]
		}
		if (exp.orAbove){
			var EObject parent=category.eContainer
			while(parent instanceof Category){
				categoriesToLookFor.add(baseURI.appendFragment(defResource.getURIFragment(parent)))
				parent=parent.eContainer
			}
		}
		return categoriesToLookFor
	}
}