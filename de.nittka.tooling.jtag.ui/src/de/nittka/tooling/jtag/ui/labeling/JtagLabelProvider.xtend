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

import de.nittka.tooling.jtag.jtag.Category
import de.nittka.tooling.jtag.jtag.CategoryType
import de.nittka.tooling.jtag.jtag.File
import de.nittka.tooling.jtag.jtag.Folder
import de.nittka.tooling.jtag.jtag.JtagConfig
import de.nittka.tooling.jtag.jtag.JtagSearches
import de.nittka.tooling.jtag.jtag.Search
import de.nittka.tooling.jtag.ui.JtagFileURIs
import javax.inject.Inject
import org.eclipse.core.runtime.Path
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider
import org.eclipse.xtext.ui.label.DefaultEObjectLabelProvider

/**
 * Provides labels for a EObjects.
 * 
 * see http://www.eclipse.org/Xtext/documentation.html#labelProvider
 */
class JtagLabelProvider extends DefaultEObjectLabelProvider {


	@Inject
	new(AdapterFactoryLabelProvider delegate) {
		super(delegate);
	}

	// Labels and icons can be computed like this:

	def String text(File doc){
		if(doc.title!==null)'''«doc.fileName.fileName» - «doc.title»'''else doc?.fileName?.fileName
	}

	def String text(CategoryType type){
		val desc=type.description
		return if(desc!==null)'''«type.name» («desc»)'''else type.name
	}

	def String text(Category cat){
		val desc=cat.description
		val result=if(desc!==null)'''«cat.name» («desc»)'''else cat.name
		if(cat.eContainer instanceof Category){
			return '''«result» <- «text(cat.eContainer as Category)»'''
		}else{
			return result;
		}
	}

	def image(Folder f){
		return "folder.png";
	}

	def image(File f){
		return getJtagFileIcon(f.fileName.fileName)
	}

	def static getJtagFileIcon(String fileName){
			val fileExt=new Path(fileName.toString).fileExtension
		if(JtagFileURIs.isImageExtension(fileExt)){
			return "jtagfile.gif"
		} else if(JtagFileURIs.isMovieExtension(fileExt)){
			return "movie.gif"
		} else{
			return "unknown_file.gif"
		}
	}

	def image(JtagSearches s){
		return "searches.png"
	}

	def image(Search s){
		return "search.png"
	}

	def image(JtagConfig s){
		return "categories.gif"
	}

	def image(CategoryType s){
		return "categorytype.gif"
	}

	def image(Category s){
		return "category.gif"
	}

}