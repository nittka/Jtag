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

class JtagUIValidator extends JtagValidator {

	public static val MISSING_JTAG_FILE="missingJtag"

	@Inject IWorkspace ws;

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

	def static List<String> getFilesWithoutDefinition(Folder folder, IWorkspace workspace){
		val file=workspace.root.getFile(new Path(folder.eResource.getURI.toPlatformString(true)))
		val existingJtag=folder.files.map[fileName.fileName]
		val folderIgnores=folder.ignore.map[p|p.replaceAll("\\.","\\\\.").replaceAll("\\*","\\.*").replaceAll("\\(","\\\\(").replaceAll("\\)","\\\\)")]
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

	@Check(CheckType.NORMAL)
	def checkAllFilesHaveDescription(Folder folder) {
		val List<String> missingFiles=getFilesWithoutDefinition(folder, ws)
		if(!missingFiles.empty){
			error('''no description for: «missingFiles.join(",\n")»''', JtagPackage.Literals.FOLDER__DESC, 
							MISSING_JTAG_FILE, missingFiles.join(";;"))
		}
	}
}