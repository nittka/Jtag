package de.nittka.tooling.jtag.ui;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.URI;

import com.google.common.collect.ImmutableList;

import de.nittka.tooling.jtag.jtag.File;
import de.nittka.tooling.jtag.jtag.FileName;

public class JtagFileURIs {

	//tif, tiff works for image preview but not for html rendering
	private static List<String> SUPPORTED_IMAGE_EXTENSION=ImmutableList.of("jpg","jpeg","jpe","gif","png","bmp");
	private static List<String> KNOWN_MOVIE_EXTENSION=ImmutableList.of("avi","mov","mpg","mpeg","mp4","wmv","divx");

	public static boolean isImageExtension(String fileExtension){
		return fileExtension!=null && SUPPORTED_IMAGE_EXTENSION.contains(fileExtension.toLowerCase());
	}

	public static boolean isMovieExtension(String fileExtension){
		return fileExtension!=null && KNOWN_MOVIE_EXTENSION.contains(fileExtension.toLowerCase());
	}

	public static URI getReferencedResourceURI(FileName file){
		try{
			return file.eResource().getURI().trimSegments(1).appendSegment(file.getFileName());
		}catch(Exception e){
			//partial name cannot reference existing file
			return null;
		}
	}

	public static String getImageLocation(URI imageFileURI){
		IResource wsFile = ResourcesPlugin.getWorkspace().getRoot().findMember(imageFileURI.toPlatformString(true));
		if(wsFile.exists() && isImageExtension(imageFileURI.fileExtension())){
			return wsFile.getLocation().toString();
		}
		return null;
	}
	
	/**
	 * location of files with image extension supported for both Jtag view and html (hover and search)
	 * */
	public static String getImageLocation(File file){
		IPath filePath=getFilePath(file);
		if(filePath!=null && isImageExtension(filePath.getFileExtension())){
			return filePath.toString();
		}
		return null;
	}

	public static IPath getFilePath(File file){
		IResource wsFile = ResourcesPlugin.getWorkspace().getRoot().findMember(file.eResource().getURI().toPlatformString(true));
		if(wsFile.exists()){
			FileName name = file.getFileName();
			IPath folder = wsFile.getLocation().removeLastSegments(1);
			IPath fileLocation = folder.append(name.getFileName());
			return fileLocation;
		}
		return null;
	}
}
