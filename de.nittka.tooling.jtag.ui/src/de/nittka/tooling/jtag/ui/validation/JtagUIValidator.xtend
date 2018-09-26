package de.nittka.tooling.jtag.ui.validation

import de.nittka.tooling.jtag.jtag.FileName
import de.nittka.tooling.jtag.jtag.Folder
import de.nittka.tooling.jtag.jtag.JtagPackage
import de.nittka.tooling.jtag.ui.JtagFileURIs
import de.nittka.tooling.jtag.validation.JtagValidator
import java.util.List
import javax.inject.Inject
import org.eclipse.core.resources.IContainer
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IWorkspace
import org.eclipse.core.runtime.Path
import org.eclipse.xtext.validation.Check
import org.eclipse.xtext.validation.CheckType
import de.nittka.tooling.jtag.jtag.TagSearch
import de.nittka.tooling.jtag.ui.search.JtagTagCounter

class JtagUIValidator extends JtagValidator {

	public static val MISSING_JTAG_FILE="missingJtag"

	@Inject IWorkspace ws;
	@Inject JtagTagCounter tagCounter;

	@Check
	def checkFileExistence(FileName file) {
		val referencedResouce=JtagFileURIs.getReferencedResourceURI(file)
		if(referencedResouce!==null){
			val referencedIFile=ws.root.getFile(new Path(referencedResouce.toPlatformString(true)))
			if(!referencedIFile.exists){
				error('''«referencedResouce.lastSegment» does not exists''', JtagPackage.Literals.FILE_NAME__FILE_NAME)
			}
		}
	}

	def static List<String> getFilesWithoutDefinition(Folder folder, IWorkspace workspace, boolean considerIgnorePatterns){
		val file=workspace.root.getFile(new Path(folder.eResource.getURI.toPlatformString(true)))
		val existingJtag=folder.files.map[fileName.fileName]
		val List<String>folderIgnores=if(considerIgnorePatterns){
			folder.ignore.map[prepareIgnorePattern]
		}else{
			#[]
		}
		val List<String> missingFiles=newArrayList
		if(file.exists){
			val container=file.parent
			container.accept([resource|
				switch(resource){
					IContainer: return container==resource
					IFile case resource.fileExtension=="jtag": return false
					IFile case resource.name==".project": return false
					IFile: {
						val name=resource.name
						if(!existingJtag.contains(name)){
							if(!folderIgnores.exists[p|name.matches(p)]){
								missingFiles.add(name)
							}
						}
						return false
					}
					default: throw new IllegalStateException("unknown case "+resource)
				}
			])
		}
		return missingFiles
	}

	def private static String prepareIgnorePattern(String patternString){
		return patternString.replaceAll("\\.","\\\\.").replaceAll("\\*","\\.*").replaceAll("\\(","\\\\(").replaceAll("\\)","\\\\)")
	}

	@Check(CheckType.NORMAL)
	def checkUnusedIgnorePatterns(Folder folder){
		if(!folder.ignore.empty){
			val missingFiles=getFilesWithoutDefinition(folder, ws, false)
			folder.ignore.forEach[pattern, index|
				if(!missingFiles.exists[file|file.matches(pattern.prepareIgnorePattern())]){
					warning("no file without description matches this pattern", folder, JtagPackage.Literals.FOLDER__IGNORE, index)
				}
			]
		}
	}

	@Check(CheckType.NORMAL)
	def checkAllFilesHaveDescription(Folder folder) {
		val List<String> missingFiles=getFilesWithoutDefinition(folder, ws, true)
		if(!missingFiles.empty){
			error('''no description for: «missingFiles.join(",\n")»''', JtagPackage.Literals.FOLDER__DESCRIPTION, 
							MISSING_JTAG_FILE, missingFiles.join(";;"))
		}
	}

	@Check
	def checkSearchTag(TagSearch search){
		if(search.tag!==null){
			if(!tagCounter.getTags(search.eResource).contains(search.tag)){
				warning("unused tag", JtagPackage.Literals.TAG_SEARCH__TAG)
			}
		}
	}
}