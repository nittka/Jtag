package de.nittka.tooling.jtag.ui.linking;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.resource.DefaultLocationInFileProvider;
import org.eclipse.xtext.util.ITextRegion;

import de.nittka.tooling.jtag.jtag.File;

public class JtagLocationInFileProvider extends
		DefaultLocationInFileProvider {

	@Override
	public ITextRegion getSignificantTextRegion(EObject obj) {
		if(obj instanceof File){
			File doc = ((File) obj);
			if(doc.getFileName() != null){
				return getFullTextRegion(doc.getFileName());
			}
		}
		return super.getSignificantTextRegion(obj);
	}
}
