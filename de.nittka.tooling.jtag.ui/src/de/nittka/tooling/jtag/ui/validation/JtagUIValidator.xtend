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

	public static val MISSING_XARCHIVE_FILE="missingJtag"

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

	@Check(CheckType.NORMAL)
	def checkAllFilesHaveDescription(Folder folder) {
		val file=ws.root.getFile(new Path(folder.eResource.getURI.toPlatformString(true)))
		val existingJtag=folder.files.map[fileName.fileName+"."+fileName.extension]
		val folderIgnores=folder.ignore.map[p|p.replaceAll("\\.","\\\\.").replaceAll("\\*","\\.*")]
		if(file.exists){
			val List<String> missingFiles=newArrayList
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
			if(!missingFiles.empty){
				error('''no description for: «missingFiles.join(",\n")»''', JtagPackage.Literals.FOLDER__DESC, 
								MISSING_XARCHIVE_FILE, missingFiles.join(";;"))
			}
		}
	}
}