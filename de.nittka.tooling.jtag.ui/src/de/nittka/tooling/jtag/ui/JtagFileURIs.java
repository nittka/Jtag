package de.nittka.tooling.jtag.ui;

import org.eclipse.emf.common.util.URI;

import de.nittka.tooling.jtag.jtag.FileName;

public class JtagFileURIs {

	public static URI getReferencedResourceURI(FileName file){
		try{
			return file.eResource().getURI().trimSegments(1).appendSegment(file.getFileName()).appendFileExtension(file.getExtension());
		}catch(Exception e){
			//partial name cannot reference existing file
			return null;
		}
	}

//	public static IFile getJtagFile(IFile file){
//		if("jtag".equals(file.getFileExtension())){
//			throw new IllegalArgumentException(file+" is already an Jtag file");
//		}
//		IPath path = file.getFullPath().removeFileExtension().addFileExtension("jtag");
//		IFile xarchiveFile=file.getParent().getFile(new Path(path.lastSegment()));
//		return xarchiveFile;
//	}
}
