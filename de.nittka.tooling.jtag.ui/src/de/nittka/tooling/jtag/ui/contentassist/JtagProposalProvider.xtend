/*******************************************************************************
 * Copyright (C) 2017-2020 Alexander Nittka (alex@nittka.de)
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package de.nittka.tooling.jtag.ui.contentassist

import de.nittka.tooling.jtag.jtag.CategoryRef
import de.nittka.tooling.jtag.jtag.File
import de.nittka.tooling.jtag.jtag.Folder
import de.nittka.tooling.jtag.jtag.JtagFactory
import de.nittka.tooling.jtag.ui.quickfix.JtagQuickfixProvider
import de.nittka.tooling.jtag.ui.search.JtagTagCounter
import de.nittka.tooling.jtag.ui.validation.JtagUIValidator
import javax.inject.Inject
import org.eclipse.core.resources.IWorkspace
import org.eclipse.core.runtime.Path
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.Assignment
import org.eclipse.xtext.Keyword
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext
import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor

/**
 * see http://www.eclipse.org/Xtext/documentation.html#contentAssist on how to customize content assistant
 */
class JtagProposalProvider extends AbstractJtagProposalProvider {

	@Inject
	var JtagTagCounter tagCounter;
	@Inject
	IWorkspace workspace
	@Inject
	JtagQuickfixProvider quickfix

	override completeFolder_Tags(EObject model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		completeTags(model, context, acceptor)
	}

	override completeFile_Tags(EObject model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		completeTags(model, context, acceptor)
	}

	override completeTagSearch_Tag(EObject model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		completeTags(model, context, acceptor)
	}

	def private completeTags(EObject model, ContentAssistContext context, ICompletionProposalAcceptor acceptor){
		val tags=tagCounter.getTagCount(model.eResource)
		val prefix=context.prefix
		if(prefix.empty) {
			tags.keySet.forEach[acceptor.accept(createCompletionProposal(context))]
		} else {
			tags.forEach[tag, count|
				//prevent proposal of prefix+tag immediately following (aquickfix) 
				if(count.get>1) {
					acceptor.accept(createCompletionProposal(tag, context))
				}
			]
		}
	}

	def completeFolder_Ignore(Folder model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		val undefinedFiles=JtagUIValidator.getFilesWithoutDefinition(model, workspace, true)
		undefinedFiles.forEach[acceptor.accept(createCompletionProposal('''"«it»"''', context))]
		val fileExtensions=undefinedFiles.map[new Path(it).fileExtension].toSet
		fileExtensions.forEach[acceptor.accept(createCompletionProposal('''"*.«it»"''', null, null, 350,context.prefix, context))]
	}

	def completeFile_Date(File model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		var file=workspace.root.findMember(model.eResource.URI.toPlatformString(true));
		var quickfixFiles=quickfix.getFiles(file.parent, newArrayList(model.fileName.fileName))
		if(quickfixFiles.size>0){
			var date=quickfixFiles.get(0).date
			if(date!=null){
				acceptor.accept(createCompletionProposal(date, context))
			}
		}
	}

	def completeFileName_FileName(Folder model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		val missingFiles=JtagUIValidator.getFilesWithoutDefinition(model, workspace, false)
		missingFiles.forEach[
			acceptor.accept(createCompletionProposal(quickfix.maybeEscape(it), context))
		]
	}

	override completeCategory_Name(EObject model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		acceptor.accept(createCompletionProposal("", "category name", getImage(JtagFactory.eINSTANCE.createCategory), context))
	}

	override completeCategoryType_Name(EObject model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		acceptor.accept(createCompletionProposal("", "category type name", getImage(JtagFactory.eINSTANCE.createCategoryType), context))
	}

	override completeKeyword(Keyword keyword, ContentAssistContext contentAssistContext, ICompletionProposalAcceptor acceptor) {
		if(keyword.value==":"){
			val m=contentAssistContext.currentModel
			if(m instanceof CategoryRef){
				val type=(m as CategoryRef).type
				if(type.eIsProxy){
					//do not propose colon if category type is invalid
					return
				}
			}
		}
		super.completeKeyword(keyword, contentAssistContext, acceptor)
	}
}
