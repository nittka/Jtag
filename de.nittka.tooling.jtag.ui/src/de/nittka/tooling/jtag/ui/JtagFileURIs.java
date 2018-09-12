package de.nittka.tooling.jtag.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.URI;

import de.nittka.tooling.jtag.jtag.File;
import de.nittka.tooling.jtag.jtag.FileName;

public class JtagFileURIs {

	public static URI getReferencedResourceURI(FileName file){
		try{
			return file.eResource().getURI().trimSegments(1).appendSegment(file.getFileName());
		}catch(Exception e){
			//partial name cannot reference existing file
			return null;
		}
	}

	public static String getImageLocation(File file){
		IResource wsFile = ResourcesPlugin.getWorkspace().getRoot().findMember(file.eResource().getURI().toPlatformString(true));
		if(wsFile.exists()){
			FileName name = file.getFileName();
			IPath folder = wsFile.getLocation().removeLastSegments(1);
			IPath fileLocation = folder.append(name.getFileName());
			return fileLocation.toString();
		}
		return null;
	}
}
