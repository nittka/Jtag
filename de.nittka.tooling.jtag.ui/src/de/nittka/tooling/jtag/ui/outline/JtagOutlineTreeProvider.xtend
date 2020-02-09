/*******************************************************************************
 * Copyright (C) 2017-2020 Alexander Nittka (alex@nittka.de)
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package de.nittka.tooling.jtag.ui.outline

import de.nittka.tooling.jtag.jtag.Category
import de.nittka.tooling.jtag.jtag.File
import de.nittka.tooling.jtag.jtag.Folder
import de.nittka.tooling.jtag.jtag.JtagFactory
import de.nittka.tooling.jtag.jtag.JtagPackage
import de.nittka.tooling.jtag.jtag.Search
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.ui.editor.outline.impl.DefaultOutlineTreeProvider
import org.eclipse.xtext.ui.editor.outline.impl.DocumentRootNode
import javax.inject.Inject
import org.eclipse.xtext.ui.editor.preferences.IPreferenceStoreAccess
import de.nittka.tooling.jtag.ui.preferences.JtagRootPreferencePage
import org.eclipse.core.resources.IWorkspace
import de.nittka.tooling.jtag.ui.validation.JtagUIValidator

/**
 * Customization of the default outline structure.
 *
 * see http://www.eclipse.org/Xtext/documentation.html#outline
 */
class JtagOutlineTreeProvider extends DefaultOutlineTreeProvider {

	@Inject
	IPreferenceStoreAccess preferences
	@Inject
	IWorkspace ws

	def dispatch isLeaf(File file){
		true;
	}

	def dispatch isLeaf(Search search){
		true
	}

	def dispatch String text(Search search){
		if(search.name!=null) search.name else "unnamed search"
	}

	def dispatch String text(Category cat){
		val desc=cat.description
		return if(desc!==null)'''«cat.name» («desc»)'''else cat.name
	}

	override protected _createChildren(DocumentRootNode parentNode, EObject modelElement) {
		super._createChildren(parentNode, modelElement)
		maybeAddIgnoredFilesNode(parentNode, modelElement)
	}

	def private void maybeAddIgnoredFilesNode(DocumentRootNode parentNode, EObject modelElement){
		if(modelElement instanceof Folder){
			val folder=modelElement as Folder
			if(!folder.ignore.empty && ws!==null && preferences.preferenceStore.getBoolean(JtagRootPreferencePage.OUTLINE_SHOW_IGNORED_FILES)){
				val ignoreFileNames=JtagUIValidator.getIgnoredFiles(folder, ws)
				val ignoreParent=createEStructuralFeatureNode(parentNode, folder, JtagPackage.eINSTANCE.folder_Ignore, imageDispatcher.invoke(folder), "ignored files", ignoreFileNames.empty);
				ignoreFileNames.map[createDummyFile].forEach[file|
					val ignoreNode=new IgnoreFileOutlineNode(ignoreParent, folder, imageDispatcher.invoke(file), file.fileName.fileName)
					ignoreNode.setTextRegion(ignoreParent.significantTextRegion)
				]
			}
		}
	}

	def private File createDummyFile(String fileName){
		val factory=JtagFactory.eINSTANCE
		val f=factory.createFile
		val name=factory.createFileName
		name.fileName=fileName
		f.fileName=name
		return f
	}
}