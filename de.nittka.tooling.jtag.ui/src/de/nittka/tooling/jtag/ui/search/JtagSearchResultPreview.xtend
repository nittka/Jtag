package de.nittka.tooling.jtag.ui.search

import de.nittka.tooling.jtag.jtag.File
import de.nittka.tooling.jtag.ui.JtagFileURIs
import java.util.List
import javax.inject.Inject
import javax.inject.Provider
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.resource.IReferenceDescription
import org.eclipse.xtext.resource.XtextResourceSet
import org.eclipse.xtext.ui.editor.findrefs.ReferenceSearchResult

class JtagSearchResultPreview {

	@Inject
	Provider<XtextResourceSet> rsProvider;

	def String createHtml(ReferenceSearchResult searchResult){
	val List<IReferenceDescription> matches=searchResult.matchingReferences
	val rs=rsProvider.get
	'''
		<html>
		 <head>
		  <style>
		   .pic{
		    display:inline;
		   }
		   img{
		    padding:5px;
		    max-width:200px;
		    max-height:200px;
		   }
		  </style>
		 </head>
		 <body>
		 «FOR desc:matches»
		 	«desc.render(rs)»
		 «ENDFOR»
		 </bod<>
		</html>
	'''
	}

	def String render(IReferenceDescription desc, XtextResourceSet rs){
		val EObject e=rs.getEObject(desc.targetEObjectUri,true)
		if(e instanceof File){
			val file=e as File
			val location=JtagFileURIs.getImageLocation(file)
			if(location!==null){
				val javaFile=new java.io.File(location)
				'''
					 <div class="pic">
					  <img src="file://«location»" title="«javaFile.name»  -  «javaFile.parentFile.absolutePath»">
					 </div>
				'''
			}
		}
	}
}