/*******************************************************************************
 * Copyright (C) 2017-2020 Alexander Nittka (alex@nittka.de)
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package de.nittka.tooling.jtag.ui.quickfix

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.Metadata
import com.drew.metadata.exif.ExifSubIFDDirectory
import de.nittka.tooling.jtag.jtag.File
import de.nittka.tooling.jtag.jtag.Folder
import de.nittka.tooling.jtag.jtag.JtagFactory
import de.nittka.tooling.jtag.ui.validation.JtagUIValidator
import java.text.Collator
import java.text.SimpleDateFormat
import java.util.List
import java.util.Locale
import java.util.TimeZone
import org.eclipse.core.resources.IContainer
import org.eclipse.core.resources.IFile
import org.eclipse.core.runtime.Path
import org.eclipse.xtext.ui.editor.quickfix.DefaultQuickfixProvider
import org.eclipse.xtext.ui.editor.quickfix.Fix
import org.eclipse.xtext.ui.editor.quickfix.IssueResolutionAcceptor
import org.eclipse.xtext.validation.Issue

//import org.eclipse.xtext.ui.editor.quickfix.Fix
//import org.eclipse.xtext.ui.editor.quickfix.IssueResolutionAcceptor
//import org.eclipse.xtext.validation.Issue

/**
 * Custom quickfixes.
 *
 * see http://www.eclipse.org/Xtext/documentation.html#quickfixes
 */
class JtagQuickfixProvider extends DefaultQuickfixProvider {

	@Fix(JtagUIValidator::MISSING_JTAG_FILE)
	def addMissingXarchive(Issue issue, IssueResolutionAcceptor acceptor) {
		val fileNames=issue.data.get(0).split(";;").toList
		acceptor.accept(issue, 'Jtag entries for '+fileNames.join(",\n"), 'creates new entries', null) [
			obj, context |
			val container=context.xtextDocument.getAdapter(IFile).parent
			val filesToAdd=getFiles(container, fileNames)
			val folder=obj as Folder
			folder.files.addAll(filesToAdd)
		]
	}

	def List<File> getFiles(IContainer container, List<String> fileNames){
		val List<File>entriesToAdd=newArrayList
		val sorter=Collator.getInstance(Locale.GERMANY)
		fileNames.sortInplace([a,b|sorter.compare(a,b)])
		.filter[name|!name.isIgnoreFileName]
		.forEach[fileName|
			val target=container.getFile(new Path(fileName))
			val factory =JtagFactory.eINSTANCE
			val newEntry=factory.createFile
			newEntry.setFileName(factory.createFileName)
			newEntry.fileName.setFileName(maybeEscape(target.fullPath.lastSegment))
			val date=getDate(target)
			newEntry.setDate(date)
			newEntry.tags.add("quickfix")
			if(date===null){
				newEntry.tags.add("noDate")
			}
			entriesToAdd.add(newEntry)
		]
		return entriesToAdd
	}

	def private boolean isIgnoreFileName(String fileName){
		if(fileName==".project" || fileName.endsWith(".jtag")){
			return true
		}
		return false;
	}

	def String maybeEscape(String fileName){
		//rough approximation of the FileNameWithExtension rule
		//if the name matches - no escaping necessary
		val char dot='.'
		if(fileName.matches("[a-zA-Z_][a-zA-Z0-9._-]*")){
			val int firstDotIndex=fileName.indexOf(dot)
			if(firstDotIndex<=0 || fileName.substring(firstDotIndex+1).indexOf(dot) <=0){
				return fileName;
			}
		}
		return '''"«fileName»"'''
	}

	def private static String getDate(IFile f){
		try{
			val Metadata metadata = ImageMetadataReader.readMetadata(f.contents);
			val dir= metadata.directories.filter(ExifSubIFDDirectory).head
			if(dir!==null){
				val date=(dir as ExifSubIFDDirectory).getDateOriginal(TimeZone.^default)
				val SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd")
				return format.format(date)
			}
		}catch(Exception e){
			//ignore
		}
		return null
	}
}