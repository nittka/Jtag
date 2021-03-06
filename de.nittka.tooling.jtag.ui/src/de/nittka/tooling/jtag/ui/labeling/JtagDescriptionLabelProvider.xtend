/*******************************************************************************
 * Copyright (C) 2017-2020 Alexander Nittka (alex@nittka.de)
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package de.nittka.tooling.jtag.ui.labeling

import de.nittka.tooling.jtag.jtag.JtagPackage
import org.eclipse.xtext.resource.IEObjectDescription
import org.eclipse.xtext.resource.IReferenceDescription
import org.eclipse.xtext.resource.IResourceDescription
import org.eclipse.xtext.ui.label.DefaultDescriptionLabelProvider
import org.eclipse.emf.common.util.URI

//import org.eclipse.xtext.resource.IEObjectDescription

/**
 * Provides labels for a IEObjectDescriptions and IResourceDescriptions.
 * 
 * see http://www.eclipse.org/Xtext/documentation.html#labelProvider
 */
class JtagDescriptionLabelProvider extends DefaultDescriptionLabelProvider {

	// Labels and icons can be computed like this:
	
	override text(IEObjectDescription ele) {
		if(ele.EClass===JtagPackage.Literals.FILE){
			val title=ele.getUserData("title")
			return (if(title!==null)'''«ele.name» - «title»''' else '''«ele.name»''').toString
		} else if(ele.EClass===JtagPackage.Literals.SEARCH){
			return ele.qualifiedName.toString
		}
		super.text(ele)
	}

	override image(IEObjectDescription ele) {
		val clazz = ele.EClass
		switch clazz{
			case JtagPackage.eINSTANCE.file: return JtagLabelProvider.getJtagFileIcon(ele.qualifiedName.toString)
			case JtagPackage.eINSTANCE.jtagConfig: return "categorytype.gif"
			case JtagPackage.eINSTANCE.search: return "search.png"
		} 
	}

	override image(IResourceDescription element) {
		if(!element.getExportedObjectsByType(JtagPackage.eINSTANCE.file).empty){
			return "folder.png"
		} else if(!element.getExportedObjectsByType(JtagPackage.eINSTANCE.categoryType).empty){
			return "categories.gif"
		} else{
			return "searches.png"
		}
	}

	override text(IReferenceDescription referenceDescription) {
		val sourceURI=referenceDescription.sourceEObjectUri.toString
		if(sourceURI.contains("/@search/")){
			return "unnamed search"
		} else if(sourceURI.contains("//@categories.")){
			if(isType(referenceDescription.targetEObjectUri)){
				return "folder category type"
			} else{
				return "folder category"
			}
		}
	}

	override image(IReferenceDescription referenceDescription) {
		val sourceURI=referenceDescription.sourceEObjectUri.toString
		if(sourceURI.contains("/@search/")){
			return "search.png"
		} else if(sourceURI.contains("//@categories.")){
			if(isType(referenceDescription.targetEObjectUri)){
				return "categorytype.gif"
			} else{
				return "category.gif"
			}
		}
	}

	def private boolean isType(URI targetURi){
		return !targetURi.toString.contains("/@category.")
	}

}
